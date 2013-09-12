// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestColors extends DockingSuite {

  test("shade-of") { implicit fixture => import fixture._
    compare("shade-of? blue blue")
    compare("shade-of? blue red")
    compare("shade-of? red red")
    compare("shade-of? (blue + 1) blue")
    compare("shade-of? blue (blue + 1)")
    compare("shade-of? (blue - 1) blue")
    compare("shade-of? blue (blue - 1)")
  }

}
