// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window
import java.io.File
import java.net.SocketException
import java.nio.file.Path

import org.nlogo.api.{ CompilerServices, Exceptions, ExportPlotWarningAction, IPCHandler, LabProtocol, Version }
import org.nlogo.agent.OutputObject
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ EditDialogFactory, GUIWorkspace }
import org.nlogo.workspace.WorkspaceFactory

import scala.collection.mutable.Set
import scala.sys.process.{ Process, ProcessLogger }
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
    def strToArg(arg: String, value: String): Seq[String] = {
      if (value.isEmpty) {
        Seq()
      } else {
        Seq(arg, value)
      }
    }

    def boolToArg(arg: String, value: Boolean): Seq[String] = {
      if (value) {
        Seq(arg)
      } else {
        Seq()
      }
    }

    EventQueue.invokeLater(() => progressDialog.setVisible(true))

    var process: Option[Process] = None

    try {
      process = Option(Process(Seq(ProcessHandle.current.info.command.get, "-Xmx2G", "-cp",
                                   System.getProperty("java.class.path"), s"-Dorg.nlogo.is3d=${Version.is3D}",
                                   "org.nlogo.bsapp.BehaviorSpaceApp", modelPath.toString, protocol.name,
                                   "--threads", protocol.threadCount.toString,
                                   "--skip", protocol.runsCompleted.toString) ++
                               boolToArg("--update-view", protocol.updateView) ++
                               boolToArg("--update-plots", protocol.updatePlotsAndMonitors) ++
                               boolToArg("--mirror-headless", protocol.mirrorHeadlessOutput) ++
                               strToArg("--table", protocol.table.trim) ++
                               strToArg("--spreadsheet", protocol.spreadsheet.trim) ++
                               strToArg("--stats", protocol.stats.trim) ++
                               strToArg("--lists", protocol.lists.trim))
                         .run(ProcessLogger(processOutput, processError)))

      val ipcHandler = IPCHandler(true)

      handler = Option(ipcHandler)

      ipcHandler.connect()

      progressDialog.addGUIListener(new ProgressDialog.GUIListener {
        override def speedChanged(value: Double): Unit = {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "speed",
            "value" -> value
          )))
        }

        override def updateViewChanged(value: Boolean): Unit = {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "update_view",
            "value" -> value
          )))
        }

        override def updatePlotsChanged(value: Boolean): Unit = {
          ipcHandler.writeLine(ujson.write(Obj(
            "type" -> "update_plots",
            "value" -> value
          )))
        }
      })

      new Thread {
        override def run(): Unit = {
          while (process.exists(_.isAlive)) {
            ipcHandler.readLine().map(processMessage).recover {
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
    println(str)
  }

  private def processError(str: String): Unit = {
    guiError(str)
  }

  private def processMessage(str: String): Unit = {
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
