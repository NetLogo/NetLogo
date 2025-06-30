// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ LabPostProcessorInputFormat, LabProtocol }
import org.nlogo.core.WorldDimensions

object LabInterface {
  trait Worker {
    def protocol: LabProtocol
    def addListener(l: ProgressListener): Unit
    def addTableWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter): Unit
    def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter): Unit
    def addStatsWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter, in: LabPostProcessorInputFormat.Format): Unit
    def addListsWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter,
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
    externalXMLFile: Option[java.io.File],
    tableWriter: Option[java.io.PrintWriter],
    spreadsheetWriter: Option[java.io.PrintWriter],
    statsWriter: Option[(java.io.PrintWriter, String)],
    listsWriter: Option[(java.io.PrintWriter, String)],
    dims: Option[WorldDimensions],
    threads: Int,
    suppressErrors: Boolean,
    updatePlots: Boolean,
    mirrorHeadlessOutput: Boolean = false
    )
}
trait LabInterface {
  import LabInterface._
  def newWorker(protocol: LabProtocol): Worker
  def run(settings: Settings, worker: Worker, fn: () => Workspace): Unit
}
