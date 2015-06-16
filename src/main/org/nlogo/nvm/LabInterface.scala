// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.WorldDimensions

object LabInterface {
  trait Worker {
    def addListener(l: ProgressListener): Unit
    def addTableWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter): Unit
    def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter): Unit
    def run(testWorkspace: Workspace, fn: ()=>Workspace, threads: Int): Unit
    def compile(w: Workspace): Unit // only for testing purposes
  }
  trait ProgressListener {
    def experimentStarted() = {}
    def experimentAborted() = {}
    def experimentCompleted() = {}
    def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]) = {}
    def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) = {}
    def stepCompleted(w: Workspace, step: Int) = {}
    def runCompleted(w: Workspace, runNumber: Int, steps: Int) = {}
    def runtimeError(w: Workspace, runNumber: Int, t: Throwable) = {}
  }
  case class Settings(model: String,
                      setupFile: Option[java.io.File],
                      experiment: Option[String],
                      tableWriter: Option[java.io.PrintWriter],
                      spreadsheetWriter: Option[java.io.PrintWriter],
                      dims: Option[WorldDimensions],
                      threads: Int,
                      suppressErrors: Boolean)
}
trait LabInterface {
  import LabInterface._
  def load(protocols: String): Unit
  def names: List[String]
  def newWorker(protocolName: String): Worker
  def newWorker(setupFile: java.io.File): Worker
  def newWorker(protocolName: String, setupFile: java.io.File): Worker
  def run(settings: Settings, fn: ()=>Workspace): Unit
}
