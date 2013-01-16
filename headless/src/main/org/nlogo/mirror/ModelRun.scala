// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.awt.image.BufferedImage

import org.nlogo.plot.{ Plot, PlotAction, PlotRunner }

trait ModelRunInterface {
  self: ModelRun =>
  def name: String
  def save(outputStream: java.io.OutputStream): Unit
}

class ModelRun(
  var name: String,
  val modelString: String,
  val viewArea: java.awt.geom.Area,
  val backgroundImage: BufferedImage,
  private var _generalNotes: String = "",
  private var _annotations: Map[Int, String] = Map())
  extends ModelRunInterface
  with SavableRun {
  var stillRecording = true

  private var _dirty: Boolean = false
  def dirty = _dirty || data.map(_.dirty).getOrElse(false)
  def dirty_=(value: Boolean) {
    _dirty = value
    if (!_dirty) _data.foreach(_.dirty = false)
  }

  var currentFrameIndex = 0
  def currentFrame = _data.flatMap(_.frame(currentFrameIndex))

  private var _data: Option[Data] = None
  def data = _data

  def generalNotes = _generalNotes
  def generalNotes_=(text: String) {
    _generalNotes = text
    dirty = true
  }

  def start(realPlots: Seq[Plot], mirrorables: Iterable[Mirrorable], plotActions: Seq[PlotAction]) {
    _data = Some(Data(realPlots))
    _data.foreach(_.append(mirrorables, plotActions))
  }

  def load(realPlots: Seq[Plot], rawMirroredUpdates: Seq[Array[Byte]], plotActionSeqs: Seq[Seq[PlotAction]]) {
    val deltas = (rawMirroredUpdates, plotActionSeqs).zipped.map(Delta(_, _))
    _data = Some(Data(realPlots, deltas))
    stillRecording = false
  }

  override def toString = (if (dirty) "* " else "") + name

  object Frame {
    def apply(realPlots: Seq[Plot]) = {
      val plots = realPlots.map(_.clone)
      plots.foreach(_.clear())
      new Frame(Map(), plots)
    }
  }
  case class Frame private (
    mirroredState: Mirroring.State,
    plots: Seq[Plot])
    extends PlotRunner {

    override def getPlot(name: String) =
      plots.find(_.name.equalsIgnoreCase(name))
    override def getPlotPen(plotName: String, penName: String) =
      getPlot(plotName).flatMap(_.getPen(penName))

    def applyDelta(delta: Delta): Frame = {
      val newMirroredState = Mirroring.merge(mirroredState, delta.mirroredUpdate)
      val newFrame = Frame(newMirroredState, plots.map(_.clone))
      delta.plotActions.foreach(newFrame.run)
      newFrame
    }

    def ticks: Option[Double] =
      for {
        entry <- mirroredState.get(AgentKey(Mirrorables.World, 0))
        ticks <- entry.lift(Mirrorables.MirrorableWorld.wvTicks)
      } yield ticks.asInstanceOf[Double]
  }

  object Delta {
    def apply(mirroredUpdate: Update, plotActions: Seq[PlotAction]): Delta =
      Delta(Serializer.toBytes(mirroredUpdate), plotActions)
  }
  case class Delta(
    val rawMirroredUpdate: Array[Byte],
    val plotActions: Seq[PlotAction]) {
    def mirroredUpdate: Update = Serializer.fromBytes(rawMirroredUpdate)
  }

  object Data {
    def apply(realPlots: Seq[Plot]) = new Data(realPlots)
    def apply(realPlots: Seq[Plot], deltas: Seq[Delta]) = {
      val data = new Data(realPlots)
      deltas.foreach(data.appendFrame)
      data
    }
  }

  class Data private (val realPlots: Seq[Plot]) {

    private var _deltas = Seq[Delta]()
    def deltas = _deltas
    def size = _deltas.length

    private var _dirty = false
    def dirty = _dirty
    def dirty_=(value: Boolean) { _dirty = value }

    def lastFrameIndex = size - 1
    private def lastFrame = frame(lastFrameIndex).getOrElse(Frame(realPlots))

    private var frameCache = Map[Int, Frame]()
    private def appendFrame(delta: Delta) {
      val newFrame = lastFrame.applyDelta(delta)
      // TODO make interval definable elsewhere
      if (lastFrameIndex % 5 != 0) frameCache -= lastFrameIndex
      frameCache += size -> newFrame
      _deltas :+= delta // added at the end not to mess up lastFrameIndex and size
    }

    def frame(index: Int): Option[Frame] =
      if (!_deltas.isDefinedAt(index))
        None
      else frameCache.get(index)
        .orElse(frame(index - 1).map(_.applyDelta(deltas(index))))

    def append(mirrorables: Iterable[Mirrorable], plotActions: Seq[PlotAction]) {
      val (newMirroredState, mirroredUpdate) =
        Mirroring.diffs(lastFrame.mirroredState, mirrorables)
      val delta = Delta(mirroredUpdate, plotActions)
      appendFrame(delta)
      _dirty = true
    }

  }

}
