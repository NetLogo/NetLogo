// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed trait PlotAction extends Action {
  val plotName: String
}

object PlotAction {

  case class ClearPlot(plotName: String)
    extends PlotAction

  case class PlotY(plotName: String, penName: String, y: Double)
    extends PlotAction

  case class PlotXY(plotName: String, penName: String, x: Double, y: Double)
    extends PlotAction

  case class AutoPlot(plotName: String, on: Boolean)
    extends PlotAction

  case class SetRange(plotName: String, isX: Boolean, min: Double, max: Double)
    extends PlotAction

  case class PenDown(plotName: String, penName: String, down: Boolean)
    extends PlotAction

  case class HidePen(plotName: String, penName: String, hidden: Boolean)
    extends PlotAction

  case class HardResetPen(plotName: String, penName: String)
    extends PlotAction

  case class SoftResetPen(plotName: String, penName: String)
    extends PlotAction

  case class SetPenInterval(plotName: String, penName: String, interval: Double)
    extends PlotAction

  case class SetPenMode(plotName: String, penName: String, mode: Int)
    extends PlotAction

  case class SetPenColor(plotName: String, penName: String, color: Int)
    extends PlotAction

  case class CreateTemporaryPen(plotName: String, penName: String)
    extends PlotAction

}
