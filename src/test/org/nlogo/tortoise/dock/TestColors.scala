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

  test("scale-color") { implicit fixture => import fixture._
    compare("scale-color white 10 22 15")
    compare("scale-color white 15 22 15")
    compare("scale-color white 20 22 15")
    compare("scale-color white 25 22 15")
    compare("scale-color blue -30 -20 20")
    compare("scale-color blue -10 -20 20")
    compare("scale-color blue 0 -20 20")
    compare("scale-color blue 10 -20 20")
    compare("scale-color blue 30 -20 20")

    compare("scale-color 11 10 -20 20")
  }
}
