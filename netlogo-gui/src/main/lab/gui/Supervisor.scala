// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window
import java.io.File
import java.net.SocketException
import java.nio.file.Path
import java.util.Base64

import org.nlogo.api.{ ActionBuffer, CompilerServices, Exceptions, ExportPlotWarningAction, IPCHandler, LabProtocol }
import org.nlogo.agent.OutputObject
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.I18N
import org.nlogo.drawing.{ DrawingActionBroker, DrawingActionRunner }
import org.nlogo.editor.Colorizer
import org.nlogo.mirror.{ FakeWorld, Mirroring, Serializer }
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ EditDialogFactory, GUIWorkspace }
import org.nlogo.workspace.WorkspaceFactory

import scala.collection.mutable.Set
import scala.sys.process.Process
import scala.util.Try

import ujson.Obj

class Supervisor(
  parent: Window,
  val workspace: GUIWorkspace,
  modelPath: Path,
  val protocol: LabProtocol,
  factory: WorkspaceFactory,
  dialogFactory: EditDialogFactory,
  compiler: CompilerServices,
  colorizer: Colorizer,
  saveProtocol: (LabProtocol, Int) => Unit,
  automated: Boolean
) extends Thread("BehaviorSpace Supervisor") {

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace")

  val completed = Set[Int]()
  var highestCompleted = protocol.runsCompleted

  var paused = false
  var aborted = false

  private var handler: Option[IPCHandler] = None

  private val progressDialog = new ProgressDialog(parent, this, compiler, colorizer, saveProtocol)

  override def start(): Unit = {
    EventQueue.mustBeEventDispatchThread()

    workspace.jobManager.haltSecondary()
    workspace.jobManager.haltPrimary()

    if (protocol.runsCompleted == 0 && !automated) {
      try {
        new RunOptionsDialog(parent, dialogFactory, Option(workspace.getModelDir).map(new File(_).toPath),
                             workspace.guessExportName(protocol.name), protocol).run()
      } catch {
        case ex: UserCancelException => return
      }
    }

    progressDialog.setUpdateView(protocol.updateView)
    progressDialog.setPlotsAndMonitorsSwitch(protocol.updatePlotsAndMonitors)
    progressDialog.enablePlotsAndMonitorsSwitch(protocol.updatePlotsAndMonitors)

    workspace.setShouldUpdatePlots(protocol.updatePlotsAndMonitors)
    workspace.setExportPlotWarningAction(ExportPlotWarningAction.Warn)
    workspace.setTriedToExportPlot(false)

    super.start()
  }

  override def run(): Unit = {
    def strToArgs(arg: String, value: String): Seq[String] = {
      if (value.isEmpty) {
        Seq()
      } else {
        Seq(arg, value)
      }
    }

    def boolToArgs(arg: String, value: Boolean): Seq[String] = {
      if (value) {
        Seq(arg)
      } else {
        Seq()
      }
    }

    EventQueue.invokeLater(() => progressDialog.setVisible(true))

    var process: Option[Process] = None

    try {
      process = Option(Process(Seq("java", "-Xmx2G", "org.nlogo.headless.Main", "--headless", "--model", modelPath.toString,
                                   "--experiment", protocol.name, "--threads", protocol.threadCount.toString,
                                   "--skip", protocol.runsCompleted.toString, "--ipc") ++
                               boolToArgs("--update-plots", protocol.updatePlotsAndMonitors) ++
                               boolToArgs("--mirror-headless", protocol.mirrorHeadlessOutput) ++
                               strToArgs("--table", protocol.table.trim) ++
                               strToArgs("--spreadsheet", protocol.spreadsheet.trim) ++
                               strToArgs("--stats", protocol.stats.trim) ++
                               strToArgs("--lists", protocol.lists.trim)).run(true))

      val ipcHandler = IPCHandler(true)

      handler = Option(ipcHandler)

      new Thread {
        override def run(): Unit = {
          while (process.exists(_.isAlive)) {
            ipcHandler.readLine().map(processOutput).recover {
              case _: SocketException =>
                process.foreach(_.destroy())
            }
          }
        }
      }.start()

      process.foreach(_.exitValue())
    } catch {
      case _: InterruptedException => // ignore, user most likely aborted the experiment (Isaac B 9/30/25)
    }

    process.foreach(_.destroy())
    handler.foreach(_.close())

    EventQueue.invokeLater(() => {
      workspace.jobManager.haltPrimary()
      workspace.jobManager.haltSecondary()
      workspace.behaviorSpaceRunNumber(0)
      workspace.behaviorSpaceExperimentName("")
      workspace.setShouldUpdatePlots(true)

      if (paused) {
        progressDialog.saveProtocolP()
      } else {
        progressDialog.resetProtocol()
      }

      progressDialog.close()
    })
  }

  private def processOutput(str: String): Unit = {
    Try {
      val json = ujson.read(str)

      json("type").str match {
        case "mirror" =>
          workspace.mirrorOutput(new OutputObject(json("caption").str, json("message").str, json("newline").bool,
                                                  json("temporary").bool),
                                 json("area").bool)

        case "error" =>
          val message = json("message").str

          json("exception").str match {
            case "compiler" => guiError(I18N.gui("error.compilation") + "\n" + message)
            case "runtime" => guiError(I18N.gui("error.runtime") + "\n" + message)
            case "io" => guiError(I18N.gui("error.io") + "\n" + message)
            case _ => Exceptions.handle(new Throwable(message))
          }

        case "experiment_start" =>
          progressDialog.experimentStarted()

        case "run_start" =>
          progressDialog.runStarted(json("number").num.toInt, json("settings").arr.toSeq.map { obj =>
            (obj("name").str, obj("value").str)
          })

        case "step_complete" =>
          progressDialog.stepCompleted(json("number").num.toInt)

        case "mirror_world" =>
          if (workspace.displaySwitchOn) {
            val state = Mirroring.merge(Map(), Serializer.fromBytes(Base64.getDecoder.decode(json("state").str)))

            val renderer = new FakeWorld(state).newRenderer

            val actionBuffer = new ActionBuffer(new DrawingActionBroker(renderer.trailDrawer))

            actionBuffer.activate()

            val actionRunner = new DrawingActionRunner(renderer.trailDrawer)

            actionBuffer.grab().foreach(actionRunner.run)

            EventQueue.invokeLater(() => {
              workspace.view.paintFromRenderer(renderer)
            })
          }

        case "measurements" =>
          progressDialog.measurementsTaken(json("values").arr.map(_.num).toSeq)

        case "run_complete" =>
          completed += json("number").num.toInt

          while (completed.contains(highestCompleted + 1))
            highestCompleted += 1

        case _ =>
      }
    }
  }

  def pause(): Unit = {
    paused = true

    handler.foreach(_.writeLine(ujson.write(Obj(
      "type" -> "pause"
    ))))
  }

  def abort(): Unit = {
    aborted = true
    interrupt()
  }

  def writing(): Unit = {
    progressDialog.writing()
  }

  private def guiError(message: String): Unit = {
    new OptionPane(workspace.getFrame, I18N.gui("error.title"), message, OptionPane.Options.Ok, OptionPane.Icons.Error)
  }
}
