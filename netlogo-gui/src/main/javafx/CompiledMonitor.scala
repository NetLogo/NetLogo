// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ core, internalapi, nvm },
    core.{ CompilerException, Monitor => CoreMonitor },
    internalapi.{ CompiledMonitor => ApiCompiledMonitor },
    nvm.Procedure

case class CompiledMonitor(
  val widget:         CoreMonitor,
  val compilerError:  Option[CompilerException],
  val procedureTag:   String,
  val procedure:      Procedure,
  val compiledSource: String,
  widgetActions:      WidgetActions)
  extends ApiCompiledMonitor
  with ReporterMonitorable {
    var updateCallback: (String => Unit) = { (s: String) => }

    def onUpdate(callback: String => Unit): Unit = {
      updateCallback = callback
    }

    def onError(callback: Exception => Unit): Unit = {
      // TODO: Fill this out
    }

    def defaultValue = "0"

    var currentValue = defaultValue

    def update(value: AnyRef): Unit = {
      value match {
        case s: String =>
          currentValue = s
          updateCallback(s)
        case other     => updateCallback(other.toString)
      }
    }

    override def modelLoaded(): Unit = {
      widgetActions.addMonitorable(this)
    }
}
