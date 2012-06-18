// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.Dump
import org.nlogo.api.WorldDimensions
import org.nlogo.nvm.Workspace

// Currently this class contains two kinds of code: code for remembering run data in memory using
// Run objects, and code for generating spreadsheet data from those Run objects.  Both kinds of code
// are here because currently we only have one output format that needs to store run data.  If we
// add more formats like that, then this one should probably be split up so that the other formats
// can share the code for remembering the run data. - ST 12/30/08

class SpreadsheetExporter(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: Protocol,
                          out: java.io.PrintWriter)
  extends Exporter(modelFileName, initialDims, protocol, out)
{
  val runs = new collection.mutable.HashMap[Int,Run]
  override def runStarted(w: Workspace, runNumber: Int, settings: List[Pair[String,Any]]) {
    runs(runNumber) = new Run(settings)
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) {
    runs(runNumber).addMeasurements(values)
  }
  override def runCompleted(w: Workspace, runNumber: Int, steps: Int) {
    runs(runNumber).done = true
    runs(runNumber).steps = steps
  }
  def finish() {
    for(runNumber <- runs.keySet)
      if(!runs(runNumber).done)
        runs -= runNumber
    writeExportHeader()
    writeSummary()
    if(!protocol.metrics.isEmpty)
      writeRunData()
    out.close()
  }
  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
  def runNumbers = runs.keySet.toList.sorted
  def foreachRun(fn: (Run, Int) => Option[Any]) {
    // if the experiment was aborted, the completed run numbers might not be
    // consecutive, so we have to be careful - ST 3/31/09
    val outputs =
      for {
        runNumber <- runNumbers
        // even if there are no metrics, in this context we pretend there is one, otherwise we'd output
        // nothing at all - ST 12/17/04, 5/6/08
        j <- 0 until (1 max protocol.metrics.length)
        output = fn(runs(runNumber), j).map(Dump.csv.data).getOrElse("")
      } yield output
    out.println(outputs.mkString(","))
  }
  def writeSummary() {
    // first output run numbers, like this:
    // "[run number]","1","2","3"
    out.print(Dump.csv.header("[run number]"))
    for(runNumber <- runNumbers)
      out.print(List.fill(1 max protocol.metrics.length)(Dump.csv.number(runNumber))
                    .mkString(",", ",", ""))
    out.println()
    // now output one row per variable, like this:
    // "initial-density","0.3","0.5","0.4"
    // "fgcolor","133.0","133.0","133.0"
    // "bgcolor","79.0","79.0","79.0"
    for(v <- protocol.valueSets.map(_.variableName)) {
      out.print(Dump.csv.header(v) + ",")
      foreachRun((run,metricNumber) =>
        if(metricNumber == 0)
          Some(run.settings.find(_._1 == v).get._2)
        else None)
    }
    // now output summary information, like this:
    // "[reporter]","metric","metric","metric"
    // "[final]","248.0","228.0","243.0"
    // "[min]","225.0","196.0","243.0"
    // "[max]","534.0","845.0","704.0"
    // "[mean]","341.57142857142856","360.8095238095238","381.8095238095238"
    // "[steps]","20","20","20"
    if(protocol.runMetricsEveryStep && !protocol.metrics.isEmpty) {
      out.print(Dump.csv.header("[reporter]"))
      for(_ <- runs; metric <- protocol.metrics)
        out.print("," + Dump.csv.header(metric))
      out.println()
      out.print(Dump.csv.header("[final]") + ",")
      foreachRun((run, metricNumber) =>
        Some(run.lastMeasurement(metricNumber)))
      out.print(Dump.csv.header("[min]") + ",")
      foreachRun((run, metricNumber) =>
        run.minMeasurement(metricNumber))
      out.print(Dump.csv.header("[max]") + ",")
      foreachRun((run, metricNumber) =>
        run.maxMeasurement(metricNumber))
      out.print(Dump.csv.header("[mean]") + ",")
      foreachRun((run, metricNumber) =>
        run.meanMeasurement(metricNumber))
    }
    out.print(Dump.csv.header("[steps]") + ",")
    foreachRun((run,metricNumber) =>
      Some(Int.box(run.steps)))
  }
  def writeRunData() {
    // output the raw run data, like this:
    // "[all run data]","metric","metric","metric"
    // ,"473.0","845.0","704.0"
    // ,"534.0","468.0","614.0"
    // ,"478.0","452.0","548.0"
    // ,"403.0","425.0","466.0"
    // ,"399.0","423.0","439.0"
    out.println()
    out.print(Dump.csv.header(if(protocol.runMetricsEveryStep) "[all run data]"
                              else "[initial & final values]"))
    for(_ <- runs; metric <- protocol.metrics)
      out.print(',' + Dump.csv.header(metric))
    out.println()
    if(runs.isEmpty) return
    // first figure out how long the longest run is, so we know in
    // advance how many rows to generate
    val mostMeasurements =
      if(protocol.runMetricsEveryStep)
        runs.values.map(_.steps).max
      else 0
    // now actually generate the rows
    for(i <- 0 to mostMeasurements) {
      out.print(",")
      foreachRun((run,metricNumber) =>
        if(protocol.runMetricsEveryStep && i > run.steps) None
        else Some(run.getMeasurement(i,metricNumber)))
    }
  }
  ///
  class Run(val settings: List[Pair[String,Any]]) {
    var done = false
    var steps = 0
    // values for the metrics at each time step; the values are often Doubles, but not necessarily.
    // we use Array instead of List because List has a lot of memory overhead (one object per
    // cons cell) and for a big experiment we can have a ton of measurements.
    val measurements = new collection.mutable.ArrayBuffer[Array[AnyRef]]
    def addMeasurements(values: List[AnyRef]) { measurements += values.toArray }
    // careful here... normally measurement number means step number, but if runMetricsEveryStep is
    // false, then we'll only have two measurements, regardless of the number of steps - ST 12/19/04
    def getMeasurement(measurementNumber: Int, metricNumber: Int): AnyRef =
      measurements(measurementNumber)(metricNumber)
    def lastMeasurement(metricNumber: Int): AnyRef =
      measurements.last(metricNumber)
    def doubles(metricNumber: Int): Seq[Double] =
      measurements.map(_(metricNumber)).collect{
        case d: java.lang.Double => d.doubleValue}
    def minMeasurement(metricNumber: Int): Option[Double] =
      Some(doubles(metricNumber))
        .filter(_.nonEmpty)
        .map(_.min)
    def maxMeasurement(metricNumber: Int): Option[Double] =
      Some(doubles(metricNumber))
        .filter(_.nonEmpty)
        .map(_.max)
    // includes initial measurement
    def meanMeasurement(metricNumber: Int): Option[Double] =
      Some(doubles(metricNumber))
        .filter(_.size == measurements.size)
        .map(_.sum / measurements.size)
  }
}
