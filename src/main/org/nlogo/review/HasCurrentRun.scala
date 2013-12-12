// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun
import org.nlogo.util.SimplePublisher

case class BeforeRunChangeEvent(
  val oldRun: Option[ModelRun],
  val newRun: Option[ModelRun],
  val requestHalt: Boolean)

case class AfterRunChangeEvent(
  val oldRun: Option[ModelRun],
  val newRun: Option[ModelRun])

trait HasCurrentRun {
  private var _currentRun: Option[ModelRun] = None
  val beforeRunChangePub = new SimplePublisher[BeforeRunChangeEvent]
  val afterRunChangePub = new SimplePublisher[AfterRunChangeEvent]

  def currentRun = _currentRun
  def setCurrentRun(newRun: Option[ModelRun], requestHalt: Boolean) {
    if (_currentRun != newRun) {
      val oldRun = _currentRun
      beforeRunChangePub.publish(BeforeRunChangeEvent(oldRun, newRun, requestHalt))
      _currentRun = newRun
      afterRunChangePub.publish(AfterRunChangeEvent(oldRun, newRun))
    }
  }
}

trait EnabledWithCurrentRun {
  val hasCurrentRun: HasCurrentRun
  def setEnabled(enabled: Boolean): Unit
  setEnabled(hasCurrentRun.currentRun.isDefined)
  hasCurrentRun.afterRunChangePub.newSubscriber { event =>
    setEnabled(event.newRun.isDefined)
  }
}