// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{LabExporterType, LabProtocol}
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.Workspace

class TableExporter(modelFileName: String,
                    initialDims: WorldDimensions,
                    protocol: LabProtocol,
                    out: java.io.PrintWriter)
  extends Exporter(modelFileName, initialDims, protocol, out, exporterType=LabExporterType.TABLE)
{
  val settings = new collection.mutable.HashMap[Int,List[(String,Any)]]
  override def experimentStarted(): Unit = {
    if (protocol.runsCompleted == 0) {
      writeExportHeader()
      writeExperimentHeader()
    }
    out.flush()
  }
  override def runStarted(w: Workspace, runNumber: Int,runSettings: List[(String,Any)]): Unit = {
    settings(runNumber) = runSettings
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
    if(!values.isEmpty)
      writeTableRow(runNumber,step,values)
  }
  override def runCompleted(w: Workspace, runNumber: Int, steps: Int): Unit = {
    if(protocol.metrics.isEmpty)
      writeTableRow(runNumber,steps,Nil)  // record how long the run lasted, if nothing else
    out.flush()
    settings -= runNumber
  }
  override def experimentAborted(): Unit = {
    out.close()
  }
  override def experimentCompleted(): Unit = {
    out.close()
  }
  def writeExperimentHeader(): Unit = {
    // sample header: "[run number]","var1","var2","[step]","metric1","metric2"
    val headers =
      "[run number]" :: protocol.valueSets(0).map(_.variableName) :::
      "[step]" :: protocol.metrics
    out.println(headers.map(csv.header).mkString(","))
  }
  def writeTableRow(runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
    val entries = runNumber :: settings(runNumber).map(_._2) ::: step :: values
    out.println(entries.map(csv.data).mkString(","))
  }
}
