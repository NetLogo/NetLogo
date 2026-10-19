package org.nlogo.extensions.samplescala

import java.io.File

import org.nlogo.headless.TestLanguage

object Tests {
  // file paths are relative to the repository root
  // this example assumes a single `tests.txt` file
  val testFileNames = Seq("tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles)
