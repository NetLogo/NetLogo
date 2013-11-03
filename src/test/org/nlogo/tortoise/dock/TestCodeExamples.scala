// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestCodeExamples extends DockingSuite with SlowTest {

  test("link lattice") { implicit fixture => import fixture._
    open("models/Code Examples/Link Lattice Example.nlogo")
    testCommand("resize-world -6 6 -6 6")
    testCommand("setup-square")
    testCommand("setup-hex")
  }

}
