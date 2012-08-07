// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.Dump
import org.nlogo.api.WorldDimensions
import org.nlogo.nvm.Workspace

class TableExporter(modelFileName: String,
                    initialDims: WorldDimensions,
                    protocol: Protocol,
                    out: java.io.PrintWriter)
  extends Exporter(modelFileName, initialDims, protocol, out)
{
  val settings = new collection.mutable.HashMap[Int,List[Pair[String,Any]]]
  override def experimentStarted() {
    writeExportHeader()
    writeExperimentHeader()
    out.flush()
  }
  override def runStarted(w: Workspace, runNumber: Int,runSettings: List[Pair[String,Any]]) {
    settings(runNumber) = runSettings
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) {
    if(!values.isEmpty)
      writeTableRow(runNumber,step,values)
  }
  override def runCompleted(w: Workspace, runNumber: Int, steps: Int) {
    if(protocol.metrics.isEmpty)
      writeTableRow(runNumber,steps,Nil)  // record how long the run lasted, if nothing else
    out.flush()
    settings -= runNumber
  }
  override def experimentAborted() {
    out.close()
  }
  override def experimentCompleted() {
    out.close()
  }
  def writeExperimentHeader() {
    // sample header: "[run number]","var1","var2","[step]","metric1","metric2"
    val headers =
      "[run number]" :: protocol.valueSets.map(_.variableName) :::
      "[step]" :: protocol.metrics
    out.println(headers.map(Dump.csv.header).mkString(","))
  }
  def writeTableRow(runNumber: Int, step: Int, values: List[AnyRef]) {
    val entries = runNumber :: settings(runNumber).map(_._2) ::: step :: values
    out.println(entries.map(Dump.csv.data).mkString(","))
  }
}
