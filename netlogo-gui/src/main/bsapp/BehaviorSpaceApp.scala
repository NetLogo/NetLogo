// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.EventQueue
import java.io.IOException
import java.net.SocketException

import org.nlogo.agent.OutputObject
import org.nlogo.api.{ IPCHandler, LabProtocol, LogoException }
import org.nlogo.core.CompilerException
import org.nlogo.headless.{ BehaviorSpaceCoordinator, HeadlessWorkspace }
import org.nlogo.nvm.{ DummyPrimaryWorkspace, EngineException, LabInterface }
import org.nlogo.plot.PlotManager
import org.nlogo.swing.AppUtils
import org.nlogo.window.{ DefaultEditorFactory, Events, InterfacePanelLite }

import scala.util.Try

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
  private val frame = new BehaviorSpaceFrame(this)

  private val ipcHandler = IPCHandler(false)

  private val primaryWorkspace = new DummyPrimaryWorkspace {
    override def mirrorOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {
      ipcHandler.writeLine(ujson.write(Obj(
        "type" -> "mirror",
        "caption" -> oo.caption,
        "message" -> oo.message,
        "newline" -> oo.addNewline,
        "temporary" -> oo.isTemporary,
        "area" -> toOutputArea
      ))).recover {
        case _: SocketException =>
          exit()
      }
    }

    override def runtimeError(t: Throwable): Unit = {
      ipcHandler.writeLine(ujson.write(Obj(
        "type" -> "error",
        "exception" -> (t match {
          case _: CompilerException => "compiler"
          case _: LogoException | EngineException => "runtime"
          case _: IOException => "io"
          case _ => "other"
        }),
        "message" -> t.getMessage
      ))).recover {
        case _: SocketException =>
          exit()
      }
    }
  }

  private val workspace: SemiHeadlessWorkspace = newWorkspace(args.updateView)

  private val lab = HeadlessWorkspace.newLab

  def run(): Unit = {
    AppUtils.setupGUI(None)

    val settings = LabInterface.Settings(args.model, Option(args.experiment), None, args.table, args.spreadsheet,
                                         args.stats, args.lists, None, args.threads, false, args.updatePlots,
                                         args.mirrorHeadless, args.skip, Option(ipcHandler))

    val protocol: LabProtocol = BehaviorSpaceCoordinator.selectProtocol(settings).get

    val interfacePanel = new InterfacePanelLite(workspace.viewWidget, workspace, workspace,
                                                new PlotManager(workspace, workspace.world.mainRNG.clone),
                                                new DefaultEditorFactory(workspace), workspace.extensionManager)

    frame.addLinkComponent(workspace)

    workspace.getOpenModel.foreach { model =>
      EventQueue.invokeAndWait(() => {
        model.widgets.foreach(interfacePanel.loadWidget)

        frame.add(interfacePanel)
        frame.pack()

        Events.CompileAllEvent().raise(frame)
      })
    }

    frame.setVisible(true)

    ipcHandler.connect()

    new Thread {
      override def run(): Unit = {
        while (true) {
          ipcHandler.readLine().map(processMessage).recover {
            case _: SocketException =>
              exit()
          }
        }
      }
    }.start()

    var addPrimary = true

    lab.run(settings, lab.newWorker(protocol), primaryWorkspace, () => {
      if (addPrimary) {
        addPrimary = false

        workspace
      } else {
        newWorkspace(false)
      }
    })

    ipcHandler.close()

    System.exit(0)
  }

  private def newWorkspace(updateView: Boolean): SemiHeadlessWorkspace = {

    val workspace = SemiHeadlessWorkspace.create(frame, updateView)

    workspace.setPrimaryWorkspace(primaryWorkspace)
    workspace.open(args.model, false)

    workspace
  }

  private def processMessage(message: String): Unit = {
    Try {
      val json = ujson.read(message)

      json("type").str match {
        case "speed" =>
          workspace.updateManager.speed = json("value").num
          workspace.updateManager.nudgeSleeper()

        case "update_view" =>
          workspace.setUpdateView(json("value").bool)

        case "update_plots" =>
          workspace.setUpdatePlotsAndMonitors(json("value").bool)

        case _ =>
      }
    }
  }

  def exit(): Unit = {
    lab.abort()
  }
}
