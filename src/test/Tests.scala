package org.nlogo.extensions.simpler

import java.io.File
import org.nlogo.headless.TestLanguage

object Tests {
  val testFileNames = Seq("Tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles)
