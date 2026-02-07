// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.EventQueue
import java.io.IOException
import java.net.SocketException
import javax.swing.Timer

import org.nlogo.agent.OutputObject
import org.nlogo.api.{ Dump, IPCHandler, LabProtocol }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.headless.{ BehaviorSpaceCoordinator, HeadlessWorkspace }
import org.nlogo.nvm.{ DummyPrimaryWorkspace, LabInterface, Workspace }
import org.nlogo.swing.{ AppUtils, OptionPane, RichAction, WindowAutomator }
import org.nlogo.window.{ Events, ThreadUtils }

import ujson.Obj

// this is a simplified version of the normal GUI app that is used to run BehaviorSpace
// experiments in a separate process. it has all the necessary capabilities to run and
// display a model, but does not provide any user interfaces for interacting with the
// model or the NetLogo ecosystem. (Isaac B 2/4/26)

object BehaviorSpaceApp {
  def main(args: Array[String]): Unit = {
    if (args.size < 2)
      System.exit(1)

    val argsIter = args.drop(2).iterator

    val parsedArgs: CommandLineArgs = argsIter.foldLeft(CommandLineArgs(args(0), args(1))) {
      case (current, "--threads") if argsIter.hasNext =>
        current.copy(threads = argsIter.next.toInt)

      case (current, "--skip") if argsIter.hasNext =>
        current.copy(skip = argsIter.next.toInt)

      case (current, "--update-view") =>
        current.copy(updateView = true)

      case (current, "--update-plots") =>
        current.copy(updatePlots = true)

      case (current, "--mirror-headless") =>
        current.copy(mirrorHeadless = true)

      case (current, "--table") if argsIter.hasNext =>
        current.copy(table = Option(argsIter.next.trim))

      case (current, "--spreadsheet") if argsIter.hasNext =>
        current.copy(spreadsheet = Option(argsIter.next.trim))

      case (current, "--stats") if argsIter.hasNext =>
        current.copy(stats = Option(argsIter.next.trim))

      case (current, "--lists") if argsIter.hasNext =>
        current.copy(lists = Option(argsIter.next.trim))

      case (current, "--automated") =>
        WindowAutomator.setAutomated(true)

        current

      case (current, _) =>
        current
    }

    new BehaviorSpaceApp(parsedArgs).run()
  }

  case class CommandLineArgs(model: String, experiment: String, threads: Int = 1, skip: Int = 0,
                             updateView: Boolean = false, updatePlots: Boolean = false,
                             mirrorHeadless: Boolean = false, table: Option[String] = None,
                             spreadsheet: Option[String] = None, stats: Option[String] = None,
                             lists: Option[String] = None)
}

class BehaviorSpaceApp(args: BehaviorSpaceApp.CommandLineArgs) {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.behaviorSpace")

  private val frame = new BehaviorSpaceFrame(this)

  private val ipcHandler = IPCHandler(false)

  // lab.abort() should do its job pretty much immediately, so if it takes more than a few seconds,
  // something is definitely broken or frozen. this timer is a failsafe that forces an exit in this case
  // so the user doesn't have to manually quit the process. (Isaac B 2/6/26)
  private val timer = new Timer(5000, RichAction(_ => System.exit(0)))

  private val primaryWorkspace = new DummyPrimaryWorkspace {
    override def mirrorOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {
      val event = new Events.OutputEvent(false, oo, false, !toOutputArea, System.currentTimeMillis)

      if (EventQueue.isDispatchThread) {
        event.raise(workspace)
      } else {
        ThreadUtils.waitFor(workspace, new Runnable {
          override def run(): Unit = {
            event.raise(workspace)
          }
        })
      }
    }

    override def runtimeError(t: Throwable): Unit = {
      t match {
        case _: CompilerException =>
          displayError(s"${I18N.gui("error.compilation")}\n${t.getMessage}")

        case _: RuntimeException =>
          displayError(s"${I18N.gui("error.runtime")}\n${t.getMessage}")

        case _: IOException =>
          displayError(s"${I18N.gui("error.io")}\n${t.getMessage}")

        case _ =>
          displayError(s"${I18N.gui("error.general")}\n${Option(t.getMessage).getOrElse(t)}")
      }
    }
  }

  private val workspace: SemiHeadlessWorkspace = newWorkspace(args.updateView, args.updatePlots)

  private val lab = HeadlessWorkspace.newLab

