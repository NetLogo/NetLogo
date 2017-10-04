// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame

import org.nlogo.nvm.JobManagerOwner
import org.nlogo.api.{ CommandRunnable, Exceptions, JobOwner, LogoException }
import org.nlogo.nvm.{ Context, HaltException, Instruction, JobManagerInterface }
import org.nlogo.awt.EventQueue
import org.nlogo.swing.ModalProgressTask
import Events.JobRemovedEvent

// The behavior of this class used to be part of GUIWorkspace.
// It was separated to allow for instantiating the JobManager before instantiating the Workspace.
class GUIJobManagerOwner(
  val updateManager: UpdateManagerInterface,
  viewManager:       ViewManager,
  displayStatusRef:  GUIWorkspaceScala.DisplayStatusRef,
  world:             AnyRef,
  frame:             Frame)
  extends JobManagerOwner
  with Event.LinkChild
  {

  def getLinkParent: AnyRef = frame

  val updateRunner: Runnable =
    new Runnable() {
      def run(): Unit = {
        new Events.PeriodicUpdateEvent().raise(GUIJobManagerOwner.this)
      }
    }

  private var _periodicUpdatesEnabled = false;
  def periodicUpdatesEnabled = _periodicUpdatesEnabled
  def setPeriodicUpdatesEnabled(enabled: Boolean): Unit = {
    _periodicUpdatesEnabled = enabled
  }

  def ownerFinished(owner: JobOwner): Unit = {
    new JobRemovedEvent(owner).raiseLater(this)
    if (owner.ownsPrimaryJobs) {
      updateManager.reset()
      updateDisplay(false, false)
    }
  }

  def periodicUpdate(): Unit = {
    if (periodicUpdatesEnabled) {
      ThreadUtils.waitFor(world, updateRunner)
    }
  }

  def runtimeError(
    owner:       JobOwner,
    manager:     JobManagerInterface,
    context:     Context,
    instruction: Instruction,
    ex:          Exception): Unit = {
    // this method is called from the job thread, so we need to switch over
    // to the event thread.  but in the error dialog we want to be able to
    // show the original thread in which it happened, so we hang on to the
    // current thread before switching - ST 7/30/04
    val thread = Thread.currentThread()
    EventQueue.invokeLater(
      new Runnable() {
        def run(): Unit = {
          runtimeErrorPrivate(owner, manager, context, instruction, thread, ex)
        }
      })
  }

  private def halt(manager: JobManagerInterface): Unit = {
    manager.interrupt()
    ModalProgressTask.onUIThread(frame, "Halting...",
      new Runnable() {
        def run(): Unit = {
          manager.haltPrimary()
          displayStatusRef.updateAndGet(s => s.codeSet(true))
          viewManager.dirty()
          viewManager.repaint()
      }})
  }

  private def runtimeErrorPrivate(
    owner:       JobOwner,
    manager:     JobManagerInterface,
    context:     Context,
    instruction: Instruction,
    thread:      Thread,
    ex:          Exception): Unit = {
      // halt, or at least turn graphics back on if they were off
      if (ex.isInstanceOf[HaltException] && ex.asInstanceOf[HaltException].haltAll) {
        halt(manager) // includes turning graphics back on
      } else if (! owner.isInstanceOf[MonitorWidget]) {
        displayStatusRef.updateAndGet(s => s.codeSet(true))
      }
      // tell the world!
      if (! ex.isInstanceOf[HaltException]) {
        // check to see if the error occurred inside a "run" or "runresult" instruction;
        // if so, report the error as having occurred there - ST 5/7/03
        val posAndLength =
          Option(instruction.token)
            .map(_ => instruction.getPositionAndLength)
            .getOrElse(Array[Int](-1, 0))
        val sourceOwner = context.activation.procedure.owner
        new org.nlogo.window.Events.RuntimeErrorEvent(owner, sourceOwner, posAndLength(0), posAndLength(1))
          .raiseLater(this)
      }
      // MonitorWidgets always immediately restart their jobs when a runtime error occurs,
      // but we don't want to just stream errors to the command center, so let's not print
      // anything to the command center, and assume that someday MonitorWidgets will do
      // their own user notification - ST 12/16/01
      if ( ! owner.isInstanceOf[MonitorWidget] || ex.isInstanceOf[HaltException]) {
        // It doesn't seem like we should need to use invokeLater() here, because
        // we're already on the event thread.  But without using it, at least on
        // Mac 142U1DP3 (and maybe other Mac VMs, and maybe other platforms too,
        // I don't know), the error dialog didn't wind up with the keyboard focus
        // if the Code tab came forward... probably because something that
        // the call to select() in ProceduresTab was doing was doing invokeLater()
        // itself?  who knows... in any case, this seems to fix it - ST 7/30/04
        EventQueue.invokeLater(new Runnable() {
          def run(): Unit = {
            RuntimeErrorDialog.show(context, instruction, thread, ex)
          }
        })
      }
  }

  @throws(classOf[LogoException])
  private def waitFor(runnable: CommandRunnable): Unit = {
    ThreadUtils.waitFor(world, runnable)
  }

  // this is called *only* from job thread - ST 8/20/03, 1/15/04
  def updateDisplay(haveWorldLockAlready: Boolean, force: Boolean): Unit = {
    viewManager.dirty()
    val displayStatus = displayStatusRef.get
    val displayIsRendering = displayStatus.shouldRender(force)
    val shouldUpdate = updateManager.shouldUpdateNow
    if (shouldUpdate && displayIsRendering) {
      if (haveWorldLockAlready) {
        try {
          waitFor(new CommandRunnable() {
            def run(): Unit = {
              viewManager.incrementalUpdateFromEventThread()
            }
          })
          // don't block the event thread during a smoothing pause
          // or the UI will go sluggish (issue #1263) - ST 9/21/11
          while(! updateManager.isDoneSmoothing()) {
            ThreadUtils.waitForQueuedEvents(world)
          }
        } catch {
          case ex: HaltException => Exceptions.ignore(ex)
          case ex: LogoException => throw new IllegalStateException(ex)
        }
      } else {
        viewManager.incrementalUpdateFromJobThread()
      }
      updateManager.pause()
    } else if (! shouldUpdate) {
      if (displayStatus.trackSkippedFrames) {
        viewManager.framesSkipped()
      }
    }
  }
}
