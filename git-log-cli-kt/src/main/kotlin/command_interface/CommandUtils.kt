package command_interface

import git_commands.Constants
import git_commands.StreamGobbler
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

// Infrastructure to run the commands.

class Command(private val directory: String, private val commands: MutableList<String>) {
  /** The calling thread waits until this command [process] finishes executing or times out after [Constants.TIMEOUT_SEC]. */
  fun streamAndWait(lineHandler: (String) -> Unit) {
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    try {
      // https://developerlife.com/2020/07/15/non-capturing-lambda-problems/ explains the choice of using an anonymous
      // class over the lambda for the 2nd parameter.
      submitToExecutorBlocking(executor, object : Consumer<String> {
        override fun accept(line: String) = lineHandler(line)
      })
    }
    finally {
      executor.shutdown()
    }
    executor.awaitTermination(Constants.TIMEOUT_SEC, TimeUnit.SECONDS)
  }

  /**
   * Run the [consumer] on the provided [executorService]. And the calling thread waits for the [process] to finish (or
   * timeout).
   */
  fun submitToExecutorBlocking(executorService: ExecutorService, consumer: Consumer<String>) {
    val processBuilder: ProcessBuilder = ProcessBuilder().apply {
      directory(File(directory))
      command(commands)
    }
    val process: Process = processBuilder.start()
    executorService.submit(StreamGobbler(process.inputStream, consumer))
    // Wait (on the calling thread) for the Runnable (in executorService's thread) to finish.
    process.waitFor(Constants.TIMEOUT_SEC, TimeUnit.SECONDS)
  }
}

