// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.AnyFunSuiteEx

class LocalFileTests extends AnyFunSuiteEx {
  test("file path") {
    new LocalFile(TestEnvironment.projectFilePath("resources/system/tokens.txt")).getAbsolutePath
  }
}
