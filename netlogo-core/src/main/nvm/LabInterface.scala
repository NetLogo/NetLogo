// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.net.URI
import org.nlogo.api.{ LabProtocol, Version }
import org.nlogo.core.WorldDimensions

object LabInterface {
  trait Worker {
    def addListener(l: ProgressListener)
    def addTableWriter(modelFileName: String, initialDims: WorldDimensions, versionString: String, w: java.io.PrintWriter)
    def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, versionString: String, w: java.io.PrintWriter)
    def run(testWorkspace: Workspace, fn: ()=>Workspace, threads: Int)
    def compile(w: Workspace) // only for testing purposes
  }
  trait ProgressListener {
    def experimentStarted() { }
    def experimentAborted() { }
    def experimentCompleted() { }
    def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]) { }
    def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) { }
    def stepCompleted(w: Workspace, step: Int) { }
    def runCompleted(w: Workspace, runNumber: Int, steps: Int) { }
    def runtimeError(w: Workspace, runNumber: Int, t: Throwable) { }
  }
  case class Settings(
    modelPath: String,
    modelLocation: URI,
    protocolName: Option[String],
    externalXMLFile: Option[URI],
    tableWriter: Option[java.io.PrintWriter],
    spreadsheetWriter: Option[java.io.PrintWriter],
    dims: Option[WorldDimensions],
    threads: Int,
    suppressErrors: Boolean,
    version: Version)
}
trait LabInterface {
  import LabInterface._
  def newWorker(protocol: LabProtocol): Worker
  def run(settings: Settings, protocol: LabProtocol, fn: ()=>Workspace)
}
