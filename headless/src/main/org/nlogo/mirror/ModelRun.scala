// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.awt.image.BufferedImage

import org.nlogo.api
import org.nlogo.api.Action
import org.nlogo.drawing.{ DrawingAction, DrawingActionRunner }
import org.nlogo.plot.{ BasicPlotActionRunner, Plot, PlotAction }

class ModelRun(
  var name: String,
  val modelString: String,
  val viewArea: java.awt.geom.Area,
  val fixedViewSettings: FixedViewSettings,
  val interfaceImage: BufferedImage,
  private var _generalNotes: String = "",
  private var _indexedNotes: List[IndexedNote] = Nil)
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

  def indexedNotes = _indexedNotes
  def indexedNotes_=(notes: List[IndexedNote]) {
    _indexedNotes = notes
    dirty = true
  }

  def start(
    initialPlots: Seq[Plot],
    initialDrawingImage: BufferedImage,
    mirrorables: Iterable[Mirrorable]) {
    _data = Some(Data(initialPlots, initialDrawingImage))
    _data.foreach(_.append(mirrorables, IndexedSeq[Action]()))
  }

  def load(
    initialPlots: Seq[Plot],
    initialDrawingImage: BufferedImage,
    rawMirroredUpdates: Seq[Array[Byte]],
    actionSeqs: Seq[IndexedSeq[Action]]) {
    val deltas = (rawMirroredUpdates, actionSeqs).zipped.map(Delta(_, _))
    _data = Some(Data(initialPlots, initialDrawingImage, deltas))
    stillRecording = false
  }

  override def toString = (if (dirty) "* " else "") + name

  object Data {
    def apply(initialPlots: Seq[Plot], initialDrawingImage: BufferedImage): Data = {
      val cm = initialDrawingImage.getColorModel
      val clonedImage = new BufferedImage(
        cm, initialDrawingImage.copyData(null),
        cm.isAlphaPremultiplied, null)
      new Data(initialPlots.map(_.clone), clonedImage)
    }
    def apply(initialPlots: Seq[Plot], initialDrawingImage: BufferedImage, deltas: Seq[Delta]): Data = {
      val data = apply(initialPlots, initialDrawingImage)
      deltas.foreach(data.appendFrame)
      data
    }
  }

  class Data private (
    val initialPlots: Seq[Plot],
    val initialDrawingImage: BufferedImage) {

    private var _deltas = IndexedSeq[Delta]()
    def deltas = _deltas
    def size = _deltas.length

    private var _dirty = false
    def dirty = _dirty
    def dirty_=(value: Boolean) { _dirty = value }

    def lastFrameIndex = size - 1
    private def lastFrame =
      frameCache.get(lastFrameIndex)
        .getOrElse(Frame(Map(), initialPlots, initialDrawingImage))

    private val frameCache = new FrameCache(deltas _, 10, 20)
    def frame(index: Int) = frameCache.get(index)

    private def appendFrame(delta: Delta) {
      val newFrame = lastFrame.applyDelta(delta)
      frameCache.add(size, newFrame)
      _deltas :+= delta // added at the end not to mess up lastFrameIndex and size
    }

    def append(mirrorables: Iterable[Mirrorable], actions: IndexedSeq[Action]) {
      val (newMirroredState, mirroredUpdate) =
        Mirroring.diffs(lastFrame.mirroredState, mirrorables)
      val delta = Delta(mirroredUpdate, actions)
      appendFrame(delta)
      _dirty = true
    }
  }
}

object Delta {
  def apply(mirroredUpdate: Update, actions: IndexedSeq[Action]): Delta =
    Delta(Serializer.toBytes(mirroredUpdate), actions)
}

case class Delta(
  val rawMirroredUpdate: Array[Byte],
  val actions: IndexedSeq[Action]) {
  def mirroredUpdate: Update = Serializer.fromBytes(rawMirroredUpdate)
  def size = rawMirroredUpdate.size + actions.size // used in FrameCache cost calculations
}

case class Frame private (
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
      ticks <- entry.lift(Mirrorables.MirrorableWorld.wvTicks)
    } yield ticks.asInstanceOf[Double]
}
