// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.scalatest.FunSuite

class ProtocolTests extends FunSuite {
  test("empty 1") {
    val protocol = new Protocol("", "", "", "", 1, true, 0, "", Nil, Nil)
    expect(1)(protocol.countRuns)
    expect("List()")(protocol.elements.mkString(" "))
  }
  test("empty 2") {
    val protocol = new Protocol("", "", "", "", 5, true, 0, "", Nil, Nil)
    expect(5)(protocol.countRuns)
    expect("List() List() List() List() List()")(protocol.elements.mkString(" "))
  }
  test("enumerated 1") {
    val protocol =
      new Protocol("", "", "", "", 1, true, 0, "", Nil,
                   List(new EnumeratedValueSet("foo", List(1d, 2d, 3d))))
    expect(3)(protocol.countRuns)
    expect("List((foo,1.0)) List((foo,2.0)) List((foo,3.0))")(
      protocol.elements.mkString(" "))
  }
  test("enumerated 2") {
    val protocol =
      new Protocol("enumerated2", "", "", "", 2, true, 0, "", Nil,
                   List(new EnumeratedValueSet("foo", List(1d, 2d, 3d))))
    expect(6)(protocol.countRuns)
    expect("List((foo,1.0)) List((foo,1.0)) List((foo,2.0)) List((foo,2.0)) List((foo,3.0)) List((foo,3.0))")(
      protocol.elements.mkString(" "))
    expect("enumerated2 (6 runs)")(protocol.toString)
  }
  test("stepped 1") {
    val protocol = new Protocol("", "", "", "", 1, true, 0, "", Nil,
                                List(new SteppedValueSet("foo", 1d, 1d, 5d)))
    expect(5)(protocol.countRuns)
    expect("List((foo,1.0)) List((foo,2.0)) List((foo,3.0)) List((foo,4.0)) List((foo,5.0))")(
      protocol.elements.mkString(" "))
  }
  test("stepped 2") {
    val protocol = new Protocol("stepped2", "", "", "", 10, true, 0, "", Nil,
                                List(new SteppedValueSet("foo", 1d, 1d, 5d)))
    expect(50)(protocol.countRuns)
    expect("stepped2 (50 runs)")(protocol.toString)
  }
  // bug #62. by doing the calculations in BigDecimal we avoid weird 00000 and 99999 type numbers
  test("avoid floating point error") {
    val protocol = new Protocol("stepped3", "", "", "", 1, true, 0, "", Nil,
                                List(new SteppedValueSet("foo", 0.1d, 0.1d, 0.5d)))
    expect("List((foo,0.1)) List((foo,0.2)) List((foo,0.3)) List((foo,0.4)) List((foo,0.5))")(
      protocol.elements.mkString(" "))
  }
  test("both") {
    def make(repetitions:Int) =
      new Protocol("both", "", "", "", repetitions, true, 0, "", Nil,
                   List(new EnumeratedValueSet("foo", List(1d, 2d, 3d)),
                        new SteppedValueSet("bar", 1d, 1d, 5d)))
    val protocol1 = make(1)
    expect(15)(protocol1.countRuns)
    expect(
      "List((foo,1.0), (bar,1.0)) List((foo,1.0), (bar,2.0)) List((foo,1.0), (bar,3.0)) " +
      "List((foo,1.0), (bar,4.0)) List((foo,1.0), (bar,5.0)) List((foo,2.0), (bar,1.0)) " +
      "List((foo,2.0), (bar,2.0)) List((foo,2.0), (bar,3.0)) List((foo,2.0), (bar,4.0)) " +
      "List((foo,2.0), (bar,5.0)) List((foo,3.0), (bar,1.0)) List((foo,3.0), (bar,2.0)) " +
      "List((foo,3.0), (bar,3.0)) List((foo,3.0), (bar,4.0)) List((foo,3.0), (bar,5.0))")(
      protocol1.elements.mkString(" "))
    expect(45)(make(3).countRuns)
  }
}
