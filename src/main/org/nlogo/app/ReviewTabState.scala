package org.nlogo.app

import org.nlogo.mirror
import org.nlogo.mirror.{ Mirrorable, Mirrorables, Mirroring }
import javax.swing.AbstractListModel
import org.nlogo.plot.PlotAction
import org.nlogo.plot.Plot
import org.nlogo.mirror.Serializer

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
  def currentlyRecording = _recordingEnabled && currentRun.map(_.stillRecording).getOrElse(false)

  def userWarnedForMemory = _userWarnedForMemory
  def userWarnedForMemory_=(b: Boolean) { _userWarnedForMemory = b }

  def reset() {
    val lastIndex = _runs.size - 1
    _runs = Seq[Run]()
    _currentRun = None
    _userWarnedForMemory = false
    fireIntervalRemoved(this, 0, lastIndex)
  }

  def closeCurrentRun() {
    for (run <- currentRun) {
      val index = _runs.indexOf(run)
      _runs = _runs.filterNot(_ == run)
      fireIntervalRemoved(this, index, index)
    }
  }

  private def avoidDuplicate(name: String) =
    (name +: Stream.from(1).map(i => name + " (" + i + ")"))
      .find(str => _runs.forall(_.name != str))
      .get

  def newRun(name: String, modelString: String, potemkineInterface: PotemkinInterface): Run = {
    val run = new Run(avoidDuplicate(name), modelString, potemkineInterface)
    addRun(run)
  }

  def loadRun(
    name: String,
    modelString: String,
    rawMirroredUpdates: Seq[Array[Byte]],
    plotActionSeqs: Seq[Seq[PlotAction]],
    potemkineInterface: PotemkinInterface,
    generalNotes: String): Run = {
    val run = new Run(avoidDuplicate(name), modelString, potemkineInterface, generalNotes)
    run.load(rawMirroredUpdates, plotActionSeqs)
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
  private var _generalNotes: String = "",
  private var _annotations: Map[Int, String] = Map()) {
  var stillRecording = true

  private var _dirty: Boolean = false
  def dirty = _dirty || data.map(_.dirty).getOrElse(false)
  def dirty_=(value: Boolean) {
    _dirty = value
    if (!_dirty) _data.foreach(_.dirty = false)
  }

  private var _data: Option[Data] = None
  def data = _data

  def generalNotes = _generalNotes
  def generalNotes_=(text: String) {
    _generalNotes = text
    dirty = true
  }

  def sizeInBytes = _data.map(_.sizeInBytes).getOrElse(0L)

  def load(rawMirroredUpdates: Seq[Array[Byte]], plotActionSeqs: Seq[Seq[PlotAction]]) {
    val deltas = (rawMirroredUpdates, plotActionSeqs).zipped.map(Delta(_, _))
    _data = Some(load(deltas))
    stillRecording = false
  }
  def append(mirrorables: Iterable[Mirrorable], plotActions: Seq[PlotAction]) {
    if (_data.isEmpty)
      _data = Some(start(mirrorables, plotActions))
    else
      _data.foreach(_.append(mirrorables, plotActions))
  }

  def start(mirrorables: Iterable[Mirrorable], plotActions: Seq[PlotAction]): Data = {
    val data = new Data(Seq())
    data.append(mirrorables, plotActions)
    data.setCurrentFrame(0)
    data
  }
  def load(deltas: Seq[Delta]): Data = {
    val data = new Data(deltas)
    deltas.foreach(data.appendFrame)
    data.setCurrentFrame(0)
    data
  }

  override def toString = (if (dirty) "* " else "") + name

  case class Frame(
    mirroredState: Mirroring.State,
    plots: Seq[Plot]) {
    def applyDelta(delta: Delta): Frame = {
      val newMirroredState = Mirroring.merge(mirroredState, delta.mirroredUpdate)
      val newPlots = plots // TODO: apply delta.plotActions
      Frame(newMirroredState, newPlots)
    }
  }

  object Delta {
    def apply(mirroredUpdate: mirror.Update, plotActions: Seq[PlotAction]): Delta =
      Delta(Serializer.toBytes(mirroredUpdate), plotActions)
  }
  case class Delta(
    val rawMirroredUpdate: Array[Byte],
    val plotActions: Seq[PlotAction]) {
    def mirroredUpdate: mirror.Update = Serializer.fromBytes(rawMirroredUpdate)
  }

  class Data protected[Run] (private var _deltas: Seq[Delta]) {

    def deltas = _deltas

    // TODO: change memory approach completely
    def sizeInBytes = 0L

    def size = _deltas.length
    private var _dirty = false
    def dirty = _dirty
    def dirty_=(value: Boolean) { _dirty = value }

    private var frameCache = Map[Int, Frame]()

    private var _currentFrame: Frame = Frame(Map(), Seq()) // TODO init plots?
    private var _currentFrameIndex = -1

    def currentFrame = _currentFrame
    def currentFrameIndex = _currentFrameIndex
    def setCurrentFrame(index: Int) {
      if (!_deltas.isDefinedAt(index)) {
        val msg = "Frame " + index + " does not exist. " +
          "Sequence size is " + _deltas.size
        throw new IllegalArgumentException(msg)
      }
      val newCurrentFrame = frame(index)
      _currentFrame = newCurrentFrame
      _currentFrameIndex = index
    }

    private var _lastFrame: Frame = Frame(Map(), Seq())
    private var _lastFrameIndex = -1
    def lastFrame = _lastFrame
    def lastFrameIndex = _lastFrameIndex

    protected[Run] def appendFrame(delta: Delta) {
      val newFrame = lastFrame.applyDelta(delta)
      val frameCacheInterval = 5 // maybe this should be definable somewhere else
      _lastFrameIndex += 1
      if (_lastFrameIndex % frameCacheInterval == 0)
        frameCache += _lastFrameIndex -> newFrame
      _lastFrame = newFrame
    }

    def currentTicks = ticksAt(_currentFrame)
    def ticksAtLastFrame = ticksAt(_lastFrame)
    def ticksAt(frame: Frame): Option[Double] = {
      for {
        entry <- frame.mirroredState.get(mirror.AgentKey(Mirrorables.World, 0))
        result = entry(Mirrorables.MirrorableWorld.wvTicks).asInstanceOf[Double]
        if result != -1
      } yield result
    }

    def frame(index: Int): Frame = {
      if (!_deltas.isDefinedAt(index)) {
        val msg = "Frame " + index + " does not exist. " +
          "Sequence size is " + _deltas.size
        throw new IllegalArgumentException(msg)
      }
      if (index == currentFrameIndex)
        _currentFrame
      else if (index == lastFrameIndex)
        _lastFrame
      else frameCache.getOrElse(index,
        frame(index - 1).applyDelta(_deltas(index)))
    }

    def append(mirrorables: Iterable[Mirrorable], plotActions: Seq[PlotAction]) {
      val (newMirroredState, mirroredUpdate) =
        Mirroring.diffs(lastFrame.mirroredState, mirrorables)
      val delta = Delta(mirroredUpdate, plotActions)
      _deltas :+= delta
      appendFrame(delta)
      _dirty = true
    }

  }

}
