package org.nlogo.review

import scala.collection.mutable.Publisher

import org.nlogo.mirror.ModelRun

case class CurrentRunChangeEvent(
  val oldRun: Option[ModelRun],
  val newRun: Option[ModelRun])

trait HasCurrentRun extends Publisher[CurrentRunChangeEvent] {
  private var _currentRun: Option[ModelRun] = None
  override type Pub = Publisher[CurrentRunChangeEvent]

  def currentRun = _currentRun
  def currentRun_=(newRun: Option[ModelRun]) {
    if (_currentRun != newRun) {
      val oldRun = _currentRun
      _currentRun = newRun
      publish(CurrentRunChangeEvent(oldRun, newRun))
    }
  }
}

trait EnabledWithCurrentRun extends HasCurrentRun#Sub {
  val hasCurrentRun: HasCurrentRun
  def setEnabled(enabled: Boolean): Unit
  setEnabled(hasCurrentRun.currentRun.isDefined)
  hasCurrentRun.subscribe(this)
  override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    event match {
      case CurrentRunChangeEvent(_, newRun) =>
        setEnabled(newRun.isDefined)
      case _ =>
    }
  }
}