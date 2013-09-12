// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestBooleans extends DockingSuite {

  test("not") { implicit fixture => import fixture._
    compare("not true")
    compare("not false")
  }
}
