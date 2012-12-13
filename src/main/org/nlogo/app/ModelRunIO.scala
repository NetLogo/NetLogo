package org.nlogo.app

import java.io.ObjectOutputStream
import javax.imageio.ImageIO
import java.io.ObjectInputStream
import org.nlogo.plot.PlotAction

object ModelRunIO {
  def save(out: ObjectOutputStream, run: ModelRun) {
    // Area is not serializable so we save a shape instead:
    val viewAreaShape = java.awt.geom.AffineTransform
      .getTranslateInstance(0, 0)
      .createTransformedShape(run.viewArea)
    val imageBytes = {
      val byteStream = new java.io.ByteArrayOutputStream
      ImageIO.write(run.backgroundImage, "PNG", byteStream)
      byteStream.close()
      byteStream.toByteArray
    }
    val deltas = run.data.toSeq.flatMap(_.deltas)
    val rawMirroredUpdates = deltas.map(_.rawMirroredUpdate)
    val plotActionFrames = deltas.map(_.plotActions)
    val thingsToSave = Seq(
      run.modelString,
      viewAreaShape,
      imageBytes,
      rawMirroredUpdates,
      plotActionFrames,
      run.generalNotes)
    thingsToSave.foreach(out.writeObject)
    out.close()
  }

  def load(in: ObjectInputStream, name: String): ModelRun = {
    val Seq(
      modelString: String,
      viewShape: java.awt.Shape,
      imageBytes: Array[Byte],
      rawMirroredUpdates: Seq[Array[Byte]],
      plotActionFrames: Seq[Seq[PlotAction]],
      generalNotes: String) = Stream.continually(in.readObject()).take(6)
    in.close()
    val viewArea = new java.awt.geom.Area(viewShape)
    val backgroundImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes))
    val run = new ModelRun(name, modelString, viewArea, backgroundImage, generalNotes)
    run.load(rawMirroredUpdates, plotActionFrames)
    run
  }
}