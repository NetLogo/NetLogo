package org.nlogo.app

import org.nlogo.plot.PlotAction

import javax.swing.AbstractListModel

class ReviewTabState(
  var widgetHooks: Seq[WidgetHook] = Seq(),
  private var _runs: Seq[ModelRun] = Seq[ModelRun](),
  private var _currentRun: Option[ModelRun] = None,
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
    _runs = Seq[ModelRun]()
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

  def uniqueName(name: String) =
    (name +: Stream.from(1).map(i => name + " (" + i + ")"))
      .find(str => _runs.forall(_.name != str))
      .get

  def addRun(run: ModelRun) = {
    _runs :+= run
    setCurrentRun(run)
    val lastIndex = _runs.size - 1
    fireIntervalAdded(this, lastIndex, lastIndex)
    run
  }

  def setCurrentRun(run: ModelRun) {
    for {
      previousRun <- _currentRun
      if previousRun != run
    } previousRun.stillRecording = false
    _currentRun = Some(run)
  }

  def undirty(run: ModelRun) {
    val index = _runs.indexOf(run)
    if (index != -1) {
      run.dirty = false
      fireContentsChanged(this, index, index)
    }
  }

  def dirty = runs.exists(_.dirty)
  def sizeInBytes: Long = _runs.map(_.sizeInBytes).sum
}

