// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.scalatest.FunSuite
import org.nlogo.api.DummyCompilerServices

class TestLoading extends FunSuite {
  test("scientific notation") {
    val manager = new AggregateManagerLite()
    manager.load(SCI_NOT_MODEL,new DummyCompilerServices)
    expect(expected)(manager.innerSource)
  }
  val SCI_NOT_MODEL =
    "1.0E-4\n" +
    "    org.nlogo.sdm.gui.AggregateDrawing 1 \n" +
    "        org.nlogo.sdm.gui.StockFigure \"attributes\" \"attributes\" "+
    "1 \"FillColor\" \"Color\" 225 225 182 164 119 60 40 \n" +
    "            org.nlogo.sdm.gui.WrappedStock \"foo\" \"0\" 0\n" ;
  val expected =
    ";; System dynamics model globals\n" +
    "globals [\n" +
    "  ;; stock values\n" +
    "  foo\n" +
    "  ;; size of each step, see SYSTEM-DYNAMICS-GO\n" +
    "  dt\n" +
    "]\n\n" +
    ";; Initializes the system dynamics model.\n" +
    ";; Call this in your model's SETUP procedure.\n" +
    "to system-dynamics-setup \n" +
    "  reset-ticks\n" +
    "  set dt 1.0E-4\n" +
    "  ;; initialize stock values\n" +
    "  set foo 0\n" +
    "end\n\n" +
    ";; Step through the system dynamics model by performing next iteration of Euler's method.\n" +
    ";; Call this in your model's GO procedure.\n" +
    "to system-dynamics-go\n\n" +
    "  ;; update stock values\n" + 
    "  ;; use temporary variables so order of computation doesn't affect result.\n" +
    "  let new-foo ( foo )\n" +
    "  set foo new-foo\n\n" +
    "  tick-advance dt\n" + 
    "end\n\n" +
    ";; Plot the current state of the system dynamics model's stocks\n" +
    ";; Call this procedure in your model's GO procedure.\n" + 
    "to system-dynamics-do-plot\n" +
    "  if plot-pen-exists? \"foo\" [\n" +
    "    set-current-plot-pen \"foo\"\n" +
    "    plotxy ticks foo\n" +
    "  ]\n" +
    "end\n\n" ;
}
