// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun

import javax.swing.AbstractListModel

class ReviewTabState(
  private var _runs: IndexedSeq[ModelRun] = IndexedSeq.empty,
  private var _recordingEnabled: Boolean = false)
  extends AbstractListModel
  with HasCurrentRun
  with HasCurrentRun#Sub {

  subscribe(this) // subscribe to our own CurrentRunChangeEvents

  // ListModel methods:
  override def getElementAt(index: Int): AnyRef = _runs(index)
  override def getSize = _runs.size

  def runs = _runs

  def currentFrame = currentRun.flatMap(_.currentFrame)
  def currentFrameIndex = currentRun.flatMap(_.currentFrameIndex)
  def currentTicks = currentFrame.flatMap(_.ticks)
  def recordingEnabled = _recordingEnabled
  def recordingEnabled_=(b: Boolean) { _recordingEnabled = b }
  def currentlyRecording = _recordingEnabled && currentRun.map(_.stillRecording).getOrElse(false)

  def reset() {
    val lastIndex = _runs.size - 1
    _runs = IndexedSeq[ModelRun]()
    currentRun = None
    fireIntervalRemoved(this, 0, lastIndex)
  }

  def closeCurrentRun() {
    for (run <- currentRun) {
      val index = _runs.indexOf(run)
      _runs = _runs.filterNot(_ == run)
      fireIntervalRemoved(this, index, index)
      currentRun = _runs
        .lift(index) // keep same index if possible
        .orElse(_runs.lastOption) // or use last (or None if empty)
    }
  }

  def addRun(run: ModelRun) = {
    _runs :+= run
    val lastIndex = _runs.size - 1
    fireIntervalAdded(this, lastIndex, lastIndex)
    currentRun = Some(run)
    run
  }

  override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    event match {
      case BeforeCurrentRunChangeEvent(Some(oldRun), _) =>
        oldRun.stillRecording = false
      case _ =>
    }
  }
}
