// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.funsuite.AnyFunSuite

class ResourceTests extends AnyFunSuite {
  test("asString") {
    val expected = "\nNetLogo author: Uri Wilensky\n"
    val resource = Resource.asString("/system/about.txt")
    assertResult(expected)(resource.take(expected.size))
  }
}
