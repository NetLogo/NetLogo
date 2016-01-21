// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite
import ConstraintSpecification.ChoiceConstraintSpecification

class WidgetTests extends FunSuite {
  test("Chooser handles choices with lists") {
    val l = LogoList(Seq(1, 2, 3).map(_.toDouble))

    val inputChoices = List(ChooseableList(l), ChooseableDouble(4.toDouble))
    val expectedChoices = List[AnyRef](l, 4.0.asInstanceOf[AnyRef]).asInstanceOf[List[AnyRef]]
    val chooser = Chooser(display = "FOOBAR", varName = "FOOBAR", choices = inputChoices)
    assertResult(ChoiceConstraintSpecification(expectedChoices, 0))(chooser.constraint)
  }
}
