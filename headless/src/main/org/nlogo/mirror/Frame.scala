// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.awt.image.BufferedImage
import java.io.IOException

import org.nlogo.api.PlotAction
import org.nlogo.drawing.{ DrawingAction, DrawingActionRunner }
import org.nlogo.plot.{ BasicPlotActionRunner, Plot }

case class Frame(
  mirroredState: Mirroring.State,
  plots: Seq[Plot],
  drawingImage: BufferedImage) {

  private def newPlots(delta: Delta): Seq[Plot] = {
    val plotActions = delta.actions.collect { case pa: PlotAction => pa }
    val clonedPlots = plots.map(_.clone)
    if (plotActions.nonEmpty) {
      val plotActionRunner = new BasicPlotActionRunner(clonedPlots)
      plotActions.foreach(plotActionRunner.run)
    }
    clonedPlots
  }

  private def newImage(delta: Delta, state: Mirroring.State): BufferedImage = {
    val drawingActions = delta.actions.collect { case da: DrawingAction => da }
    if (drawingActions.nonEmpty) {
      val trailDrawer = new FakeWorld(state).trailDrawer
      trailDrawer.readImage(drawingImage)
      val drawingActionRunner = new DrawingActionRunner(trailDrawer)
      drawingActions.foreach(drawingActionRunner.run)
      trailDrawer.getDrawing.asInstanceOf[BufferedImage]
    } else drawingImage
  }

  def applyDelta(delta: Delta): Frame = {
    val newMirroredState = Mirroring.merge(mirroredState, delta.mirroredUpdate)
    Frame(newMirroredState, newPlots(delta), newImage(delta, newMirroredState))
  }

  def ticks: Option[Double] =
    for {
      entry <- mirroredState.get(AgentKey(Mirrorables.World, 0))
      ticks <- entry.lift(Mirrorables.MirrorableWorld.WorldVar.Ticks.id)
    } yield ticks.asInstanceOf[Double]
}
