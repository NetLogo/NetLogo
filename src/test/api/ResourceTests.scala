// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class ResourceTests extends FunSuite {
  test("getResourceLines") {
    val expected = "NetLogo author: Uri Wilensky\n"
    val resource = Resource.getResourceAsString("/system/about.txt")
    assertResult(expected)(resource.take(expected.size))
  }
}
