// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import java.io.{ File, FileWriter, PrintWriter }
import java.lang.{ Double => JDouble }
import java.net.SocketException

import org.nlogo.api.{ Dump, LabProtocol, LabPostProcessorInputFormat, PartialData }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ LabInterface, PrimaryWorkspace, Workspace }

import scala.collection.mutable.Queue
import scala.io.Source
import scala.util.Try

import ujson.{ Num, Obj }

class Lab extends LabInterface {
  private var worker: Option[LabInterface.Worker] = None

  private var paused = false
  private var aborted = false

  override def newWorker(protocol: LabProtocol) =
    new Worker(protocol)

  override def run(settings: LabInterface.Settings, worker: LabInterface.Worker, primaryWorkspace: PrimaryWorkspace,
          fn: () => Workspace): Unit = {
    import settings._

    // pool of workspaces is the same size as the thread pool
    // unless there are fewer runs than threads (Isaac B 6/27/25)
    val actualThreads = threads.min(worker.protocol.countRuns)

    val workspaces = (1 to actualThreads).map(_ => fn()).toList
    val queue = new Queue[Workspace]

    workspaces.foreach(queue.enqueue)

    queue.foreach(w => dims.foreach(w.setDimensions))

    def nextWorkspace = queue.synchronized { if (queue.isEmpty) null else queue.dequeue() }

    val modelDims = queue.head.world.getDimensions

    val postProcessor: Option[LabPostProcessorInputFormat.Format] = {
      table.filter(_.trim != "-").map(LabPostProcessorInputFormat.Table(_)).orElse {
        spreadsheet.filter(_.trim != "-").map(LabPostProcessorInputFormat.Spreadsheet(_))
      }
    }

    def error(message: String): Unit = {
      ipcHandler match {
        case Some(handler) =>
          handler.writeLine(ujson.write(Obj(
            "type" -> "error",
            "exception" -> "runtime",
            "message" -> message
          )))

        case _ =>
          println(message)
      }

      abort()
    }

    table.foreach { path =>
      if (path.trim == "-") {
        worker.addTableWriter(modelPath, dims.getOrElse(modelDims), {
          new PrintWriter(System.out) {
            // don't close System.out - ST 6/9/09
            override def close(): Unit = {}
          }
        })
      } else if (worker.protocol.runsCompleted > 0 && !new File(path).exists) {
        error(I18N.gui.get("tools.behaviorSpace.error.pause.table"))
      } else {
        worker.addTableWriter(modelPath, dims.getOrElse(modelDims),
                              new PrintWriter(new FileWriter(path, worker.protocol.runsCompleted > 0)))
      }
    }

    spreadsheet.foreach { path =>
      if (path.trim == "-") {
        worker.addSpreadsheetWriter(modelPath, dims.getOrElse(modelDims), {
          new PrintWriter(System.out) {
            // don't close System.out - ST 6/9/09
            override def close(): Unit = {}
          }
        })
      } else if (worker.protocol.runsCompleted > 0 && !new File(path).exists) {
        error(I18N.gui.get("tools.behaviorSpace.error.pause.spreadsheet"))
      } else if (worker.protocol.runsCompleted > 0) {
        try {
          val partialData = new PartialData

          val source = Source.fromFile(path)

          var data = source.getLines.drop(6).toList

          source.close()

          partialData.runNumbers = "," + data.head.split(",", 2)(1)

          data = data.tail

          while (!data.head.contains("[")) {
            partialData.variables = partialData.variables :+ "," + data.head.split(",", 2)(1)

            data = data.tail
          }

          if (data.head.contains("[reporter]")) {
            partialData.reporters = "," + data.head.split(",", 2)(1)
            data = data.tail
            partialData.finals =  "," + data.head.split(",", 2)(1)
            data = data.tail
            partialData.mins = "," + data.head.split(",", 2)(1)
            data = data.tail
            partialData.maxes = "," + data.head.split(",", 2)(1)
            data = data.tail
            partialData.means = "," + data.head.split(",", 2)(1)
            data = data.tail
          }

          partialData.steps = "," + data.head.split(",", 2)(1)

          data = data.tail.tail

          partialData.dataHeaders = "," + data.head.split(",", 2)(1)
          partialData.data = data.tail

          worker.addSpreadsheetWriter(modelPath, dims.getOrElse(modelDims), new PrintWriter(path), partialData)
        } catch {
          case _: Throwable =>
            error(I18N.gui.get("tools.behaviorSpace.error.pause.invalidSpreadsheet"))
        }
      } else {
        worker.addSpreadsheetWriter(modelPath, dims.getOrElse(modelDims), new PrintWriter(path))
      }
    }

    stats.foreach { path =>
      postProcessor match {
        case Some(processor) =>
          worker.addStatsWriter(modelPath, dims.getOrElse(modelDims), {
            if (path.trim == "-") {
              new PrintWriter(System.out) {
                // don't close System.out - ST 6/9/09
                override def close(): Unit = {}
              }
            } else {
              new PrintWriter(path)
            }
          }, processor)

        case _ =>
          error(I18N.gui.get("tools.behaviorSpace.error.stats"))
      }
    }

    lists.foreach { path =>
      postProcessor match {
        case Some(processor) =>
          worker.addListsWriter(modelPath, dims.getOrElse(modelDims), {
            if (path.trim == "-") {
              new PrintWriter(System.out) {
                // don't close System.out - ST 6/9/09
                override def close(): Unit = {}
              }
            } else {
              new PrintWriter(path)
            }
          }, processor)

        case _ =>
          error(I18N.gui.get("tools.behaviorSpace.error.lists"))
      }
    }

    if (!aborted) {
      val listener: LabInterface.ProgressListener = ipcHandler.fold {
        new LabInterface.ProgressListener {
          override def runCompleted(w: Workspace, runNumber: Int, step: Int): Unit = {
            queue.synchronized { queue.enqueue(w) }
          }

          override def runtimeError(w: Workspace, runNumber: Int, t: Throwable): Unit = {
            if (!suppressErrors)
              primaryWorkspace.runtimeError(t)
          }
        }
      } { handler =>
        new LabInterface.ProgressListener {
          override def experimentStarted(): Unit = {
            handler.writeLine(ujson.write(Obj(
              "type" -> "experiment_start",
            ))).recover {
              case _: SocketException =>
                abort()
            }
          }

          override def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]): Unit = {
            if (w == workspaces.head) {
              handler.writeLine(ujson.write(Obj(
                "type" -> "run_start",
                "number" -> runNumber,
                "settings" -> settings.map((name, value) => Obj(
                  "name" -> name,
                  "value" -> Dump.logoObject(value.asInstanceOf[AnyRef])
                ))
              ))).recover {
                case _: SocketException =>
                  abort()
              }
            }
          }

          override def stepCompleted(w: Workspace, steps: Int): Unit = {
            if (w == workspaces.head) {
              handler.writeLine(ujson.write(Obj(
                "type" -> "step_complete",
                "number" -> steps
              ))).recover {
                case _: SocketException =>
                  abort()
              }
            }
          }

          override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
            if (w == workspaces.head) {
              handler.writeLine(ujson.write(Obj(
                "type" -> "measurements",
                "values" -> values.collect {
                  case d: JDouble => Num(d)
                }
              ))).recover {
                case _: SocketException =>
                  abort()
              }
            }
          }

          override def runCompleted(w: Workspace, runNumber: Int, step: Int): Unit = {
            if (w == workspaces.head) {
              handler.writeLine(ujson.write(Obj(
                "type" -> "run_complete",
                "number" -> runNumber,
                "step" -> step
              ))).recover {
                case _: SocketException =>
                  abort()
              }
            }

            queue.synchronized { if (!paused) queue.enqueue(w) }
          }

          override def runtimeError(w: Workspace, runNumber: Int, t: Throwable): Unit = {
            if (!suppressErrors)
              primaryWorkspace.runtimeError(t)
          }
        }
      }

      worker.addListener(listener)

      ipcHandler.foreach { handler =>
        new Thread {
          override def run(): Unit = {
            while (!paused && !aborted) {
              handler.readLine().flatMap { str =>
                Try {
                  if (ujson.read(str)("type").str == "pause")
                    queue.synchronized { paused = true }
                }
              }.recover {
                case _: SocketException =>
                  abort()
              }
            }
          }
        }.start()
      }

      this.worker = Option(worker)

      worker.run(workspaces.head, () => nextWorkspace, actualThreads)
    }

    workspaces.foreach { w =>
      try {
        w.dispose()
      } catch {
        // if this is caught, the JobManager was in the middle of doing something,
        // probably means the user Halted so it's fine to ignore this (Isaac B 7/10/25)
        case _: InterruptedException =>
      }
    }
  }

  override def abort(): Unit = {
    worker.foreach(_.abort())

    aborted = true
  }
}
