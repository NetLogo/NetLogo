// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ Model, View, Slider }

import org.scalatest.FunSuite

class ModelReaderTest extends FunSuite {
  test("writes and reads sliders without units") {
    import org.nlogo.core.Model
    val slider = Slider(display = "xs", varName = "x")
    val model = Model(widgets = List(slider, View()))
    val parsedModel = ModelReader.parseModel(ModelReader.formatModel(model, null), null)
    assertResult(model.widgets)(parsedModel.widgets)
  }
}
