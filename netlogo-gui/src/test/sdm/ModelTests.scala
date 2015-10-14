// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.scalatest.FunSuite
import org.nlogo.api.DummyCompilerServices

class ModelTests extends FunSuite {
  val model = {
    val testModel = new Model("ExponentialGrowth",1)
    val stock = new Stock("stock")
    stock.initialValueExpression = "1"
    testModel.addElement(stock)
    val inflow = new Rate("inflow")
    inflow.source = new Reservoir
    inflow.sink = stock
    inflow.expression = "stock ;; I am a comment"
    testModel.addElement(inflow)
    testModel
  }
  test("translator") {
    val translator = new Translator(model,new DummyCompilerServices)
    val expected =
      ";; System dynamics model globals\nglobals [\n" +
      "  ;; stock values\n" +
      "  stock\n" +
      "  ;; size of each step, see SYSTEM-DYNAMICS-GO\n" +
      "  dt\n" +
      "]\n\n" +
      ";; Initializes the system dynamics model.\n" +
      ";; Call this in your model's SETUP procedure.\n" +
      "to system-dynamics-setup\n" +
      "  reset-ticks\n" +
      "  set dt 1.0\n" +
      "  ;; initialize stock values\n" +
      "  set stock 1\n" +
      "end\n\n" +
      ";; Step through the system dynamics model by performing next iteration of Euler's method.\n" +
      ";; Call this in your model's GO procedure.\n" +
      "to system-dynamics-go\n\n" +
      "  ;; compute variable and flow values once per step\n" +
      "  let local-inflow inflow\n\n" +
      "  ;; update stock values\n" +
      "  ;; use temporary variables so order of computation doesn't affect result.\n" +
      "  let new-stock ( stock + local-inflow )\n" +
      "  set stock new-stock\n\n" +
      "  tick-advance dt\n" +
      "end\n\n" +
      ";; Report value of flow\n" +
      "to-report inflow\n" +
      "  report ( stock ;; I am a comment\n  ) * dt\n" +
      "end\n\n" +
      ";; Plot the current state of the system dynamics model's stocks\n" +
      ";; Call this procedure in your plot's update commands.\n" +
      "to system-dynamics-do-plot\n" +
      "  if plot-pen-exists? \"stock\" [\n" +
      "    set-current-plot-pen \"stock\"\n" +
      "    plotxy ticks stock\n" +
      "  ]\n" +
      "end\n\n"
    assertResult(expected)(translator.source)
  }
}
