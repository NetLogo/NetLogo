package org.nlogo.app

import java.io.ObjectOutputStream
import javax.imageio.ImageIO
import java.io.ObjectInputStream
import org.nlogo.plot.PlotAction
import org.nlogo.plot.Plot
import java.io.OutputStream
import java.io.InputStream
import org.nlogo.api.ModelReader
import org.nlogo.api.ModelSection
import org.nlogo.plot.PlotLoader

trait SavableRun {
  self: ModelRun =>
  def save(outputStream: OutputStream) {
    val oos = new ObjectOutputStream(outputStream)
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
    val plotActionFrames = deltas.map(_.plotActions)
    val thingsToSave = Seq(
      modelString,
      viewAreaShape,
      imageBytes,
      rawMirroredUpdates,
      plotActionFrames,
      generalNotes)
    thingsToSave.foreach(oos.writeObject)
    oos.close()
  }
}

object ModelRunIO {
  def load(inputStream: InputStream, name: String): ModelRun = {
    val in = new java.io.ObjectInputStream(inputStream)
    def read[A]() = in.readObject().asInstanceOf[A]
    val modelString = read[String]()
    val viewShape = read[java.awt.Shape]()
    val imageBytes = read[Array[Byte]]()
    val rawMirroredUpdates = read[Seq[Array[Byte]]]()
    val plotActionFrames = read[Seq[Seq[PlotAction]]]()
    val generalNotes = read[String]()
    in.close()
    val viewArea = new java.awt.geom.Area(viewShape)
    val backgroundImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes))
    val run = new ModelRun(name, modelString, viewArea, backgroundImage, generalNotes)
    val plots = parsePlots(modelString)
    run.load(plots, rawMirroredUpdates, plotActionFrames)
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
      PlotLoader.parsePlot(widget.toArray, plot, identity)
      plot
    }
  }
}
