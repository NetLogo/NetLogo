// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{LabExporterType, LabProtocol}
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.Workspace

// Currently this class contains two kinds of code: code for remembering run data in memory using
// Run objects, and code for generating spreadsheet data from those Run objects.  Both kinds of code
// are here because currently we only have one output format that needs to store run data.  If we
// add more formats like that, then this one should probably be split up so that the other formats
// can share the code for remembering the run data. - ST 12/30/08

class SpreadsheetExporter(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          out: java.io.PrintWriter,
                          partialData: PartialData = new PartialData)
  extends Exporter(modelFileName, initialDims, protocol, out, exporterType=LabExporterType.SPREADSHEET)
{
  val runs = new collection.mutable.HashMap[Int,Run]
  override def runStarted(w: Workspace, runNumber: Int, settings: List[(String,Any)]): Unit = {
    runs(runNumber) = new Run(settings)
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
    runs(runNumber).addMeasurements(step, values)
  }
  override def runCompleted(w: Workspace, runNumber: Int, steps: Int): Unit = {
    runs(runNumber).done = true
    runs(runNumber).steps = steps
  }
  def finish(): Unit = {
    for(runNumber <- runs.keySet)
      if (!runs(runNumber).done)
        runs -= runNumber
    writeExportHeader()
    writeSummary()
    if (!protocol.metrics.isEmpty)
      writeRunData()
    out.close()
  }
  override def experimentCompleted(): Unit = { finish() }
  override def experimentAborted(): Unit = { finish() }
  def runNumbers = runs.keySet.toList.sorted
  def foreachRun(fn: (Run, Int) => Option[Any]): Unit = {
    // if the experiment was aborted, the completed run numbers might not be
    // consecutive, so we have to be careful - ST 3/31/09
    val outputs =
      for {
        runNumber <- runNumbers
        // even if there are no metrics, in this context we pretend there is one, otherwise we'd output
        // nothing at all - ST 12/17/04, 5/6/08
        j <- 0 until (protocol.metrics.length + 1)
        output = fn(runs(runNumber), j).map(csv.data).getOrElse("")
      } yield output
    out.println(outputs.mkString(","))
  }
  def writeSummary(): Unit = {
    // first output run numbers, like this:
    // "[run number]","1","2","3"
    out.print(csv.header("[run number]"))
    out.print(partialData.runNumbers)
    for(runNumber <- runNumbers)
      out.print(List.fill(protocol.metrics.length + 1)(csv.number(runNumber))
                    .mkString(",", ",", ""))
    out.println()
    // now output one row per variable, like this:
    // "initial-density","0.3","0.5","0.4"
    // "fgcolor","133.0","133.0","133.0"
    // "bgcolor","79.0","79.0","79.0"
    var i = 0
    for(v <- protocol.valueSets(0).map(_.variableName)) {
      out.print(csv.header(v) + ",")
      if (partialData.variables.length > 0)
        out.print(partialData.variables(i))
      i += 1
      foreachRun((run,metricNumber) =>
        if (metricNumber == 0)
          Some(run.settings.find(_._1 == v).get._2)
        else None)
    }
    // now output summary information, like this:
    // "[reporter]","metric","metric","metric"
    // "[final]","248.0","228.0","243.0"
    // "[min]","225.0","196.0","243.0"
    // "[max]","534.0","845.0","704.0"
    // "[mean]","341.57142857142856","360.8095238095238","381.8095238095238"
    // "[total steps]","20","20","20"
    if ((protocol.runMetricsEveryStep || !protocol.runMetricsCondition.isEmpty) && !protocol.metrics.isEmpty) {
      out.print(csv.header("[reporter]"))
      out.print(partialData.reporters)
      for (_ <- runs) {
        out.print("," + csv.header("[step]"))
        for (metric <- protocol.metrics)
          out.print("," + csv.header(metric))
      }
      out.println()
      out.print(csv.header("[final]") + partialData.finals + ",")
      foreachRun((run, metricNumber) =>
        run.lastMeasurement(metricNumber))
      out.print(csv.header("[min]") + partialData.mins + ",")
      foreachRun((run, metricNumber) =>
        run.minMeasurement(metricNumber))
      out.print(csv.header("[max]") + partialData.maxes + ",")
      foreachRun((run, metricNumber) =>
        run.maxMeasurement(metricNumber))
      out.print(csv.header("[mean]") + partialData.means + ",")
      foreachRun((run, metricNumber) =>
        run.meanMeasurement(metricNumber))
    }
    out.print(csv.header("[total steps]") + partialData.steps + ",")
    foreachRun((run,metricNumber) =>
      Some(Int.box(run.steps)))
  }
  def writeRunData(): Unit = {
    // output the raw run data, like this:
    // "[all run data]","metric","metric","metric"
    // ,"473.0","845.0","704.0"
    // ,"534.0","468.0","614.0"
    // ,"478.0","452.0","548.0"
    // ,"403.0","425.0","466.0"
    // ,"399.0","423.0","439.0"
    out.println()
    out.print(csv.header(if (protocol.runMetricsEveryStep || !protocol.runMetricsCondition.isEmpty) "[all run data]"
                              else "[final value]"))
    out.print(partialData.dataHeaders)
    for(_ <- runs) {
      out.print("," + csv.header("[step]"))
      for (metric <- protocol.metrics) {
        out.print("," + csv.header(metric))
      }
    }
    out.println()
    if (runs.isEmpty) return
    // first figure out how long the longest run is, so we know in
    // advance how many rows to generate
    val mostMeasurements =
      if (protocol.runMetricsEveryStep)
        runs.values.map(_.steps).max
      else if (!protocol.runMetricsCondition.isEmpty)
        runs.values.map(_.numMeasurements).max
      else 0

    // now actually generate the rows
    for(i <- 0 to mostMeasurements) {
      if (!partialData.data.isEmpty) {
        out.print(partialData.data.head)
        partialData.data = partialData.data.tail
      }
      out.print(",")
      foreachRun((run,metricNumber) =>
        if (protocol.runMetricsEveryStep && i > run.steps) None
        else if (!protocol.runMetricsCondition.isEmpty && i > run.numMeasurements - 1) None // Subtract one for now, since we run the final metrics
        else Some(run.getMeasurement(i,metricNumber)))
    }
  }
  ///
  class Run(val settings: List[(String,Any)]) {
    var done = false
    var steps = 0
    var numMeasurements = 0
    // values for the metrics at each time step; the values are often Doubles, but not necessarily.
    // we use Array instead of List because List has a lot of memory overhead (one object per
    // cons cell) and for a big experiment we can have a ton of measurements.
    val measurements = new collection.mutable.ArrayBuffer[Array[AnyRef]]
    def addMeasurements(step: Int, values: List[AnyRef]): Unit = {
      measurements += (step.toDouble.asInstanceOf[java.lang.Double] :: values).toArray
      numMeasurements += 1
    }
    // careful here... normally measurement number means step number, but if runMetricsEveryStep is
    // false, then we'll only have two measurements, regardless of the number of steps - ST 12/19/04
    def getMeasurement(measurementNumber: Int, metricNumber: Int): AnyRef =
      measurements(measurementNumber)(metricNumber)
    def lastMeasurement(metricNumber: Int): Option[AnyRef] =
      Some(measurements.last(metricNumber))
    def doubles(metricNumber: Int): Seq[Double] =
      measurements.map(_(metricNumber)).collect {
        case d: java.lang.Double => d.doubleValue
      }.toSeq
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

class PartialData
{
  var runNumbers: String = ""
  var variables: Seq[String] = Seq[String]()
  var reporters: String = ""
  var finals: String = ""
  var mins: String = ""
  var maxes: String = ""
  var means: String = ""
  var steps: String = ""
  var dataHeaders: String = ""
  var data: Seq[String] = Seq[String]()
}
