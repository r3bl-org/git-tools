package cli

import color_console_log.ColorConsoleContext.Companion.colorConsole
import color_console_log.Colors
import color_console_log.Colors.*
import git_commands.LogRecord
import git_commands.parseAllLogRecords

fun main() {
  searchGitLogForString(emptyArray())
}

fun searchGitLogForString(args: Array<String>) {

  val defaults = object {
    val SearchString = "Mon Jun 15"
    val HomeFolder = System.getProperty("user.home")
    val GitRepoDirectory = "$HomeFolder/studio-master-dev/tools/adt/idea/"
  }

  if (args.isEmpty()) {
    colorConsole {//this: ColorConsoleContext
      printLine(" ", prefixWithTimestamp = false) {//this: MutableList<String>
        span(Cyan, "Usage: ${Constants.CMD_SEARCH_FOR_STRING}")
        span(Yellow, """"<folder-with-git-repo>"""")
        span(Purple, """"<your search string>"""")
      }
      printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
        span(Cyan,
             "  Eg: ${Constants.CMD_SEARCH_FOR_STRING} \"${defaults.SearchString}\" \"${defaults.GitRepoDirectory}\"")
      }
    }
  }

  val repoDirectory: String = if (args.size > 1) args[0] else defaults.GitRepoDirectory
  val searchString: String = if (args.isNotEmpty()) args[1] else defaults.SearchString

  colorConsole {//this: ColorConsoleContext
    printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
      span(Purple, "searchString")
      span(Colors.Blue, searchString)
    }
    printLine(": ", prefixWithTimestamp = false) {//this: MutableList<String>
      span(Purple, "directory")
      span(Colors.Blue, repoDirectory)
    }
  }

  val allLogRecords: MutableList<LogRecord> = parseAllLogRecords(repoDirectory)
  val searchResults: List<LogRecord> = allLogRecords.filter { it.containsString(searchString) }
  searchResults.forEach {
    it.loadBranchData(repoDirectory)
    it.colorizeString(searchString, Colors.Blue)
  }

  val colorizedCopyOfSearchResults = mutableListOf<LogRecord>()
  searchResults.forEach {
    val copyOfLogRecord: LogRecord = it.copy()
    copyOfLogRecord.colorizeString(searchString, Cyan)
    colorizedCopyOfSearchResults.add(copyOfLogRecord)
  }

  println(colorizedCopyOfSearchResults.distinct().joinToString("\n"))

  colorConsole {//this: ColorConsoleContext
    printLine {//this: MutableList<String>
      span(Purple, "Searched thru ${allLogRecords.size} log records")
      span(Colors.Blue, "found ${searchResults.size} results that match the search")
    }
  }
}
