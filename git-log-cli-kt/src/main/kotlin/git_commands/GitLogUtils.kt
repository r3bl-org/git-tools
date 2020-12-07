package git_commands

import command_interface.Command
import color_console_log.Colors
import git_commands.Constants.Companion.GIT_COMMAND
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

class Constants() {
  companion object {
    val TIMEOUT_SEC: Long = 10L
    val GIT_COMMAND = "/usr/bin/git"
  }
}

/**
 * Simple API for parsing all git log entries for the given git repo in [repoDirectory].
 * 1. This is a blocking call that will spawn an executor (on a single background thread) in order to execute the
 * [Command].
 * 2. This background thread simply streams each line from the command's output and parses it.
 * 3. When the background thread is finished, it unblocks the calling thread.
 */
fun parseAllLogRecords(repoDirectory: String): MutableList<LogRecord> {
  // List of all the LogRecord objects parsed from git log.
  val allLogRecords: MutableList<LogRecord> = mutableListOf()
  // Process each line of git log output.
  var currentLogRecord: LogRecord? = null

  Command(repoDirectory, mutableListOf(GIT_COMMAND, "--no-pager", "log", "--all"))
      .streamAndWait { line ->
        currentLogRecord = streamLineToLogRecord(allLogRecords, currentLogRecord, line)
      }

  return allLogRecords
}

/**
 * Stream each [line] into this function, and parse it into a [LogRecord]. Pass the state into this function as well,
 * to track the [currentLogRecord], and a list [allLogRecords] to collect all the parsed records.
 */
fun streamLineToLogRecord(allLogRecords: MutableList<LogRecord>,
                          currentLogRecord: LogRecord?,
                          line: String
): LogRecord? {
  var myLogRecord: LogRecord? = currentLogRecord

  if (LogRecord.isStartOfRecord(line)) {
    myLogRecord = LogRecord(LogRecord.getShaFromFirstLine(line))
        .apply { allLogRecords.add(this) }
    myLogRecord.lines.add(Colors.Yellow(line))
  }
  else {
    myLogRecord?.lines?.add(line)
  }

  return myLogRecord
}

class StreamGobbler(private val inputStream: InputStream, private val consumer: Consumer<String>) : Runnable {
  override fun run() {
    BufferedReader(InputStreamReader(inputStream)).lines().forEach(consumer)
  }
}

// Git log data model.

data class LogRecord(val SHA: String,
                     val lines: MutableList<String> = mutableListOf(),
                     val branches: MutableList<String> = mutableListOf()
) {
  val ignoreCase = true

  fun copy(): LogRecord {
    val returnValue = LogRecord(SHA)
    returnValue.lines.addAll(lines)
    returnValue.branches.addAll(branches)
    return returnValue
  }

  fun containsString(searchString: String): Boolean {
    lines.forEach { line: String ->
      if (line.contains(searchString, ignoreCase)) return true
    }
    return false
  }

  /**
   * This is a potentially destructive operation. Once the [searchString] has been replaced w/ the version w/ the
   * [Colors] it will no longer match the [searchString]. Make a defensive copy using [copy] before using this.
   */
  fun colorizeString(searchString: String, color: Colors) {
    for ((index: Int, line: String) in lines.withIndex()) {
      if (line.contains(searchString, ignoreCase)) {
        val newLine: String = line.replace(searchString, color(searchString), ignoreCase)
        lines[index] = newLine
      }
    }
  }

  override fun toString(): String {
    val commitMessage: String = lines.joinToString("\n")
    val containingBranches: String =
        if (branches.isEmpty()) Colors.Red("▶ Not in any branch 🤔\n")
        else Colors.Green("▶ Commit in the following branches 🥳\n") + branches.joinToString("\n")
    return Colors.Yellow("<<<<\n") +
           commitMessage +
           containingBranches +
           Colors.Yellow(">>>>\n")
  }

  companion object {
    private val regex: Regex = """^commit \w{40}""".toRegex()
    fun isStartOfRecord(line: String): Boolean = regex matches line
    fun getShaFromFirstLine(line: String): String = line.split(" ")[1]
  }

  fun loadBranchData(repoDirectory: String) {
    Command(repoDirectory, mutableListOf(GIT_COMMAND, "--no-pager", "branch", "--all", "--contains", SHA))
        .streamAndWait { line ->
          branches.add(Colors.Green(line))
        }
  }
}
