package org.nlogo.lab

import org.nlogo.api.{LabProtocol, LabPostProcessorInputFormat, LabExporterType}
import org.nlogo.core.{LogoList, WorldDimensions}
import scala.collection.mutable.{ HashMap, HashSet, ListBuffer }
import scala.collection.immutable.{ Set }
import java.io.{ BufferedReader, FileReader }
import scala.math.{ max }

class StatsExporter(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          out: java.io.PrintWriter,
                          in: LabPostProcessorInputFormat.Format)
  extends Exporter(modelFileName, initialDims, protocol, out, exporterType=LabExporterType.STATS)
{
  type Measurements = ListBuffer[List[Any]]
  type DataPerStep = HashMap[Int, Measurements]
  type Data = HashMap[List[Any], DataPerStep]

  private var seen = Set[List[Any]]()
  val paramCombinations = ListBuffer[List[Any]]()
  for (param <- protocol.refElements.map(_.map(_._2))) {
    if (!seen(param)) {
      paramCombinations += param
      seen += param
    }
  }
  val countParams = {
    if (protocol.refElements.length > 0) {
      protocol.refElements.toList(0).length
    } else {
      0
    }
  }
  val numericMetrics = HashSet[String]()
  val listMetrics = HashSet[String]()
  val invalidMetrics = HashSet[String]()

  def writeExperimentHeader() {
    val metrics = ListBuffer[String]()
    for (m <- protocol.metrics) {
      if (!(invalidMetrics contains m)) {
        metrics += (f"(mean) ${m}")
        metrics += (f"(std) ${m}")
      }
    }
    val headers = protocol.valueSets(0).map(_.variableName) ::: "[step]" :: metrics.toList
    out.println(headers.map(csv.header).mkString(","))
    out.flush()
  }

  def writeTableRow(params: List[Any], stats: List[Any], step: Int) {
    val writeValues = (params :+ step) ::: stats
    out.println(writeValues.map(csv.data).mkString(","))
    out.flush()
  }

  def extractData(): Option[Data] = {
    in match {
      case t: LabPostProcessorInputFormat.Table => Some(extractFromTable(t.fileName))
      case s: LabPostProcessorInputFormat.Spreadsheet => Some(extractFromSpreadsheet(s.fileName))
      case _ => None
      }
  }

  def process() {
    val d = extractData()
    d match {
      case Some(data) => {
        writeExportHeader()
        writeExperimentHeader()

        for (params <- paramCombinations) {
          if (data contains params) {
            val runData = data(params)
            val sortedSteps = runData.keys.toList.sorted

            for (step <- sortedSteps) {
              if (runData contains step) {
                val values = runData(step)
                val numMetrics = values(0).length
                val writeData = ListBuffer[Any]()

                for (i <- 0 until numMetrics) {
                  val metric = protocol.metrics(i)

                  if (!(invalidMetrics contains metric)) {
                    if (numericMetrics contains metric) {
                      val metricValues = values.map(_(i)).toList.asInstanceOf[List[Double]]
                      val mean = StatsCalculator.mean(metricValues)
                      writeData += mean

                      val std = StatsCalculator.std(metricValues)
                      writeData += {
                        if (std.isNaN) "N/A"
                        else std
                      }
                    } else if (listMetrics contains metric) {
                      val metricValues = values.map(_(i)).toList.asInstanceOf[List[List[Double]]]
                      var maxLength = 0
                      for (list <- metricValues) {
                        maxLength = max(maxLength, list.length)
                      }

                      val means = ListBuffer[Double]()
                      val stds = ListBuffer[Any]()
                      for (j <- 0 until maxLength) {
                        val elementWiseValues = ListBuffer[Double]()
                        for (list <- metricValues) {
                          if (j < list.length) {
                            elementWiseValues += list(j)
                          }
                        }
                        val mean = StatsCalculator.mean(elementWiseValues.toList)
                        means += mean

                        val std = StatsCalculator.std(elementWiseValues.toList)
                        stds += {
                          if (std.isNaN) "N/A"
                          else std
                        }
                      }
                      writeData += f"[${means.mkString(" ")}]"
                      writeData += f"[${stds.mkString(" ")}]"
                    }
                  }
                }
                writeTableRow(params, writeData.toList, step)
              }
            }
          }
        }
      }
      case None =>
    }
    out.close()
  }

  private def handleNonNumeric(entry: String, metricIndex: Int): Double = {
    try {
      val converted = entry.toDouble
      if (metricIndex >= 0) {
        val metric = protocol.metrics(metricIndex)
        // Ignore metrics that produce lists and numbers
        numericMetrics += metric
      }
      converted
    } catch {
      case _: java.lang.NumberFormatException => {
        if (metricIndex >= 0) {
          val metric = protocol.metrics(metricIndex)
          invalidMetrics += metric
        }
        Double.NaN
      }
    }
  }

  private def handleList(entry: String, metricIndex: Int): List[Double] = {
    val noBrackets = entry.replaceAll("[\\[\\]]", "")
    if (noBrackets.length == 0) return List[Double]()
    try {
      val converted = noBrackets.split(" ").toList.map(_.toDouble)
      if (metricIndex >= 0) {
        val metric = protocol.metrics(metricIndex)
        // Ignore metrics that produce lists and numbers
        if (noBrackets.length == entry.length
          || (numericMetrics contains metric)) invalidMetrics += metric
        else listMetrics += metric
      }
      converted
    } catch {
      case _: java.lang.NumberFormatException => {
        val metric = protocol.metrics(metricIndex)
        invalidMetrics += metric
        List(Double.NaN)
      }
    }
  }

  def extractFromTable(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    var row = 0
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    while ({line = bufferedReader.readLine; line} != null) {
      val params = ListBuffer[Any]()
      val measurements = ListBuffer[Any]()
      // The first 6 contain file data, and the 7th row is the header names
      if (row > Exporter.NUM_HEADER_ROWS) {
        line.split(",").zipWithIndex.foreach{case (entry, col) => {
          val noQuotes = entry.split("\"")(1)

          // Allow parameters to have string values, but convert non-numeric measurement values to NaN
          if (col > countParams) {
            val metricIndex = col - (countParams + 2) // +2 to account for the runNumber col and step col
            if ((metricIndex > -1 && (listMetrics contains (protocol.metrics(metricIndex))))
              || (noQuotes contains "["))
              measurements += handleList(noQuotes, metricIndex)
            else measurements += handleNonNumeric(noQuotes, metricIndex)
          } else {
            val n = handleNonNumeric(noQuotes, -1)
            if (n.isNaN) params += noQuotes
            else params += n
          }
        }}
        val step = measurements(0).toString.toDouble.toInt
        val paramsList = params.drop(1).toList
        if (!data.contains(paramsList)) {
          data(paramsList) = new HashMap[Int, Measurements]()
        }
        if (!data(paramsList).contains(step)) {
          data(paramsList)(step) = new ListBuffer[List[Any]]()
        }
        data(paramsList)(step) += measurements.drop(1).toList
      }
      row += 1
    }
    bufferedReader.close()
    data
  }

  def extractFromSpreadsheet(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    var reachedRunData = false
    while ({line = bufferedReader.readLine; line} != null) {
      val rowElements = line.split("[,\n]")

      // Read lines until the line that contains "[all run data]" or "[initial & final values]" is reached
      if (reachedRunData) {
        var runNumber = 1

        // Data is stored along the row, so increment our current position by the number of metrics (+1 to account for the steps column)
        // to access each run
        for (i <- 1 to rowElements.length - 1 by protocol.metrics.length + 1) {
          val params = {
            if (protocol.sequentialRunOrder) {
              paramCombinations((runNumber - 1) / protocol.repetitions)
            } else {
              paramCombinations((runNumber - 1) % protocol.repetitions)
            }
          }
          val step = rowElements(i).split("\"")(1).toInt
          val measurements = rowElements.slice(i+1, protocol.metrics.length + i + 1).zipWithIndex.map{case (entry, col) => {
            val noQuotes = entry.split("\"")(1)
            if ((noQuotes contains "[")
              ||(listMetrics contains (protocol.metrics(col)))) handleList(noQuotes, col)
            else handleNonNumeric(noQuotes, col)
          }}.toList

          if (!data.contains(params)) {
            data(params) = new HashMap[Int, Measurements]()
          }
          if (!data(params).contains(step)) {
            data(params)(step) = new ListBuffer[List[Any]]()
          }
          data(params)(step) += measurements
          runNumber += 1
        }
      } else if (rowElements.length > 0  &&
                ((rowElements(0) equals "\"[all run data]\"")
                  || (rowElements(0) equals "\"[final value]\""))) {
        reachedRunData = true
      }
    }
    bufferedReader.close()
    data
  }

  def extractFromSpreadsheet(spreadsheet: SpreadsheetExporter): Data = {
    val data = new HashMap[List[Any], DataPerStep]()
    val spreadsheetData = spreadsheet.runs
    for ((runNumber, run) <- spreadsheetData) {
      val params = run.settings.map(_._2)
      if (!data.contains(params)) {
        data(params) = new HashMap[Int, Measurements]()
      }
      for (runData <- run.measurements) {
        val step = runData(0).toString.toDouble.toInt
        val measurements = runData.drop(1)

        if (!data(params).contains(step)) {
          data(params)(step) = new ListBuffer[List[Any]]()
        }
        data(params)(step) += measurements.zipWithIndex.map{case (entry, col) => {
          val currentMetric = protocol.metrics(col)
          if (entry.isInstanceOf[LogoList]) {
            if (numericMetrics contains currentMetric) {
              invalidMetrics += currentMetric
              List(Double.NaN)
            } else {
              listMetrics += currentMetric
              entry.asInstanceOf[LogoList].toList
            }
          }
          else if (listMetrics contains currentMetric) {
            invalidMetrics += currentMetric
            List(Double.NaN)
          }
          else handleNonNumeric(entry.toString, col)
        }}.toList
      }
    }
    data
  }

  override def experimentCompleted() { process() }
  override def experimentAborted() { process() }
}
