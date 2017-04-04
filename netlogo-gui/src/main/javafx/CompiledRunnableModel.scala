// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import org.nlogo.internalapi.{
  CompiledModel, CompiledWidget, CompiledButton => ApiCompiledButton,
  CompiledMonitor => ApiCompiledMonitor,
  EmptyRunnableModel, AddProcedureRun, ModelAction, ModelUpdate, MonitorsUpdate,
  NonCompiledWidget, RunnableModel, RunComponent, SchedulerWorkspace, StopProcedure, UpdateInterfaceGlobal }

import org.nlogo.core.{ AgentKind, Button => CoreButton, Chooser => CoreChooser,
  CompilerException, InputBox => CoreInputBox, Model, Monitor => CoreMonitor, NumericInput, Program,
  Slider => CoreSlider, StringInput, Switch => CoreSwitch, Widget }
import org.nlogo.nvm.{ CompilerResults, ConcurrentJob, ExclusiveJob, Procedure, SuspendableJob }
import org.nlogo.workspace.AbstractWorkspace

import scala.util.{ Failure, Success }

class CompiledRunnableModel(workspace: AbstractWorkspace with SchedulerWorkspace, compiledWidgets: Seq[CompiledWidget]) extends RunnableModel  {
  import workspace.scheduledJobThread

  val componentMap = Map.empty[String, RunComponent]

  override def submitAction(action: ModelAction): Unit = {
    scheduleAction(action, None)
  }

  override def submitAction(action: ModelAction, component: RunComponent): Unit = {
    scheduleAction(action, Some(component))
  }

  private var taggedComponents = Map.empty[String, RunComponent]

  val monitorRegistry: Map[String, CompiledMonitor] =
    compiledWidgets.collect {
      case cm: CompiledMonitor => cm.procedureTag -> cm
    }.toMap

  def modelLoaded(): Unit = {
    monitorRegistry.values.foreach {
      case cm: CompiledMonitor =>
        val job =
          new SuspendableJob(workspace.world.observers, false, cm.procedure, 0, null, workspace.world.mainRNG)
        scheduledJobThread.registerMonitor(cm.procedureTag, job)
    }
  }

  def modelUnloaded(): Unit = {
    // TODO: unload monitors, remove existing jobs here
  }

  private def registerTag(componentOpt: Option[RunComponent], action: ModelAction, tag: String): Unit = {
    componentOpt.foreach { component =>
      taggedComponents = taggedComponents + (tag -> component)
      component.tagAction(action, tag)
    }
  }

  private def scheduleAction(action: ModelAction, componentOpt: Option[RunComponent]): Unit = {
    action match {
      case UpdateInterfaceGlobal(name, value) =>
        val tag = scheduledJobThread.scheduleOperation( { () =>
          workspace.world.setObserverVariableByName(name, value.get)
        })
        registerTag(componentOpt, action, tag)
      case AddProcedureRun(widgetTag, isForever, interval) =>
        // TODO: this doesn't take isForever into account yet
        val p = findWidgetProcedure(widgetTag)
        findWidgetProcedure(widgetTag).foreach { procedure =>
          val job =
            new SuspendableJob(workspace.world.observers, isForever, procedure, 0, null, workspace.world.mainRNG)
          val tag = scheduledJobThread.scheduleJob(job, interval)
          registerTag(componentOpt, action, tag)
        }
      case StopProcedure(jobTag) => scheduledJobThread.stopJob(jobTag)
    }
  }

  def findWidgetProcedure(tag: String): Option[Procedure] = {
    compiledWidgets.collect {
      case c@CompiledButton(_, _, t, procedure) if t == tag && procedure != null => procedure
    }.headOption
  }

  def notifyUpdate(update: ModelUpdate): Unit = {
    update match {
      case MonitorsUpdate(values, time) =>
        values.foreach {
          case (k, Success(v)) =>
            monitorRegistry.get(k).foreach(_.update(v))
          case (k, Failure(v)) =>
            println(s"failure for monitor ${monitorRegistry(k)}: $v")
            v.printStackTrace()
            // println(monitorRegistry.get(k).map(_.procedure.dump))
        }
      case other =>
        taggedComponents.get(update.tag).foreach(_.updateReceived(update))
        taggedComponents -= update.tag
    }
  }
}

case class CompiledButton(val widget: CoreButton, val compilerError: Option[CompilerException], val procedureTag: String, val procedure: Procedure)
  extends ApiCompiledButton

case class CompiledMonitor(val widget: CoreMonitor, val compilerError: Option[CompilerException], val procedureTag: String, val procedure: Procedure, val compiledSource: String)
  extends ApiCompiledMonitor {
    var updateCallback: (String => Unit) = { (s: String) => }

    def onUpdate(callback: String => Unit): Unit = {
      updateCallback = callback
    }

    def update(value: AnyRef): Unit = {
      value match {
        case s: String => updateCallback(s)
        case other     => updateCallback(other.toString)
      }
    }
}
