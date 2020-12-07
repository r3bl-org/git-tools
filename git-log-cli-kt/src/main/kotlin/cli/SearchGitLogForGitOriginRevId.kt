package cli

import color_console_log.ColorConsoleContext.Companion.colorConsole
import color_console_log.Colors
import color_console_log.Colors.*
import git_commands.LogRecord
import git_commands.parseAllLogRecords

fun main() {
  searchGitLogForOriginRevId(emptyArray())
}

fun searchGitLogForOriginRevId(args: Array<String>) {

  val defaults = object {
    val GitOriginRevIdOrSHA = "19a2e176823cd6944b10a880cf0f38f384ef33df" // equivalent SHA is "d9741ae".
    val HomeFolder = System.getProperty("user.home")
    val GitRepoDirectory = "$HomeFolder/studio-master-dev/tools/adt/idea/"
    val lineSeparator: String = "-".repeat(75)
  }

  // Display functions.
  fun printFooter(allLogRecords: MutableList<LogRecord>, finalSearchResults: List<LogRecord>) {
    colorConsole {//this: ColorConsoleContext
      printLine(prefixWithTimestamp = false) {//this: MutableList<String>
        span(Red, defaults.lineSeparator)
      }
      printLine(spanSeparator = " ", prefixWithTimestamp = false) {//this: MutableList<String>
        span(Red, "Searched thru")
        span(Blue, "${allLogRecords.size}")
        span(Red, "log records")
        span(Red, "found")
        span(Blue, "${finalSearchResults.size}")
        span(Red, "results that match the search 🛑")
      }
      printLine(prefixWithTimestamp = false) {//this: MutableList<String>
        span(Red,
             defaults.lineSeparator)
      }
    }
  }

  fun printResults(searchString: String,
                   logRecordsMatchingSearchString: MutableList<LogRecord>,
                   colorizedCopyOfFinalSearchResults: MutableList<LogRecord>
  ) {
    colorConsole {//this: ColorConsoleContext
      printLine(spanSeparator = "", prefixWithTimestamp = false) {//this: MutableList<String>
        span(White, "Resolve ")
        span(Cyan, "GitOriginRevId/SHA [$searchString] ")
        span(White, "to SHA ")
        span(Green, logRecordsMatchingSearchString.joinToString(", ", "[", "]") { it.SHA })
      }
    }
    println(colorizedCopyOfFinalSearchResults.joinToString("\n"))
  }

  fun printUsageHelp(args: Array<String>) {
    if (args.isEmpty()) {
      colorConsole {//this: ColorConsoleContext
        printLine(" ", prefixWithTimestamp = false) {//this: MutableList<String>
          span(Cyan, "Usage: SearchGitLogForGitOriginRevId.kts")
          span(Yellow, """"<folder-with-git-repo>"""")
          span(Purple, """"<GitOrigin-RevId> or <SHA>"""")
        }
        printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
          span(Cyan,
               "  Eg: SearchGitLogForGitOriginRevId.kts \"${defaults.GitOriginRevIdOrSHA}\" \"${defaults.GitRepoDirectory}\"")
        }
      }
    }
  }

  fun printHeader(searchString: String, repoDirectory: String) {
    colorConsole {//this: ColorConsoleContext
      printLine(prefixWithTimestamp = false) {//this: MutableList<String>
        span(Green,
             defaults.lineSeparator)
      }
      printLine(prefixWithTimestamp = false) {//this: MutableList<String>
        span(Green, "Starting git log search 🚀")
      }
      printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
        span(Green, "  GitOriginRevId/SHA")
        span(Blue, searchString)
      }
      printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
        span(Green, "  Directory")
        span(Blue, repoDirectory)
      }
      printLine(prefixWithTimestamp = false) {//this: MutableList<String>
        span(Green,
             defaults.lineSeparator)
      }
    }
  }

  val repoDirectory: String = if (args.isNotEmpty()) args[0] else defaults.GitRepoDirectory

  /** Can be a "GitOriginRevId" or "SHA". */
  val searchString: String = if (args.size > 1) args[1] else defaults.GitOriginRevIdOrSHA

  printUsageHelp(args)
  printHeader(searchString, repoDirectory)

  // Main logic of the script.
  val allLogRecords: MutableList<LogRecord> = parseAllLogRecords(repoDirectory)

  // GitOriginRevId / SHA -> LogRecords.
  val logRecordsMatchingSearchString: MutableList<LogRecord> =
      allLogRecords.filter { it.containsString(searchString) }
          .toMutableList()

  // Use the SHAs from the LogRecords above to filter all the records.
  val finalSearchResultsWithDupes: MutableList<LogRecord> = mutableListOf()
  logRecordsMatchingSearchString.forEach { logRecordMatchingSearchString: LogRecord ->
    finalSearchResultsWithDupes.addAll(allLogRecords.filter { it.containsString(logRecordMatchingSearchString.SHA) })
  }
  val finalSearchResults = finalSearchResultsWithDupes.distinct()
  finalSearchResults.forEach {
    it.loadBranchData(repoDirectory)
  }

  val colorizedCopyOfFinalSearchResults = mutableListOf<LogRecord>()
  finalSearchResults.forEach {
    val copyOfLogRecord: LogRecord = it.copy()
    copyOfLogRecord.colorizeString(searchString, Colors.Cyan)
    colorizedCopyOfFinalSearchResults.add(copyOfLogRecord)
  }

  printResults(searchString, logRecordsMatchingSearchString, colorizedCopyOfFinalSearchResults)
  printFooter(allLogRecords, finalSearchResults)

}
