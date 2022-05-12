// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ConstraintSpecification.ChoiceConstraintSpecification
import org.scalatest.funsuite.AnyFunSuite

class WidgetTests extends AnyFunSuite {
  test("Chooser handles choices with lists") {
    val l = LogoList(Seq(1, 2, 3).map(_.toDouble))

    val inputChoices = List(ChooseableList(l), ChooseableDouble(4.toDouble))
    val expectedChoices = List[AnyRef](l, 4.0.asInstanceOf[AnyRef]).asInstanceOf[List[AnyRef]]
    val chooser = Chooser(display = Some("FOOBAR"), variable = Some("FOOBAR"), choices = inputChoices)
    assertResult(ChoiceConstraintSpecification(expectedChoices, 0))(chooser.constraint)
  }
}