  def run(): Unit = {
    AppUtils.setupGUI(None)

    val settings = LabInterface.Settings(args.model, Option(args.experiment), None, args.table, args.spreadsheet,
                                         args.stats, args.lists, None, args.threads, false, args.updatePlots,
                                         args.mirrorHeadless, args.skip)

    val protocol: LabProtocol = BehaviorSpaceCoordinator.selectProtocol(settings).get

    protocol.runsCompleted = args.skip

    val interfaceTab = new InterfaceTab(workspace)

    frame.addLinkComponent(workspace)

    workspace.getOpenModel.foreach { model =>
      EventQueue.invokeAndWait(() => {
        model.widgets.foreach(interfaceTab.loadWidget)

        frame.add(interfaceTab)
        frame.pack()

        interfaceTab.resetSplitPane()
        interfaceTab.syncTheme()

        Events.CompileAllEvent().raise(frame)
      })
    }

    frame.setVisible(true)

    ipcHandler.connect()

    new Thread {
      override def run(): Unit = {
        while (true) {
          // currently no actual messages need to be read, but this allows us to efficiently detect if the
          // connection to the main application was lost, in which case we abort so as to not leave any
          // dangling processes open. (Isaac B 2/6/26)
          ipcHandler.readLine().recover {
            case _: SocketException =>
              abort()
          }
        }
      }
    }.start()

    var progressDialog: ProgressDialog = null

    // the progress dialog needs to be created on the event thread or it freezes indefinitely
    // while trying to compute its initial size. maybe that's a bug, but there is no indication
    // of any other related problems, and this solution is simple and reliable. (Isaac B 2/6/26)
    EventQueue.invokeAndWait(() => {
      progressDialog = new ProgressDialog(this, workspace, lab, null, protocol)
    })

    var completed = Set[Int]()
    var highestCompleted = protocol.runsCompleted

    val worker = lab.newWorker(protocol)

    worker.addListener(new LabInterface.ProgressListener {
      override def experimentStarted(): Unit = {
        progressDialog.experimentStarted()
      }

      override def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]): Unit = {
        if (w == workspace) {
          progressDialog.runStarted(runNumber, settings.map { (name, value) =>
            (name, Dump.logoObject(value.asInstanceOf[AnyRef]))
          })
        }
      }

      override def stepCompleted(w: Workspace, steps: Int): Unit = {
        if (w == workspace)
          progressDialog.stepCompleted(steps)
      }

      override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
        if (w == workspace)
          progressDialog.measurementsTaken(values)
      }

      override def runCompleted(w: Workspace, runNumber: Int, step: Int): Unit = {
        completed += runNumber

        // we can't just do `highestCompleted = runNumber`, because we need to keep track of the
        // highest contiguous completed run number in order to ensure complete data after pausing.
        // without this logic, if a later parallel run finished before an earlier one, there could
        // be gaps in the output data after resuming. (Isaac B 2/6/26)
        while (completed.contains(highestCompleted + 1))
          highestCompleted += 1
      }

      override def experimentCompleted(): Unit = {
        progressDialog.writing()
      }

      override def runtimeError(w: Workspace, runNumber: Int, t: Throwable): Unit = {
        primaryWorkspace.runtimeError(t)
      }
    })

    var addPrimary = true

    lab.run(settings, worker, primaryWorkspace, () => {
      if (addPrimary) {
        addPrimary = false

        workspace
      } else {
        newWorkspace(false, false)
      }
    }) match {
      case LabInterface.Result.Aborted =>
        ipcHandler.writeLine(ujson.write(Obj(
          "type" -> "abort"
        )))

      case LabInterface.Result.Paused =>
        if (highestCompleted < protocol.countRuns) {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "pause",
            "update_view" -> workspace.getUpdateView,
            "update_plots" -> workspace.getUpdatePlotsAndMonitors,
            "completed" -> highestCompleted
          )))
        } else {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "complete"
          )))
        }

      case LabInterface.Result.Completed =>
        if (highestCompleted >= protocol.countRuns) {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "complete"
          )))
        } else {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "abort"
          )))
        }
    }

    timer.stop()
    ipcHandler.close()

    System.exit(0)
  }

  private def newWorkspace(updateView: Boolean, updatePlots: Boolean): SemiHeadlessWorkspace = {
    val workspace = SemiHeadlessWorkspace.create(frame, updateView, updatePlots)

    workspace.setPrimaryWorkspace(primaryWorkspace)
    workspace.setMirrorHeadlessOutput(args.mirrorHeadless)
    workspace.open(args.model, false)

    workspace
  }

  private def displayError(message: String): Unit = {
    new OptionPane(frame, I18N.gui("error.title"), message, OptionPane.Options.Ok, OptionPane.Icons.Error)
  }

  def getFrame: BehaviorSpaceFrame =
    frame

  def abort(): Unit = {
    timer.start()
    lab.abort()
  }
}
