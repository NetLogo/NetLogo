// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.funsuite.AnyFunSuite

class LocalFileTests extends AnyFunSuite {
  test("file path") {
    new LocalFile(TestEnvironment.projectFilePath("resources/system/tokens.txt")).getAbsolutePath
  }
}
