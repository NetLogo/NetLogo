// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.scalatest.FunSuite

class OverridableTests extends FunSuite {
  val BLACK = new java.lang.Double(0)
  val WHITE = new java.lang.Double(9.9)
  val GRAY = new java.lang.Double(5)
  val WHITE_INT = -1
  val BLACK_INT = -16777216
  val GRAY_INT = -7500403
  test("patch") {
    val p = new PatchData(10, PatchData.COMPLETE, 0, 0, BLACK, "label", WHITE)
    p.set(0, WHITE)
    p.set(1, "hiya!")
    p.set(2, BLACK)
    expect(WHITE_INT)(org.nlogo.api.Color.getRGBInt(p.pcolor))
    expect("hiya!")(p.plabel)
    expect(BLACK_INT)(org.nlogo.api.Color.getRGBInt(p.plabelColor))
    p.rollback()
    expect(BLACK_INT)(org.nlogo.api.Color.getRGBInt(p.pcolor))
    expect("label")(p.plabel)
    expect(WHITE_INT)(org.nlogo.api.Color.getRGBInt(p.plabelColor))
  }
  test("turtle") {
    val t = new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true, "label", BLACK, 0, 0)
    t.set(0, GRAY)
    t.set(1, "hiya!")
    t.set(2, GRAY)
    t.set(3, Double.box(5))
    t.set(4, java.lang.Boolean.FALSE)
    t.set(5, Double.box(0))
    t.set(6, "circle")
    t.set(7, Double.box(5.0))
    expect(GRAY_INT)(org.nlogo.api.Color.getRGBInt(t.color))
    expect("hiya!")(t.labelString)
    expect(GRAY_INT)(org.nlogo.api.Color.getRGBInt(t.labelColor))
    expect(5.0)(t.lineThickness)
    assert(!t.hidden)
    expect(0.0)(t.heading)
    expect("circle")(t.shape)
    expect(5.0)(t.size)
    t.rollback
    expect(WHITE_INT)(org.nlogo.api.Color.getRGBInt(t.color))
    expect("label")(t.labelString)
    expect(BLACK_INT)(org.nlogo.api.Color.getRGBInt(t.labelColor))
    expect(0.0)(t.lineThickness)
    assert(t.hidden)
    expect(90.0)(t.heading)
    expect("default")(t.shape)
    expect(1.0)(t.size)
  }
  test("link") {
    val link = new LinkData(0L, 0L, 1L, LinkData.COMPLETE, 0.0, 0.0, 0.0, 0.0,
      "default", GRAY, false, "", WHITE, 0.0, false,
      1.0, 0.0, 0.0, 0)
    link.set(0, BLACK)
    link.set(1, "hiya!")
    link.set(2, BLACK)
    link.set(3, Double.box(5))
    link.set(4, java.lang.Boolean.TRUE)
    link.set(5, "other guy")
    expect(BLACK_INT)(org.nlogo.api.Color.getRGBInt(link.color))
    expect("hiya!")(link.labelString)
    expect(BLACK_INT)(org.nlogo.api.Color.getRGBInt(link.labelColor))
    expect(5.0)(link.lineThickness)
    assert(link.hidden)
    expect("other guy")(link.shape)
    link.rollback
    expect(GRAY_INT)(org.nlogo.api.Color.getRGBInt(link.color))
    expect("")(link.labelString)
    expect(WHITE_INT)(org.nlogo.api.Color.getRGBInt(link.labelColor))
    expect(0.0)(link.lineThickness)
    assert(!link.hidden)
    expect("default")(link.shape)
  }
}
