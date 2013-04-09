package org.nlogo.review

import scala.collection.mutable.Publisher

import org.nlogo.mirror.ModelRun

sealed trait CurrentRunChangeEvent {
  val oldRun: Option[ModelRun]
  val newRun: Option[ModelRun]
}

case class BeforeCurrentRunChangeEvent(
  val oldRun: Option[ModelRun],
  val newRun: Option[ModelRun])
  extends CurrentRunChangeEvent

case class AfterCurrentRunChangeEvent(
  val oldRun: Option[ModelRun],
  val newRun: Option[ModelRun])
  extends CurrentRunChangeEvent

trait HasCurrentRun extends Publisher[CurrentRunChangeEvent] {
  private var _currentRun: Option[ModelRun] = None
  override type Pub = Publisher[CurrentRunChangeEvent]

  def currentRun = _currentRun
  def currentRun_=(newRun: Option[ModelRun]) {
    if (_currentRun != newRun) {
      val oldRun = _currentRun
      publish(BeforeCurrentRunChangeEvent(oldRun, newRun))
      _currentRun = newRun
      publish(AfterCurrentRunChangeEvent(oldRun, newRun))
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
      case AfterCurrentRunChangeEvent(_, newRun) =>
        setEnabled(newRun.isDefined)
      case _ =>
    }
  }
}