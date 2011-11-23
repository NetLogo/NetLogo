// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.scalatest.FunSuite
import org.nlogo.api.DummyCompilerServices

class LoaderTests extends FunSuite {
  test("scientific notation") {
    expect(expected)(Loader.load(ScientificDT, new DummyCompilerServices))
  }
  val ScientificDT =
    """|1.0E-4
       |    org.nlogo.sdm.gui.AggregateDrawing 1
       |        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 164 119 60 40
       |            org.nlogo.sdm.gui.WrappedStock "foo" "0" 0
       |""".stripMargin
  val expected =
    """|;; System dynamics model globals
       |globals [
       |  ;; stock values
       |  foo
       |  ;; size of each step, see SYSTEM-DYNAMICS-GO
       |  dt
       |]
       |
       |;; Initializes the system dynamics model.
       |;; Call this in your model's SETUP procedure.
       |to system-dynamics-setup 
       |  reset-ticks
       |  set dt 1.0E-4
       |  ;; initialize stock values
       |  set foo 0
       |end
       |
       |;; Step through the system dynamics model by performing next iteration of Euler's method.
       |;; Call this in your model's GO procedure.
       |to system-dynamics-go
       |
       |  ;; update stock values
       |  ;; use temporary variables so order of computation doesn't affect result.
       |  let new-foo ( foo )
       |  set foo new-foo
       |
       |  tick-advance dt
       |end
       |
       |;; Plot the current state of the system dynamics model's stocks
       |;; Call this procedure in your plot's update commands.
       |to system-dynamics-do-plot
       |  if plot-pen-exists? "foo" [
       |    set-current-plot-pen "foo"
       |    plotxy ticks foo
       |  ]
       |end
       |
       |""".stripMargin
}
