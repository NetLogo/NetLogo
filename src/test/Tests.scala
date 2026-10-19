// You don't need to know or use Scala to run the language tests using the `tests.txt`
// file.  This just gets them running using scalatest, which is the test framework
// NetLogo uses to make the language tests go.  -Jeremy B April 2022

import java.io.File

import org.nlogo.headless.TestLanguage

object Tests {
  val testFileNames = Seq("tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles)
