// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun
import org.nlogo.window.GUIWorkspace

import javax.swing.AbstractListModel

class ReviewTabState(
  val ws: GUIWorkspace,
  private var _runs: Vector[ModelRun] = Vector.empty)
  extends AbstractListModel[ModelRun]
  with HasCurrentRun
  with RecordingToggling {

  // ListModel methods:
  override def getElementAt(index: Int): ModelRun = _runs(index)
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
    setCurrentRun(None, true)
    fireIntervalRemoved(this, 0, lastIndex)
  }

  def closeCurrentRun() {
    for (run <- currentRun) {

      val runsOfSameModel = _runs.filter(r => r.modelString == run.modelString)

      val (candidates, targetIndex) = {
        val rs = Option(runsOfSameModel)
          .filter(_.size > 1)
          .getOrElse(_runs)
        (rs.filterNot(_ == run), rs.indexOf(run))
      }

      val newCurrentRun =
        candidates
          .filterNot(_ == run)
          .lift(targetIndex)
          .orElse(candidates.lastOption)

      val index = _runs.indexOf(run)
      _runs = _runs.filterNot(_ == run)
      fireIntervalRemoved(this, index, index)
      setCurrentRun(newCurrentRun, true)
    }
  }

  def addRun(run: ModelRun) = {
    _runs :+= run
    val lastIndex = _runs.size - 1
    fireIntervalAdded(this, lastIndex, lastIndex)
    setCurrentRun(Some(run), false)
    run
  }

  beforeRunChangePub.newSubscriber { event =>
    event.oldRun.foreach(_.stillRecording = false)
    if (event.requestHalt && ws.jobManager.anyPrimaryJobs)
      ws.halt() // if requested, halt the running model
  }

}
