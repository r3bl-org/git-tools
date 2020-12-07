import cli.Constants.Companion.CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID
import cli.Constants.Companion.CMD_SEARCH_FOR_STRING
import cli.Constants.Companion.MSG_NO_ARGS_PROVIDED
import cli.MainEntryPoint
import org.hamcrest.core.StringContains
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainEntryPointTest {

  private val myData = object {
    val outContent = ByteArrayOutputStream()
    val errContent = ByteArrayOutputStream()
    val originalOut = System.out
    val originalErr = System.err
    val homeFolder = System.getProperty("user.home")
  }

  @Before
  fun setUpStreams() {
    System.setOut(PrintStream(myData.outContent))
    System.setErr(PrintStream(myData.errContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(myData.originalOut)
    System.setErr(myData.originalErr)
  }

  @Test
  fun `no arguments passed`() {
    MainEntryPoint.main(emptyArray())
    Assert.assertEquals(MSG_NO_ARGS_PROVIDED + "\n", myData.outContent.toString())
  }

  @Test
  fun `pass searchForString as argument`() {
    val repoFolder = "${myData.homeFolder}/github/notes"
    val searchString = "SHOULD NOT EXIST"
    MainEntryPoint.main(arrayOf(CMD_SEARCH_FOR_STRING, repoFolder, searchString))
    Assert.assertThat(myData.outContent.toString(), StringContains("results that match the search"))
  }

  @Test
  fun `pass searchForGitOriginRevId as argument`() {
    val repoFolder = "${myData.homeFolder}/github/notes"
    val searchString = "SHOULD NOT EXIST"
    MainEntryPoint.main(arrayOf(CMD_SEARCH_FOR_GIT_ORIGIN_REV_ID, repoFolder, searchString))
    Assert.assertThat(myData.outContent.toString(), StringContains("results that match the search"))
  }

}
