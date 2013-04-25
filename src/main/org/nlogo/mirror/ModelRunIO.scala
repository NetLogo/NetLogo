// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.io.{ InputStream, ObjectOutputStream, OutputStream }
import scala.Option.option2Iterable
import org.nlogo.api.{ ModelReader, ModelSection }
import org.nlogo.plot.{ Plot, PlotLoader }
import javax.imageio.ImageIO
import org.nlogo.drawing.DrawingAction
import org.nlogo.api.{ Action, PlotAction }

trait SavableRun {
  self: ModelRun =>
  def save(outputStream: OutputStream) {
    val out = new ObjectOutputStream(outputStream)
    // Area is not serializable so we save a shape instead:
    val viewAreaShape = java.awt.geom.AffineTransform
      .getTranslateInstance(0, 0)
      .createTransformedShape(viewArea)
    val imageBytes = {
      val byteStream = new java.io.ByteArrayOutputStream
      ImageIO.write(backgroundImage, "PNG", byteStream)
      byteStream.close()
      byteStream.toByteArray
    }
    val deltas = data.toSeq.flatMap(_.deltas)
    val rawMirroredUpdates = deltas.map(_.rawMirroredUpdate)
    val actionFrames = deltas.map(_.actions)
    val thingsToSave = Seq(
      name,
      modelString,
      viewAreaShape,
      imageBytes,
      rawMirroredUpdates,
      actionFrames,
      generalNotes)
    thingsToSave.foreach(out.writeObject)
    out.close()
  }
}

object ModelRunIO {
  def load(inputStream: InputStream): ModelRun = {
    val in = new java.io.ObjectInputStream(inputStream)
    def read[A]() = in.readObject().asInstanceOf[A]
    val name = read[String]()
    val modelString = read[String]()
    val viewShape = read[java.awt.Shape]()
    val imageBytes = read[Array[Byte]]()
    val rawMirroredUpdates = read[Seq[Array[Byte]]]()
    val actionFrames = read[Seq[IndexedSeq[Action]]]()
    val generalNotes = read[String]()
    in.close()
    val viewArea = new java.awt.geom.Area(viewShape)
    val backgroundImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes))
    val run = new ModelRun(name, modelString, viewArea, backgroundImage, generalNotes)
    val plots = parsePlots(modelString)
    run.load(plots, rawMirroredUpdates, actionFrames)
    run
  }

  private def parsePlots(model: String): Seq[Plot] = {
    val modelMap = ModelReader.parseModel(model)
    val interfaceSection = modelMap(ModelSection.Interface)
    val widgets = ModelReader.parseWidgets(interfaceSection)
    for {
      widget <- widgets
      if widget.head == "PLOT"
      plot = new Plot("")
    } yield {
      PlotLoader.parsePlot(widget.toArray, plot)
      plot
    }
  }
}
