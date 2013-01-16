// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

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
    expectResult(WHITE_INT)(org.nlogo.api.Color.getRGBInt(p.pcolor))
    expectResult("hiya!")(p.plabel)
    expectResult(BLACK_INT)(org.nlogo.api.Color.getRGBInt(p.plabelColor))
    p.rollback()
    expectResult(BLACK_INT)(org.nlogo.api.Color.getRGBInt(p.pcolor))
    expectResult("label")(p.plabel)
    expectResult(WHITE_INT)(org.nlogo.api.Color.getRGBInt(p.plabelColor))
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
    expectResult(GRAY_INT)(org.nlogo.api.Color.getRGBInt(t.color))
    expectResult("hiya!")(t.labelString)
    expectResult(GRAY_INT)(org.nlogo.api.Color.getRGBInt(t.labelColor))
    expectResult(5.0)(t.lineThickness)
    assert(!t.hidden)
    expectResult(0.0)(t.heading)
    expectResult("circle")(t.shape)
    expectResult(5.0)(t.size)
    t.rollback
    expectResult(WHITE_INT)(org.nlogo.api.Color.getRGBInt(t.color))
    expectResult("label")(t.labelString)
    expectResult(BLACK_INT)(org.nlogo.api.Color.getRGBInt(t.labelColor))
    expectResult(0.0)(t.lineThickness)
    assert(t.hidden)
    expectResult(90.0)(t.heading)
    expectResult("default")(t.shape)
    expectResult(1.0)(t.size)
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
    expectResult(BLACK_INT)(org.nlogo.api.Color.getRGBInt(link.color))
    expectResult("hiya!")(link.labelString)
    expectResult(BLACK_INT)(org.nlogo.api.Color.getRGBInt(link.labelColor))
    expectResult(5.0)(link.lineThickness)
    assert(link.hidden)
    expectResult("other guy")(link.shape)
    link.rollback
    expectResult(GRAY_INT)(org.nlogo.api.Color.getRGBInt(link.color))
    expectResult("")(link.labelString)
    expectResult(WHITE_INT)(org.nlogo.api.Color.getRGBInt(link.labelColor))
    expectResult(0.0)(link.lineThickness)
    assert(!link.hidden)
    expectResult("default")(link.shape)
  }
}
