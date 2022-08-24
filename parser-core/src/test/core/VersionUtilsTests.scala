// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core


import VersionUtils._
import org.scalatest.funsuite.AnyFunSuite

class VersionUtilsTests extends AnyFunSuite {

  test("numericValue") {
    assert(numericValue("NetLogo (no version)") == 0)
    assert(numericValue("NetLogo 5.2") == 502000)
    assert(numericValue("NetLogo 3D 5.2") == 502000)
    assert(numericValue("NetLogo 5.2.1") == 502010)
    assert(numericValue("NetLogo 4.0") == 400000)
    assert(numericValue("NetLogo 5.2.1") == 502010)
    assert(numericValue("NetLogo 6.0-M1") == 590000)
    assert(numericValue("NetLogo 6.0-RC1") == 595000)
    assert(numericValue("NetLogo 6.0-RC2") == 595001)
    assert(numericValue("NetLogo 6.1-RC1") == 600900)
    assert(numericValue("NetLogo 6.1-BETA1") == 600900)
    assert(numericValue("NetLogo 6.0.1-RC1") == 600006)
    assert(numericValue("NetLogo 3D Preview 5") == 390050)
    assert(numericValue("NetLogo 6.0-CONSTRUCTIONISM-2016-PREVIEW") == 590000)
    assert(numericValue("NetLogo 4.0beta1") == 390001)
    assert(numericValue("NetLogo 3-D Preview 1") == 390010)
    assert(numericValue("NetLogo 2.2pre3") == 201903)
  }

  test("isNetLogoVersionString") {
    assert(isNetLogoVersionString("NetLogo 6.3.0-beta1"))
    assert(isNetLogoVersionString("NetLogo 6.2.0-MC1"))
    assert(isNetLogoVersionString("NetLogo 3D 6.2.0-MC1"))
    assert(!isNetLogoVersionString("NetLogo3D6.2.0-MC1"))
    assert(!isNetLogoVersionString("LASKDlkasdlaksdLAKSD"))
  }

}
