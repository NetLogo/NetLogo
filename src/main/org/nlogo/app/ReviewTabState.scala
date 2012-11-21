package org.nlogo.app

import org.nlogo.mirror
import org.nlogo.mirror.{ Mirrorable, Mirrorables, Mirroring, Serializer }
import javax.swing.AbstractListModel

class ReviewTabState(
  private var _runs: Seq[Run] = Seq[Run](),
  private var _currentRun: Option[Run] = None,
  private var _recordingEnabled: Boolean = false,
  private var _userWarnedForMemory: Boolean = false)
  extends AbstractListModel {

  // ListModel methods:
  override def getElementAt(index: Int): AnyRef = _runs(index)
  override def getSize = _runs.size

  def runs = _runs
  def currentRun = _currentRun
  def currentRunData = for { run <- currentRun; data <- run.data } yield data
  def recordingEnabled = _recordingEnabled
  def recordingEnabled_=(b: Boolean) { _recordingEnabled = b }

  def userWarnedForMemory = _userWarnedForMemory
  def userWarnedForMemory_=(b: Boolean) { _userWarnedForMemory = b }

  def reset() {
    val lastIndex = _runs.size - 1
    _runs = Seq[Run]()
    _currentRun = None
    _userWarnedForMemory = false
    fireIntervalRemoved(this, 0, lastIndex)
  }

  private def avoidDuplicate(name: String) =
    (name +: Stream.from(1).map(i => name + " (" + i + ")"))
      .find(str => _runs.forall(_.name != str))
      .get

  def newRun(name: String, modelString: String, potemkineInterface: PotemkinInterface): Run = {
    val run = new Run(avoidDuplicate(name), modelString, potemkineInterface)
    addRun(run)
  }

  def loadRun(name: String, modelString: String, rawDiffs: Seq[Array[Byte]], potemkineInterface: PotemkinInterface): Run = {
    val run = new Run(avoidDuplicate(name), modelString, potemkineInterface)
    run.load(rawDiffs)
    addRun(run)
  }

  private def addRun(run: Run) = {
    _runs :+= run
    setCurrentRun(run)
    val lastIndex = _runs.size - 1
    fireIntervalAdded(this, lastIndex, lastIndex)
    run
  }

  def setCurrentRun(run: Run) {
    for {
      previousRun <- _currentRun
      if previousRun != run
    } previousRun.stillRecording = false
    _currentRun = Some(run)
  }

  def undirty(run: Run) {
    val index = _runs.indexOf(run)
    if (index != -1) {
      run.dirty = false
      fireContentsChanged(this, index, index)
    }
  }

  def dirty = runs.exists(_.dirty)
  def sizeInBytes: Long = _runs.map(_.sizeInBytes).sum
}

class Run(
  var name: String,
  val modelString: String,
  val potemkinInterface: PotemkinInterface,
  var generalNotes: String = "",
  var annotations: Map[Int, String] = Map()) {
  var stillRecording = true

  private var _dirty: Boolean = false
  def dirty = _dirty || data.map(_.dirty).getOrElse(false)
  def dirty_=(value: Boolean) {
    _dirty = value
    if (!_dirty) _data.foreach(_.dirty = false)
  }

  private var _data: Option[RunData] = None
  def data = _data

  def sizeInBytes = _data.map(_.sizeInBytes).getOrElse(0L)

  def load(rawDiffs: Seq[Array[Byte]]) {
    _data = Some(RunData.load(rawDiffs))
    stillRecording = false
  }
  def append(mirrorables: Iterable[Mirrorable]) {
    if (_data.isEmpty)
      _data = Some(RunData.start(mirrorables))
    else
      _data.foreach(_.append(mirrorables))
  }

  override def toString = (if (dirty) "* " else "") + name
}

object RunData {
  def start(mirrorables: Iterable[Mirrorable]): RunData = {
    val data = new RunData(Seq())
    data.append(mirrorables)
    data.setCurrentFrame(0)
    data
  }
  def load(rawDiffs: Seq[Array[Byte]]): RunData = {
    val data = new RunData(rawDiffs)
    for (diff <- rawDiffs) data.appendFrame(data.merge(data.lastFrame, diff))
    data.setCurrentFrame(0)
    data
  }
}

class RunData private (private var _rawDiffs: Seq[Array[Byte]]) {

  // TODO: now that we have a frame cache, that should be accounted for in memory size
  def sizeInBytes = _rawDiffs.map(_.size.toLong).sum
  def rawDiffs = _rawDiffs
  def size = _rawDiffs.length
  private var _dirty = false
  def dirty = _dirty
  def dirty_=(value: Boolean) { _dirty = value }

  private var frameCache = Map[Int, Mirroring.State]()

  private var _currentFrame: Mirroring.State = Map()
  private var _currentFrameIndex = -1

  def currentFrame = _currentFrame
  def currentFrameIndex = _currentFrameIndex
  def setCurrentFrame(index: Int) {
    if (!_rawDiffs.isDefinedAt(index)) {
      val msg = "Frame " + index + " does not exist. " +
        "Sequence size is " + _rawDiffs.size
      throw new IllegalArgumentException(msg)
    }
    val newCurrentFrame = frame(index)
    _currentFrame = newCurrentFrame
    _currentFrameIndex = index
  }

  private var _lastFrame: Mirroring.State = Map()
  private var _lastFrameIndex = -1
  def lastFrame = _lastFrame
  def lastFrameIndex = _lastFrameIndex

  private def appendFrame(newFrame: Mirroring.State) {
    val stateCacheInterval = 5 // maybe this should be definable somewhere else
    _lastFrameIndex += 1
    if (_lastFrameIndex % stateCacheInterval == 0)
      frameCache += _lastFrameIndex -> newFrame
    _lastFrame = newFrame
  }

  def currentTicks = ticksAt(_currentFrame)
  def ticksAtLastFrame = ticksAt(_lastFrame)
  def ticksAt(frame: Mirroring.State): Option[Double] = {
    for {
      entry <- frame.get(mirror.AgentKey(Mirrorables.World, 0))
      result = entry(Mirrorables.MirrorableWorld.wvTicks).asInstanceOf[Double]
      if result != -1
    } yield result
  }

  private def merge(frame: Mirroring.State, diff: Array[Byte]) =
    Mirroring.merge(frame, Serializer.fromBytes(diff))

  def frame(index: Int): Mirroring.State = {
    if (!_rawDiffs.isDefinedAt(index)) {
      val msg = "Frame " + index + " does not exist. " +
        "Sequence size is " + _rawDiffs.size
      throw new IllegalArgumentException(msg)
    }
    if (index == currentFrameIndex)
      _currentFrame
    else if (index == lastFrameIndex)
      _lastFrame
    else
      frameCache.getOrElse(index, merge(frame(index - 1), rawDiffs(index)))
  }

  def append(mirrorables: Iterable[Mirrorable]) {
    val (newFrame, diff) = Mirroring.diffs(lastFrame, mirrorables)
    _rawDiffs :+= Serializer.toBytes(diff)
    appendFrame(newFrame)
    _dirty = true
  }

}
