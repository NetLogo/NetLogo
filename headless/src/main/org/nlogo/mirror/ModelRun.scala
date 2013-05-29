// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.awt.image.BufferedImage

import org.nlogo.api
import org.nlogo.api.Action
import org.nlogo.plot.Plot

class ModelRun(
  var name: String,
  val modelString: String,
  val viewArea: java.awt.geom.Area,
  val fixedViewSettings: FixedViewSettings,
  val interfaceImage: BufferedImage,
  val initialPlots: Seq[Plot],
  val initialDrawingImage: BufferedImage,
  private var _generalNotes: String = "",
  private var _indexedNotes: List[IndexedNote] = Nil)
  extends api.ModelRun
  with FrameCache
  with SavableRun {

  val minFrameCacheSize = 10
  val maxFrameCacheSize = 20

  var stillRecording = true
  private var _dirty: Boolean = true
  def dirty = _dirty
  def dirty_=(value: Boolean) { _dirty = value }

  def generalNotes = _generalNotes
  def generalNotes_=(text: String) { _generalNotes = text; _dirty = true }

  def indexedNotes = _indexedNotes
  def indexedNotes_=(notes: List[IndexedNote]) { _indexedNotes = notes; _dirty = true }

  private var _deltas = IndexedSeq[Delta]()
  def deltas = _deltas
  def size = _deltas.length

  var currentFrameIndex: Option[Int] = None
  def currentFrame: Option[Frame] = currentFrameIndex.flatMap(frame)
  def lastFrameIndex = if (size > 0) Some(size - 1) else None
  def lastFrame: Option[Frame] = lastFrameIndex.flatMap(frame)

  override def toString = (if (_dirty) "* " else "") + name

  def load(deltas: Seq[Delta]) {
    deltas.foreach(appendFrame)
    stillRecording = false
  }

  private def appendFrame(delta: Delta): Frame = {
    val newFrame = lastFrame
      .getOrElse(Frame(Map(), initialPlots, initialDrawingImage))
      .applyDelta(delta)
    addFrameToCache(size, newFrame)
    _deltas :+= delta // added at the end not to mess up lastFrameIndex and size
    newFrame
  }

  def appendData(mirrorables: Iterable[Mirrorable], actions: IndexedSeq[Action]): Frame = {
    val oldMirroredState = lastFrame.map(_.mirroredState).getOrElse(Map())
    val (newMirroredState, mirroredUpdate) = Mirroring.diffs(oldMirroredState, mirrorables)
    val delta = Delta(Serializer.toBytes(mirroredUpdate), actions)
    _dirty = true
    appendFrame(delta)
  }
}

case class Delta(
  val rawMirroredUpdate: Array[Byte],
  val actions: IndexedSeq[Action]) {
  def mirroredUpdate: Update = Serializer.fromBytes(rawMirroredUpdate)
  def size = rawMirroredUpdate.size + actions.size // used in FrameCache cost calculations
}
