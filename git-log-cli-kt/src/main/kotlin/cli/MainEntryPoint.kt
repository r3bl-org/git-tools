@file:JvmName("MainEntryPoint")

package cli

import cli.Constants.Companion.CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID
import cli.Constants.Companion.CMD_SEARCH_FOR_STRING
import cli.Constants.Companion.MSG_NO_ARGS_PROVIDED

class Constants() {
  companion object {
    const val CMD_SEARCH_FOR_STRING = "searchForString"
    const val CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID = "searchForGitOriginRevId"
    const val MSG_NO_ARGS_PROVIDED =
        "Please pass an argument, either '${CMD_SEARCH_FOR_STRING}' or '${CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID}'"
  }
}

class MainEntryPoint {
  companion object {

    @JvmStatic
    fun main(args: Array<String>) {
      if (args.isEmpty()) println(MSG_NO_ARGS_PROVIDED)
      else process(args)
    }

    private fun process(args: Array<String>) {
      val command = args[0]
      val remainingArgs = args.copyOfRange(1, args.size)
      when (command) {
        CMD_SEARCH_FOR_STRING -> searchGitLogForString(remainingArgs)
        CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID -> searchGitLogForOriginRevId(remainingArgs)
      }
    }
  }
}