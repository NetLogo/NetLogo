// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import org.nlogo.agent.{ AgentSet, CompilationManagement, World, World2D, World3D }
import org.nlogo.api.{ CommandRunnable, JobOwner, NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.compile.Compiler
import org.nlogo.core.{ AgentKind, Dialect, UpdateMode }
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.hubnet.server.HeadlessHubNetManagerFactory
import org.nlogo.render.Renderer
import org.nlogo.sdm.AggregateManagerLite
import org.nlogo.window.{ CompilerManager, Event, Events, JobWidget, PeriodicUpdater, ProceduresInterface, ThreadUtils,
                          UpdateManager }

// this extension of HeadlessWorkspace enables a model to be displayed
// in the interface without all the baggage carried by the GUIWorkspace
// class. in addition to being massive and annoying to instantiate, the
// GUIWorkspace class creates and manages various interfaces for user
// interaction that shouldn't exist when running BehaviorSpace. (Isaac B 2/4/26)

object SemiHeadlessWorkspace {
  def create(frame: BehaviorSpaceFrame, updateView: Boolean): SemiHeadlessWorkspace = {
    val world: World & CompilationManagement = {
      if (Version.is3D) {
        new World3D
      } else {
        new World2D
      }
    }

    val dialect: Dialect = {
      if (Version.is3D) {
        NetLogoThreeDDialect
      } else {
        NetLogoLegacyDialect
      }
    }

    new SemiHeadlessWorkspace(frame, world, dialect, updateView)
  }
}

class SemiHeadlessWorkspace(frame: BehaviorSpaceFrame, world: World & CompilationManagement, dialect: Dialect,
                            private var updateView: Boolean)
  extends HeadlessWorkspace(world, new Compiler(dialect), new Renderer(world), new AggregateManagerLite,
                            new HeadlessHubNetManagerFactory)
    with ProceduresInterface with Event.LinkParent with Event.LinkChild with Events.AddJobEvent.Handler with Events.RemoveJobEvent.Handler
    with Events.JobStoppingEvent.Handler with Events.RemoveAllJobsEvent.Handler {

  val viewWidget = new ViewWidget(this)

  val updateManager = new UpdateManager {
    override def defaultFrameRate: Double =
      frameRate

    override def ticks: Double =
      world.tickCounter.ticks

    override def updateMode: UpdateMode =
      SemiHeadlessWorkspace.this.updateMode
  }

  private var _updateMode: UpdateMode = UpdateMode.Continuous

  private val periodicUpdater = new PeriodicUpdater(jobManager) {
    createActionListener()
  }

  private val lifeguard = new Thread {
    override def run(): Unit = {
      try {
        while (true) {
          if (jobManager.anyPrimaryJobs())
            world.comeUpForAir = true

          Thread.sleep(10)
        }
      } catch {
        case _: InterruptedException =>
      }
    }
  }

  private var updatePlotsAndMonitors = true

  set3d(Version.is3D)

  frame.addLinkComponent(new CompilerManager(this, world, this))

  periodicUpdater.start()
  lifeguard.start()

  def setUpdateView(updateView: Boolean): Unit = {
    this.updateView = updateView

    if (!updateView)
      viewWidget.reset()
  }

  def setUpdatePlotsAndMonitors(updatePlotsAndMonitors: Boolean): Unit = {
    this.updatePlotsAndMonitors = updatePlotsAndMonitors
  }

  override def updateMode: UpdateMode =
    _updateMode

  override def updateMode(updateMode: UpdateMode): Unit = {
    _updateMode = updateMode
  }

  override def periodicUpdate(): Unit = {
    if (updatePlotsAndMonitors) {
      ThreadUtils.waitFor(this, new CommandRunnable {
        override def run(): Unit = {
          new Events.PeriodicUpdateEvent().raise(SemiHeadlessWorkspace.this)
        }
      })
    }
  }

  override def requestDisplayUpdate(force: Boolean): Unit = {
    super.requestDisplayUpdate(force)

    if (updateView) {
      if (force)
        updateManager.pseudoTick()

      updateDisplay(true)
    }
  }

  override def updateDisplay(worldLock: Boolean): Unit = {
    if (updateView && world.displayOn && updateManager.shouldUpdateNow) {
      updateManager.beginPainting()

      if (worldLock) {
        ThreadUtils.waitFor(this, new CommandRunnable {
          override def run(): Unit = {
            viewWidget.paintBuffer()
          }
        })

        while (!updateManager.isDoneSmoothing())
          ThreadUtils.waitForQueuedEvents(this)
      } else {
        world synchronized {
          viewWidget.paintBuffer()
        }
      }

      viewWidget.repaint()

      updateManager.donePainting()
      updateManager.pause()
    }
  }

  override def breathe(): Unit = {
    jobManager.maybeRunSecondaryJobs()

    if (updateView && updateMode == UpdateMode.Continuous) {
      updateManager.pseudoTick()
      updateDisplay(true)
    }

    world.comeUpForAir = updateManager.shouldComeUpForAirAgain
  }

  override def ownerFinished(owner: JobOwner): Unit = {
    new Events.JobRemovedEvent(owner).raiseLater(this)

    if (owner.ownsPrimaryJobs) {
      updateManager.reset()
      updateDisplay(false)
    }
  }

  override def handle(e: Events.AddJobEvent): Unit = {
    val owner: JobOwner = e.owner

    val agents: AgentSet = owner match {
      case w: JobWidget if e.agents == null && w.useAgentClass =>
        world.agentSetOfKind(w.kind)

      case _ =>
        e.agents
    }

    if (owner.ownsPrimaryJobs) {
      if (e.procedure != null) {
        jobManager.addJob(owner, agents, this, e.procedure)
      } else {
        new Events.JobRemovedEvent(owner).raiseLater(this)
      }
    } else {
      jobManager.addSecondaryJob(owner, agents, this, e.procedure)
    }
  }

  override def handle(e: Events.RemoveJobEvent): Unit = {
    if (e.owner.ownsPrimaryJobs) {
      jobManager.finishJobs(e.owner)
    } else {
      jobManager.finishSecondaryJobs(e.owner)
    }
  }

  override def handle(e: Events.JobStoppingEvent): Unit = {
    jobManager.stoppingJobs(e.owner)
  }

  override def handle(e: Events.RemoveAllJobsEvent): Unit = {
    jobManager.haltSecondary()
    jobManager.haltPrimary()
  }

  override def getLinkParent: AnyRef =
    frame

  override def getLinkChildren: Array[AnyRef] =
    Array()

  override def classDisplayName: String =
    "Code"

  override def headerSource: String =
    ""

  override def innerSource: String =
    getOpenModel.fold("")(_.code)

  override def innerSource_=(s: String): Unit = {}

  override def source: String =
    headerSource + innerSource

  override def kind: AgentKind =
    AgentKind.Observer

  override def dispose(): Unit = {
    periodicUpdater.stop()

    lifeguard.interrupt()
    lifeguard.join()

    super.dispose()
  }
}
