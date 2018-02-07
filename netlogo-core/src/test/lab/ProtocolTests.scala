// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{ RefEnumeratedValueSet, LabProtocol, SteppedValueSet }
import org.scalatest.FunSuite

class ProtocolTests extends FunSuite {

  val valueSet123 = List(new RefEnumeratedValueSet("foo", List(1d, 2d, 3d).map(Double.box)))
  val valueSet115 = List(new SteppedValueSet("foo", 1d, 1d, 5d))

  test("empty 1") {
    val protocol = LabProtocol.fromValueSets("", "", "", "", 1, true, true, 0, "", Nil, Nil)
    assertResult(1)(protocol.countRuns)
    assertResult("List()")(protocol.refElements.mkString(" "))
  }
  test("empty 2") {
    val protocol = LabProtocol.fromValueSets("", "", "", "", 5, true, true, 0, "", Nil, Nil)
    assertResult(5)(protocol.countRuns)
    assertResult("List() List() List() List() List()")(protocol.refElements.mkString(" "))
  }
  test("enumerated 1") {
    val protocol =
      LabProtocol.fromValueSets("", "", "", "", 1, true, true, 0, "", Nil, valueSet123)
    assertResult(3)(protocol.countRuns)
    assertResult("List((foo,1.0)) List((foo,2.0)) List((foo,3.0))")(
      protocol.refElements.mkString(" "))
  }
  test("enumerated 2") {
    val protocol =
      LabProtocol.fromValueSets("enumerated2", "", "", "", 2, true, true, 0, "", Nil, valueSet123)
    assertResult(6)(protocol.countRuns)
    assertResult("List((foo,1.0)) List((foo,1.0)) List((foo,2.0)) List((foo,2.0)) List((foo,3.0)) List((foo,3.0))")(
      protocol.refElements.mkString(" "))
  }
  test("enumerated 2_") {
    val protocol =
      LabProtocol.fromValueSets("enumerated2", "", "", "", 2, false, true, 0, "", Nil, valueSet123)
    assertResult(6)(protocol.countRuns)
    assertResult("List((foo,1.0)) List((foo,2.0)) List((foo,3.0)) List((foo,1.0)) List((foo,2.0)) List((foo,3.0))")(
      protocol.refElements.mkString(" "))
  }
  test("stepped 1") {
    val protocol = LabProtocol.fromValueSets("", "", "", "", 1, true, true, 0, "", Nil, valueSet115)
    assertResult(5)(protocol.countRuns)
    assertResult("List((foo,1.0)) List((foo,2.0)) List((foo,3.0)) List((foo,4.0)) List((foo,5.0))")(
      protocol.refElements.mkString(" "))
  }
  test("stepped 2") {
    val protocol = LabProtocol.fromValueSets("stepped2", "", "", "", 10, true, true, 0, "", Nil, valueSet115)
    assertResult(50)(protocol.countRuns)
  }
  // bug #62. by doing the calculations in BigDecimal we avoid weird 00000 and 99999 type numbers
  test("avoid floating point error") {
    val protocol = LabProtocol.fromValueSets("stepped3", "", "", "", 1, true, true, 0, "", Nil,
                                List(new SteppedValueSet("foo", 0.1d, 0.1d, 0.5d)))
    assertResult("List((foo,0.1)) List((foo,0.2)) List((foo,0.3)) List((foo,0.4)) List((foo,0.5))")(
      protocol.refElements.mkString(" "))
  }
  test("both") {
    def make(repetitions:Int) =
      LabProtocol.fromValueSets("both", "", "", "", repetitions, true, true, 0, "", Nil,
        valueSet123 :+ new SteppedValueSet("bar", 1d, 1d, 5d))
    val protocol1 = make(1)
    assertResult(15)(protocol1.countRuns)
    assertResult(
      "List((foo,1.0), (bar,1.0)) List((foo,1.0), (bar,2.0)) List((foo,1.0), (bar,3.0)) " +
      "List((foo,1.0), (bar,4.0)) List((foo,1.0), (bar,5.0)) List((foo,2.0), (bar,1.0)) " +
      "List((foo,2.0), (bar,2.0)) List((foo,2.0), (bar,3.0)) List((foo,2.0), (bar,4.0)) " +
      "List((foo,2.0), (bar,5.0)) List((foo,3.0), (bar,1.0)) List((foo,3.0), (bar,2.0)) " +
      "List((foo,3.0), (bar,3.0)) List((foo,3.0), (bar,4.0)) List((foo,3.0), (bar,5.0))")(
      protocol1.refElements.mkString(" "))
    assertResult(45)(make(3).countRuns)
  }
  test("both_") {
    def make(repetitions:Int) =
      LabProtocol.fromValueSets("both", "", "", "", repetitions, false, true, 0, "", Nil,
        valueSet123 :+ new SteppedValueSet("bar", 1d, 1d, 5d))
    val protocol1 = make(1)
    assertResult(15)(protocol1.countRuns)
    assertResult(
      "List((foo,1.0), (bar,1.0)) List((foo,2.0), (bar,1.0)) List((foo,3.0), (bar,1.0)) " +
      "List((foo,1.0), (bar,2.0)) List((foo,2.0), (bar,2.0)) List((foo,3.0), (bar,2.0)) " +
      "List((foo,1.0), (bar,3.0)) List((foo,2.0), (bar,3.0)) List((foo,3.0), (bar,3.0)) " +
      "List((foo,1.0), (bar,4.0)) List((foo,2.0), (bar,4.0)) List((foo,3.0), (bar,4.0)) " +
      "List((foo,1.0), (bar,5.0)) List((foo,2.0), (bar,5.0)) List((foo,3.0), (bar,5.0))")(
      protocol1.refElements.mkString(" "))
    assertResult(45)(make(3).countRuns)
  }
}
