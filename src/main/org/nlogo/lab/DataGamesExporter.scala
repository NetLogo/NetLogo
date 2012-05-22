// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.Dump
import org.nlogo.api.WorldDimensions
import org.nlogo.nvm.Workspace

// There is a lot of copy-and-pasted code from SpreadsheetExporter here.  If this code turns out to
// have a future, the common code should be factored out (as the comment at the top of
// SpreadsheetExporter suggests) - ST 5/22/12

class DataGamesExporter(modelFileName: String,
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
    writeRunData()
    out.close()
  }
  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
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
  def writeRunData() {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    out.println(pretty(render(
      ("collection_name" -> modelFileName) ~
      ("cases" -> runs.keySet.toSeq.sorted.map(runInfo))
    )))
  }
  private def runInfo(runNumber: Int): net.liftweb.json.JValue = {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    def assumeDouble(a: Any) =
      a.asInstanceOf[java.lang.Double].doubleValue
    val run = runs(runNumber)
    val variableNames = protocol.valueSets.map(_.variableName)
    val cases: Seq[JObject] =
      run.measurements.zipWithIndex.map{
        case (values, step) =>
          values.toList.zipWithIndex.map{
            case (value, metricNumber) =>
              protocol.metrics(metricNumber) -> assumeDouble(value)
          }.foldLeft(("step" -> step): JObject)(_ ~ _)}
    (("runNumber" -> runNumber) ~
     (variableNames.head -> assumeDouble(run.settings.find(_._1 == variableNames.head).get._2)) ~
     ("final" -> assumeDouble(run.lastMeasurement(0))) ~
     ("min" -> run.minMeasurement(0).get) ~
     ("max" -> run.maxMeasurement(0).get) ~
     ("mean" -> run.meanMeasurement(0).get) ~
     ("steps" -> run.steps) ~
     ("contents" ->
      ("collection_name" -> "Steps") ~
      ("cases" -> cases)))
  }
}
