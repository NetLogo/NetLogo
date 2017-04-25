// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ core, internalapi, nvm },
    core.{ Button => CoreButton, CompilerException },
    internalapi.{ CompiledButton => ApiCompiledButton, ModelUpdate, Monitorable,
      TicksCleared, TicksStarted },
    nvm.Procedure

case class CompiledButton(
  val widget:        CoreButton,
  val compilerError: Option[CompilerException],
  val procedureTag:  String,
  val procedure:     Procedure,
  widgetActions:     WidgetActions)
  extends ApiCompiledButton {
    var taskTag: Option[String] = None
    val isRunning    = new JobRunningMonitorable
    val ticksEnabled = new TicksStartedMonitorable
    def start(interval: Long = 0): Unit = {
      widgetActions.run(this, interval)
    }
    def stop(): Unit = {
      widgetActions.stop(this)
    }

    def errored(e: Exception): Unit =
      isRunning.errorCallback(e)

    override def modelLoaded(): Unit = {
      widgetActions.addMonitorable(ticksEnabled)
    }

    override def modelUnloaded(): Unit = {
    }
  }

class TicksStartedMonitorable extends Monitorable[Boolean] with StaticMonitorable {
  def defaultValue = false
  var currentValue = defaultValue

  var updateCallback: (Boolean => Unit) = { (a: Boolean) => }

  def tags = Seq(TicksStarted.tag, TicksCleared.tag)

  def notifyUpdate(update: ModelUpdate): Unit = {
    update match {
      case TicksStarted => set(true)
      case TicksCleared => set(false)
      case _ =>
    }
  }

  def onUpdate(callback: Boolean => Unit): Unit = {
    updateCallback = callback
  }

  /* this monitorable cannot error */
  def onError(callback: Exception => Unit): Unit = {}

  def set(b: Boolean): Unit = {
    currentValue = b
    updateCallback(b)
  }
}

class JobRunningMonitorable extends Monitorable[Boolean] {
  def defaultValue = false
  var currentValue = defaultValue

  var updateCallback: (Boolean => Unit) = { (a: Boolean) => }
  var errorCallback: (Exception => Unit) = { (e: Exception) => }

  def onUpdate(callback: Boolean => Unit): Unit = {
    updateCallback = callback
  }

  def onError(callback: Exception => Unit): Unit = {
    errorCallback = callback
  }

  def set(b: Boolean): Unit = {
    currentValue = b
    updateCallback(b)
  }
}
