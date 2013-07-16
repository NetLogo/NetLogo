// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.io.{ InputStream, ObjectOutputStream, OutputStream }

import org.nlogo.api.{ PlotPenState, PlotState }
import org.nlogo.drawing.imageToBytes
import org.nlogo.plot.{ Plot, PlotPen, PlotPoint }

import javax.imageio.ImageIO

trait SavableRun {
  self: ModelRun =>
  def save(outputStream: OutputStream) {
    val out = new ObjectOutputStream(outputStream)
    // Area is not serializable so we save a shape instead:
    val viewAreaShape = java.awt.geom.AffineTransform
      .getTranslateInstance(0, 0)
      .createTransformedShape(viewArea)
    val interfaceImageBytes = imageToBytes(interfaceImage)
    val savableInitialPlots = initialPlots.map(SavablePlot.fromPlot)
    val initialDrawingImageBytes = imageToBytes(initialDrawingImage)
    val thingsToSave = Seq(
      name,
      modelString,
      viewAreaShape,
      fixedViewSettings,
      interfaceImageBytes,
      deltas,
      savableInitialPlots,
      initialDrawingImageBytes,
      generalNotes,
      indexedNotes)
    thingsToSave.foreach(out.writeObject)
    out.close()
  }
}

object ModelRunIO {
  def load(inputStream: InputStream): ModelRun = {
    def imageFromBytes(bytes: Array[Byte]) = ImageIO.read(new java.io.ByteArrayInputStream(bytes))
    val in = new java.io.ObjectInputStream(inputStream)
    def read[A]() = in.readObject().asInstanceOf[A]
    val name = read[String]()
    val modelString = read[String]()
    val viewArea = new java.awt.geom.Area(read[java.awt.Shape]())
    val fixedViewSettings = read[FixedViewSettings]
    val interfaceImage = imageFromBytes(read[Array[Byte]]())
    val deltas = read[Seq[Delta]]()
    val initialPlots = read[Seq[SavablePlot]].map(_.toPlot)
    val initialDrawingImage = imageFromBytes(read[Array[Byte]]())
    val generalNotes = read[String]()
    val indexedNotes = read[List[IndexedNote]]
    in.close()
    val run = new ModelRun(
      name, modelString, viewArea, fixedViewSettings,
      interfaceImage, initialPlots, initialDrawingImage,
      generalNotes, indexedNotes)
    run.load(deltas)
    run
  }
}

/*
 * I first considered using PlotExporter and the import-world plot importing
 * code for achieving plot serialization, but quickly started to fear for my
 * sanity. The following classes are the result of my prompt retreat. They
 * only contain the bare minimum needed to reconstruct a model run plot,
 * however, so I would advise against using them for anything else.
 * NP 2013-04-10
 */
private object SavablePlot {
  def fromPlot(plot: Plot): SavablePlot =
    SavablePlot(plot.name, plot.state,
      plot.pens.map { pen =>
        SavablePen(pen.name, pen.state, pen.inLegend, pen.points)
      }
    )
}

private case class SavablePlot(
  val name: String,
  val state: PlotState,
  val pens: List[SavablePen]) {
  def toPlot: Plot = {
    val plot = new Plot(name, state)
    pens.map(_.toPlotPen).foreach(plot.addPen)
    plot
  }
}

private case class SavablePen(
  val name: String,
  val state: PlotPenState,
  val inLegend: Boolean,
  val points: Vector[PlotPoint]) {
  def toPlotPen: PlotPen = {
    val plotPen = new PlotPen(
      name = name,
      defaultState = state,
      inLegend = inLegend,
      temporary = false
    )
    plotPen.points = points
    plotPen
  }
}
