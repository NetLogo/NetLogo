// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun

import javax.swing.AbstractListModel

class ReviewTabState(
  private var _runs: Vector[ModelRun] = Vector.empty)
  extends AbstractListModel
  with HasCurrentRun
  with RecordingToggling {

  // ListModel methods:
  override def getElementAt(index: Int): AnyRef = _runs(index)
  override def getSize = _runs.size

  def runs = _runs

  def currentFrame = currentRun.flatMap(_.currentFrame)
  def currentFrameIndex = currentRun.flatMap(_.currentFrameIndex)
  def currentTicks = currentFrame.flatMap(_.ticks)
  def currentlyRecording =
    recordingEnabled && currentRun.map(_.stillRecording).getOrElse(false)

  def reset() {
    val lastIndex = _runs.size - 1
    _runs = Vector[ModelRun]()
    currentRun = None
    fireIntervalRemoved(this, 0, lastIndex)
  }

  def closeCurrentRun() {
    for (run <- currentRun) {
      val index = _runs.indexOf(run)
      _runs = _runs.filterNot(_ == run)
      fireIntervalRemoved(this, index, index)
      val sameString = (_: ModelRun).modelString == run.modelString
      currentRun = _runs
        .lift(index) // keep same index if possible
        .filter(sameString)
        .orElse(_runs.filter(sameString).lastOption) // or use last (or None if empty)
    }
  }

  def addRun(run: ModelRun) = {
    _runs :+= run
    val lastIndex = _runs.size - 1
    fireIntervalAdded(this, lastIndex, lastIndex)
    currentRun = Some(run)
    run
  }

  beforeRunChangePub.newSubscriber {
    _.oldRun.foreach(_.stillRecording = false)
  }

}
