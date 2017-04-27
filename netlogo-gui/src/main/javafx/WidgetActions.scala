// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ api, internalapi, nvm },
    internalapi.{ CompiledWidget, InterfaceControl, JobErrored, JobFinished, JobScheduler,
      ModelOperation, ModelUpdate, MonitorsUpdate, UpdateFailure, UpdateVariable, UpdateSuccess },
    nvm.{ SuspendableJob, Workspace }

import
  scala.collection.mutable.WeakHashMap

// NOTE: This class isn't specific to javafx, but seemed like the most convenient place to put it.
// The functionality of this package might belong more properly in "workspace" as it is currently understood.
// I'm hesitant to include it in workspace for several reasons:
//
// 1) It isn't fundamentally a concern of NetLogo code in general as it is primarily about operation management
//    and scheduling (which NetLogo operations cannot do).
// 2) Workspace already has enough cruft growing on it and anything which gets put into org.nlogo.workspace seems
//    like it may eventually end up added to Workspace.
class WidgetActions(workspace: Workspace, scheduler: JobScheduler) extends InterfaceControl {
  val widgetsByJobTag    = WeakHashMap.empty[String, CompiledWidget]
  val monitorsByTag      = WeakHashMap.empty[String, ReporterMonitorable]
  val staticMonitorables = WeakHashMap.empty[String, Seq[StaticMonitorable]]
  val pendingUpdates     = WeakHashMap.empty[UpdateVariable, ReporterMonitorable]

  def run(button: CompiledButton, interval: Long): Unit = {
    if (button.taskTag.isEmpty) {
      val job =
        new SuspendableJob(workspace.world.observers, button.widget.forever, button.procedure, 0, null, workspace.world.mainRNG)
      val task = scheduler.createJob(job, interval)
      button.taskTag = Some(task.tag)
      widgetsByJobTag(task.tag) = button
      button.isRunning.set(true)
      scheduler.queueTask(task)
    } else {
      throw new IllegalStateException("This job is already running")
    }
  }

  def stop(button: CompiledButton): Unit = {
    if (button.taskTag.nonEmpty) {
      for {
        tag <- button.taskTag
      } scheduler.stopJob(tag)
    } else {
      throw new IllegalStateException("This job is not running")
    }
  }

  def runOperation(op: ModelOperation, m: ReporterMonitorable): Unit = {
    val task = scheduler.createOperation(op)
    op match {
      case uv: UpdateVariable => pendingUpdates(uv) = m
      case _ =>
    }
    scheduler.queueTask(task)
  }

  def notifyUpdate(update: ModelUpdate): Unit = {
    def stopButton(c: CompiledButton): Unit = {
      c.taskTag = None
      c.isRunning.set(false)
    }
    update match {
      case JobErrored(tag, error) =>
        widgetsByJobTag.get(tag) match {
          case Some(c: CompiledButton) =>
            stopButton(c)
            c.errored(error)
          case _ =>
        }
      case JobFinished(tag) =>
        widgetsByJobTag.get(tag) match {
          case Some(c: CompiledButton) =>
            stopButton(c)
          case _ =>
        }
      case MonitorsUpdate(values, time) =>
        values.foreach {
          case (key, result) =>
            monitorsByTag.get(key).foreach { monitor =>
              monitor.update(result)
            }
        }
      case UpdateSuccess(uv) =>
        for {
          monitorable <- pendingUpdates.get(uv)
        } {
          monitorable.update(uv.updateValue)
          pendingUpdates -= uv
        }
      case UpdateFailure(uv, actual) =>
        for {
          monitorable <- pendingUpdates.get(uv)
        } {
          monitorable.update(actual)
          pendingUpdates -= uv
        }
      case other =>
        staticMonitorables.getOrElse(other.tag, Seq()).foreach { m =>
          m.notifyUpdate(other)
        }
    }
    widgetsByJobTag -= update.tag
  }

  def addMonitorable(s: StaticMonitorable): Unit = {
    s.tags.foreach { t =>
      val existingMonitorables = staticMonitorables.getOrElse(t, Seq())
      staticMonitorables(t) = s +: existingMonitorables
    }
  }

  def addMonitorable(r: ReporterMonitorable): Unit = {
    val job =
      new SuspendableJob(
        workspace.world.observers, false, r.procedure, 0, null, workspace.world.mainRNG)
    monitorsByTag += (r.procedureTag -> r)
    scheduler.registerMonitor(r.procedureTag, job)
  }

  def clearAll(): Unit = {
    scheduler.clearJobsAndMonitors()
    widgetsByJobTag.clear()
    monitorsByTag.clear()
    staticMonitorables.clear()
    pendingUpdates.clear()
  }
}
