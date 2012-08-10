// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class ColorTests extends FunSuite {
  test("modulate") {
    expect(0)(Color.modulateDouble(0))
    expect(0)(Color.modulateDouble(140))
    expect(139)(Color.modulateDouble(-1))
    expect(139.9)(Color.modulateDouble(-0.1))
    expect(20)(Color.modulateDouble(1000))
    expect(120)(Color.modulateDouble(-1000))
    expect(139.9999999999999)(Color.modulateDouble(-0.000000000000001))
  }
  test("rgba") {
    val list = LogoList(0: java.lang.Double,
                        0: java.lang.Double,
                        0: java.lang.Double)
    expect((255 << 24))(Color.getARGBIntByRGBAList(list))
  }
}
