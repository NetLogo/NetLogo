// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window
import java.io.File
import java.net.SocketException
import java.nio.file.Path

import org.nlogo.api.{ Exceptions, IPCServerHandler, LabProtocol, Version }
import org.nlogo.core.I18N
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ EditDialogFactory, GUIWorkspace }

import scala.sys.process.{ Process, ProcessLogger }
import scala.util.Try

class Supervisor(parent: Window, workspace: GUIWorkspace, modelPath: Path, protocol: LabProtocol,
                 dialogFactory: EditDialogFactory, saveProtocol: (LabProtocol, Int) => Unit, automated: Boolean)
  extends Thread("BehaviorSpace Supervisor") {

  private val handler = new IPCServerHandler

  private var process: Option[Process] = None

  private var launched = false
  private var success = false
  private var saved = false

  override def start(): Unit = {
    if (protocol.runsCompleted == 0 && !automated) {
      new RunOptionsDialog(parent, dialogFactory, Option(workspace.getModelDir).map(new File(_).toPath),
                           workspace.guessExportName(protocol.name), protocol).run()
    }

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

    var errorLines = Seq[String]()

    try {
      val memoryLimit: Option[String] = {
        if (protocol.memoryLimit == 0) {
          None
        } else {
          Some(s"-Xmx${protocol.memoryLimit}M")
        }
      }

      handler.connect()

      val logger = ProcessLogger(println, line => errorLines = errorLines :+ line)

      process = Option(Process(Seq(ProcessHandle.current.info.command.get) ++ memoryLimit ++ Seq("-cp",
                                   System.getProperty("java.class.path"), s"-Dorg.nlogo.is3d=${Version.is3D}",
                                   s"-Dnetlogo.extensions.dir=${System.getProperty("netlogo.extensions.dir")}",
                                   "-Dapple.awt.application.appearance=system",
                                   "org.nlogo.bsapp.BehaviorSpaceApp", modelPath.toString, protocol.name,
                                   "--threads", protocol.threadCount.toString,
                                   "--error-behavior", protocol.errorBehavior.key,
                                   "--skip", protocol.runsCompleted.toString,
                                   "--port", handler.getPort.toString) ++
                               boolToArg("--update-view", protocol.updateView) ++
                               boolToArg("--update-plots", protocol.updatePlotsAndMonitors) ++
                               boolToArg("--mirror-headless", protocol.mirrorHeadlessOutput) ++
                               strToArg("--table", protocol.table.trim) ++
                               strToArg("--spreadsheet", protocol.spreadsheet.trim) ++
                               strToArg("--stats", protocol.stats.trim) ++
                               strToArg("--lists", protocol.lists.trim) ++
                               boolToArg("--automated", automated)).run(logger))

      new Thread {
        override def run(): Unit = {
          while (process.exists(_.isAlive)) {
            handler.readLine().map(processMessage).recover {
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

    handler.close()

    if (!saved)
      saveProtocol(protocol, 0)

    if (!launched) {
      if (errorLines.isEmpty) {
        new OptionPane(parent, I18N.gui.get("common.messages.error"),
                       I18N.gui.get("tools.behaviorSpace.error.memoryLimit"), OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
      } else {
        Exceptions.handle(new Exception(errorLines.mkString("\n")))
      }
    }
  }

  private def processMessage(str: String): Unit = {
    Try {
      val json = ujson.read(str)

      json("type").str match {
        case "launch" =>
          launched = true

        case "pause" =>
          protocol.updateView = json("update_view").bool
          protocol.updatePlotsAndMonitors = json("update_plots").bool

          saveProtocol(protocol, json("completed").num.toInt)

          saved = true

        case "complete" =>
          success = true

        case _ =>
      }
    }
  }

  def succeeded: Boolean =
    success

  def abort(): Unit = {
    process.foreach(_.destroy())
  }
}
