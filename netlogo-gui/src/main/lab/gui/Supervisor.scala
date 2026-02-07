// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window
import java.io.File
import java.net.SocketException
import java.nio.file.Path

import org.nlogo.api.{ IPCHandler, LabProtocol, Version }
import org.nlogo.core.I18N
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ EditDialogFactory, GUIWorkspace }

import scala.sys.process.{ Process, ProcessLogger }
import scala.util.Try

class Supervisor(parent: Window, workspace: GUIWorkspace, modelPath: Path, protocol: LabProtocol,
                 dialogFactory: EditDialogFactory, saveProtocol: (LabProtocol, Int) => Unit, automated: Boolean)
  extends Thread("BehaviorSpace Supervisor") {

  private val handler = IPCHandler(true)

  private var process: Option[Process] = None

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
                         .run(ProcessLogger(println, processError)))

      handler.connect()

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
  }

  private def processError(str: String): Unit = {
    new OptionPane(workspace.getFrame, I18N.gui.get("tools.behaviorSpace.error.title"), str, OptionPane.Options.Ok,
                   OptionPane.Icons.Error)
  }

  private def processMessage(str: String): Unit = {
    Try {
      val json = ujson.read(str)

      json("type").str match {
        case "pause" =>
          protocol.updateView = json("update_view").bool
          protocol.updatePlotsAndMonitors = json("update_plots").bool

          saveProtocol(protocol, json("completed").num.toInt)

        case _ =>
          saveProtocol(protocol, 0)
      }
    }
  }

  def abort(): Unit = {
    process.foreach(_.destroy())
  }
}
