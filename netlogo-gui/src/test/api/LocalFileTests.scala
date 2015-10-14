// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class LocalFileTests extends FunSuite {
  test("file path") {
    new LocalFile(TestEnvironment.projectFilePath("resources/system/tokens.txt")).readFile()
  }
}
