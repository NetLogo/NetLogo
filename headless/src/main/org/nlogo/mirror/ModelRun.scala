// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.awt.image.BufferedImage
import org.nlogo.api
import org.nlogo.drawing.DrawingAction
import org.nlogo.plot.{ BasicPlotActionRunner, Plot, PlotAction }
import org.nlogo.api.Action

class ModelRun(
  var name: String,
  val modelString: String,
  val viewArea: java.awt.geom.Area,
  val backgroundImage: BufferedImage,
  private var _generalNotes: String = "",
  private var _annotations: Map[Int, String] = Map())
  extends api.ModelRun
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

  def start(realPlots: Seq[Plot], mirrorables: Iterable[Mirrorable], actions: Seq[Action]) {
    _data = Some(Data(realPlots))
    _data.foreach(_.append(mirrorables, actions))
  }

  def load(realPlots: Seq[Plot], rawMirroredUpdates: Seq[Array[Byte]], actionSeqs: Seq[Seq[Action]]) {
    val deltas = (rawMirroredUpdates, actionSeqs).zipped.map(Delta(_, _))
    _data = Some(Data(realPlots, deltas))
    stillRecording = false
  }

  override def toString = (if (dirty) "* " else "") + name

  object Data {
    def apply(realPlots: Seq[Plot]) = new Data(realPlots)
    def apply(realPlots: Seq[Plot], deltas: Seq[Delta]) = {
      val data = new Data(realPlots)
      deltas.foreach(data.appendFrame)
      data
    }
  }

  class Data private (val realPlots: Seq[Plot]) {

    private var _deltas = IndexedSeq[Delta]()
    def deltas = _deltas
    def size = _deltas.length

    private var _dirty = false
    def dirty = _dirty
    def dirty_=(value: Boolean) { _dirty = value }

    def lastFrameIndex = size - 1
    private def lastFrame = frameCache.get(lastFrameIndex).getOrElse(Frame(realPlots))

    private val frameCache = new FrameCache(deltas _, 10)
    def frame(index: Int) = frameCache.get(index)

    private def appendFrame(delta: Delta) {
      val newFrame = lastFrame.applyDelta(delta)
      frameCache.add(size, newFrame)
      _deltas :+= delta // added at the end not to mess up lastFrameIndex and size
    }

    def append(mirrorables: Iterable[Mirrorable], actions: Seq[Action]) {
      val (newMirroredState, mirroredUpdate) =
        Mirroring.diffs(lastFrame.mirroredState, mirrorables)
      val delta = Delta(mirroredUpdate, actions)
      appendFrame(delta)
      _dirty = true
    }
  }
}

object Delta {
  def apply(mirroredUpdate: Update, actions: Seq[Action]): Delta =
    Delta(Serializer.toBytes(mirroredUpdate), actions)
}

case class Delta(
  val rawMirroredUpdate: Array[Byte],
  val actions: Seq[Action]) {
  def mirroredUpdate: Update = Serializer.fromBytes(rawMirroredUpdate)
}

object Frame {
  def apply(realPlots: Seq[Plot]) = {
    val plots = realPlots.map(_.clone)
    plots.foreach(_.clear())
    new Frame(Map(), plots)
  }
}

case class Frame private (
  mirroredState: Mirroring.State,
  plots: Seq[Plot]) {

  def applyDelta(delta: Delta): Frame = {
    val newMirroredState = Mirroring.merge(mirroredState, delta.mirroredUpdate)
    val newPlots = plots.map(_.clone)
    val plotActionRunner = new BasicPlotActionRunner(newPlots)

    delta.actions.foreach {
      case pa: PlotAction    => plotActionRunner.run(pa)
      case da: DrawingAction => // TODO
    }
    Frame(newMirroredState, newPlots)
  }

  def ticks: Option[Double] =
    for {
      entry <- mirroredState.get(AgentKey(Mirrorables.World, 0))
      ticks <- entry.lift(Mirrorables.MirrorableWorld.wvTicks)
    } yield ticks.asInstanceOf[Double]
}
