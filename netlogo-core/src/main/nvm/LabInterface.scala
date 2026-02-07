// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.io.{ File, PrintWriter }

import org.nlogo.api.{ LabPostProcessorInputFormat, LabProtocol, PartialData }
import org.nlogo.core.WorldDimensions

object LabInterface {
  trait Worker {
    def protocol: LabProtocol
    def addListener(l: ProgressListener): Unit
    def addTableWriter(modelFileName: String, initialDims: WorldDimensions, w: PrintWriter): Unit
    def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, w: PrintWriter,
                             partialData: PartialData = new PartialData): Unit
    def addStatsWriter(modelFileName: String, initialDims: WorldDimensions, w: PrintWriter,
                       in: LabPostProcessorInputFormat.Format): Unit
    def addListsWriter(modelFileName: String, initialDims: WorldDimensions, w: PrintWriter,
                       in: LabPostProcessorInputFormat.Format): Unit
    def run(testWorkspace: Workspace, fn: () => Workspace, threads: Int): Unit
    def abort(): Unit
    def compile(w: Workspace): Unit // only for testing purposes
  }

  trait ProgressListener {
    def experimentStarted(): Unit = { }
    def experimentAborted(): Unit = { }
    def experimentCompleted(): Unit = { }
    def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]): Unit = { }
    def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = { }
    def stepCompleted(w: Workspace, step: Int): Unit = { }
    def runCompleted(w: Workspace, runNumber: Int, steps: Int): Unit = { }
    def runtimeError(w: Workspace, runNumber: Int, t: Throwable): Unit = { }
  }

  case class Settings(modelPath: String,
    protocolName: Option[String],
    externalXMLFile: Option[File],
    table: Option[String],
    spreadsheet: Option[String],
    stats: Option[String],
    lists: Option[String],
    dims: Option[WorldDimensions],
    threads: Int,
    suppressErrors: Boolean,
    updatePlots: Boolean,
    mirrorHeadlessOutput: Boolean,
    runsCompleted: Int
  )

  sealed abstract trait Result

  object Result {
    case object Aborted extends Result
    case object Paused extends Result
    case object Completed extends Result
  }
}

trait LabInterface {
  def newWorker(protocol: LabProtocol): LabInterface.Worker
  def run(settings: LabInterface.Settings, worker: LabInterface.Worker, primaryWorkspace: PrimaryWorkspace,
          fn: () => Workspace): LabInterface.Result
  def pause(): Unit
  def abort(): Unit
}
