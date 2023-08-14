package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.Workspace
import scala.collection.mutable.{ ArrayBuffer, HashMap }
import scala.math.sqrt

class StatsExporter(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          out: java.io.PrintWriter)
  extends TableExporter(modelFileName, initialDims, protocol, out)
{
  val data = new collection.mutable.HashMap[Int,List[List[AnyRef]]]

  override def runStarted(w: Workspace, runNumber: Int,runSettings: List[(String,Any)]) {
    settings(runNumber) = runSettings
    data(runNumber) = List[ArrayBuffer[AnyRef]]()
  }
  override def writeExperimentHeader(): Null = {
    // sample header: "[run number]","var1","var2","[step]","metric1-mean","metric1-std","metric2-mean","metric2-std"
    val metrics = ArrayBuffer()
    protocol.metrics.foreach(m => {
      metrics += m + "-mean"
      metrics += m + "-std"
    })
    val headers =
      "[run number]" :: protocol.valueSets.map(_.variableName) :::
      "[step]" :: metrics
    out.println(headers.map(csv.header).mkString(","))
  }

  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) {
    for ((v, i)<- values.zipWithIndex) {
      data(runNumber)(i) += v
    }

    val writeData = ArrayBuffer[List[AnyRef]]()
    data(runNumber).foreach(metricValues => {
      val mean = metricValues.sum / metricValues.size
      val std = {
        if (metricValues.size > 1) sqrt(metricValues.map(v => (v - mean)*(v - mean)).sum / step)
        else "None"
      }
    })
    if(!values.isEmpty)
      writeTableRow(runNumber,step,values)
  }

  override def writeTableRow(runNumber: Int, step: Int, values: List[AnyRef]): Null = {

  }

}