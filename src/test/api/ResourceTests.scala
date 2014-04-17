// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class ResourceTests extends FunSuite {
  test("asString") {
    val expected = "NetLogo author: Uri Wilensky\n"
    val resource = Resource.asString("/system/about.txt")
    assertResult(expected)(resource.take(expected.size))
  }
}
