// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import javax.swing.AbstractListModel
import org.nlogo.mirror.ModelRun

class ReviewTabState(
  private var _runs: Seq[ModelRun] = Seq(),
  private var _currentRun: Option[ModelRun] = None,
  private var _recordingEnabled: Boolean = false)
  extends AbstractListModel {

  // ListModel methods:
  override def getElementAt(index: Int): AnyRef = _runs(index)
  override def getSize = _runs.size

  def runs = _runs

  def currentRun = _currentRun
  def currentRunData = currentRun.flatMap(_.data)
  def currentFrame = currentRun.flatMap(_.currentFrame)
  def currentFrameIndex = currentRun.map(_.currentFrameIndex).getOrElse(0)
  def recordingEnabled = _recordingEnabled
  def recordingEnabled_=(b: Boolean) { _recordingEnabled = b }
  def currentlyRecording = _recordingEnabled && currentRun.map(_.stillRecording).getOrElse(false)

  def reset() {
    val lastIndex = _runs.size - 1
    _runs = Seq[ModelRun]()
    _currentRun = None
    fireIntervalRemoved(this, 0, lastIndex)
  }

  def closeCurrentRun() {
    for (run <- currentRun) {
      val index = _runs.indexOf(run)
      _runs = _runs.filterNot(_ == run)
      fireIntervalRemoved(this, index, index)
    }
  }

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
}

