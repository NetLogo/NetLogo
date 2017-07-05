// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ View, Slider }

import org.scalatest.FunSuite

class ModelReaderTest extends FunSuite {
  test("writes and reads sliders without units") {
    import org.nlogo.core.Model
    val slider = Slider(display = Some("xs"), variable = Some("xs"))
    val model = Model(widgets = List(slider, View()))
    val parsedModel = ModelReader.parseModel(ModelReader.formatModel(model), null, Map())
    assertResult(model.widgets)(parsedModel.widgets)
  }
}
