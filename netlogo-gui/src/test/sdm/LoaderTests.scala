// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.scalatest.FunSuite
import org.nlogo.api.DummyCompilerServices

// actually this tests Translator too, since Loader calls Translator

class LoaderTests extends FunSuite {

  test("scientific notation") {
    assertResult(expected)(Loader.load(ScientificDT, new DummyCompilerServices))
  }
  val ScientificDT =
    """|1.0E-4
       |    org.nlogo.sdm.gui.AggregateDrawing 1
       |        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 164 119 60 40
       |            org.nlogo.sdm.gui.WrappedStock "foo" "0" 0
       |""".stripMargin.replaceAll("\r\n", "\n")
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
       |""".stripMargin.replaceAll("\r\n", "\n")

  test("issue #35") {
    assertResult(issue35Expected)(Loader.load(issue35Input, new DummyCompilerServices))
  }
  val issue35Input =
    """|0.01
       |    org.nlogo.sdm.gui.AggregateDrawing 9
       |        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 242 296 50 50
       |            org.nlogo.sdm.gui.WrappedConverter "type-1 + ( type-2 * 3)" "type-1-fitness"
       |        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 369 293 50 50
       |            org.nlogo.sdm.gui.WrappedConverter "type-2 * 2" "type-2-fitness"
       |        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 177 107 60 40
       |            org.nlogo.sdm.gui.WrappedStock "type-1" "0.01" 0
       |        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 479 108 60 40
       |            org.nlogo.sdm.gui.WrappedStock "type-2" "0.99" 0
       |        org.nlogo.sdm.gui.ConverterFigure "attributes" "attributes" 1 "FillColor" "Color" 130 188 183 501 286 50 50
       |            org.nlogo.sdm.gui.WrappedConverter "(type-1 * type-1-fitness) + (type-2 * type-2-fitness)" "ave-fitness"
       |        org.nlogo.sdm.gui.ReservoirFigure "attributes" "attributes" 1 "FillColor" "Color" 192 192 192 103 101 30 30
       |        org.nlogo.sdm.gui.RateConnection 3 133 117 149 119 165 121 NULL NULL 0 0 0
       |            org.jhotdraw.figures.ChopEllipseConnector REF 11
       |            org.jhotdraw.standard.ChopBoxConnector REF 5
       |            org.nlogo.sdm.gui.WrappedRate "type-1 * (type-1-fitness - ave-fitness)" "t1-replicator"
       |                org.nlogo.sdm.gui.WrappedReservoir  REF 6 0
       |        org.nlogo.sdm.gui.ReservoirFigure "attributes" "attributes" 1 "FillColor" "Color" 192 192 192 396 110 30 30
       |        org.nlogo.sdm.gui.RateConnection 3 426 125 446 125 467 126 NULL NULL 0 0 0
       |            org.jhotdraw.figures.ChopEllipseConnector REF 17
       |            org.jhotdraw.standard.ChopBoxConnector REF 7
       |            org.nlogo.sdm.gui.WrappedRate "type-2 * (type-2-fitness - ave-fitness)" "t2-replicator"
       |                org.nlogo.sdm.gui.WrappedReservoir  REF 8 0
       |""".stripMargin.replaceAll("\r\n", "\n")
  val issue35Expected =
    """|;; System dynamics model globals
       |globals [
       |  ;; stock values
       |  type-1
       |  type-2
       |  ;; size of each step, see SYSTEM-DYNAMICS-GO
       |  dt
       |]
       |
       |;; Initializes the system dynamics model.
       |;; Call this in your model's SETUP procedure.
       |to system-dynamics-setup
       |  reset-ticks
       |  set dt 0.01
       |  ;; initialize stock values
       |  set type-1 0.01
       |  set type-2 0.99
       |end
       |
       |;; Step through the system dynamics model by performing next iteration of Euler's method.
       |;; Call this in your model's GO procedure.
       |to system-dynamics-go
       |
       |  ;; compute variable and flow values once per step
       |  let local-type-1-fitness type-1-fitness
       |  let local-type-2-fitness type-2-fitness
       |  let local-ave-fitness ave-fitness
       |  let local-t1-replicator t1-replicator
       |  let local-t2-replicator t2-replicator
       |
       |  ;; update stock values
       |  ;; use temporary variables so order of computation doesn't affect result.
       |  let new-type-1 ( type-1 + local-t1-replicator )
       |  let new-type-2 ( type-2 + local-t2-replicator )
       |  set type-1 new-type-1
       |  set type-2 new-type-2
       |
       |  tick-advance dt
       |end
       |
       |;; Report value of flow
       |to-report t1-replicator
       |  report ( type-1 * (type-1-fitness - ave-fitness)
       |  ) * dt
       |end
       |
       |;; Report value of flow
       |to-report t2-replicator
       |  report ( type-2 * (type-2-fitness - ave-fitness)
       |  ) * dt
       |end
       |
       |;; Report value of variable
       |to-report type-1-fitness
       |  report type-1 + ( type-2 * 3)
       |end
       |
       |;; Report value of variable
       |to-report type-2-fitness
       |  report type-2 * 2
       |end
       |
       |;; Report value of variable
       |to-report ave-fitness
       |  report (type-1 * type-1-fitness) + (type-2 * type-2-fitness)
       |end
       |
       |;; Plot the current state of the system dynamics model's stocks
       |;; Call this procedure in your plot's update commands.
       |to system-dynamics-do-plot
       |  if plot-pen-exists? "type-1" [
       |    set-current-plot-pen "type-1"
       |    plotxy ticks type-1
       |  ]
       |  if plot-pen-exists? "type-2" [
       |    set-current-plot-pen "type-2"
       |    plotxy ticks type-2
       |  ]
       |end
       |
       |""".stripMargin.replaceAll("\r\n", "\n")

}
