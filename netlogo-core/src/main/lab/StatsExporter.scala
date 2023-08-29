package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.{ HashMap, ListBuffer }
import scala.collection.immutable.{ Set }
import java.io.{ BufferedReader, FileReader }

// import scala.math.sqrt

class StatsProcessor(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          tableExporter: TableExporter,
                          spreadsheetExporter: SpreadsheetExporter,
                          exporterFileNames: HashMap[Exporter, String],
                          out: java.io.PrintWriter)
  extends TableExporter(modelFileName, initialDims, protocol, out)
  with PostProcessor
{
  type Measurements = ListBuffer[List[Double]]
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
  val numericMetrics = HashMap[String, Boolean]();
  for (m <- protocol.metrics) {
    numericMetrics(m) = true
  }

  override def writeExperimentHeader() {
    val metrics = ListBuffer[String]()
    for (m <- protocol.metrics) {
      if (numericMetrics(m)) {
        metrics += (f"[${m}]-mean")
        metrics += (f"[${m}]-std")
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

  def process: Unit = {
    val data = extractData()
    data match {
      case Some(d) => {
        writeExportHeader()
        writeExperimentHeader()
        for (params <- paramCombinations) {
          val runData = d(params)
          val sortedSteps = runData.keys.toList.sorted
          for (step <- sortedSteps) {
            val values = runData(step)
            val numMetrics = values(0).length
            val writeData = ListBuffer[Any]()
            for (i <- 0 until numMetrics) {
              val metric = protocol.metrics(i)
              if (numericMetrics(metric)) {
                val metricValues = values.map(_(i)).toList
                val mean = StatsCalculator.mean(metricValues)
                writeData += mean

                val std = StatsCalculator.std(metricValues)
                writeData += {
                  if (std.isNaN) {
                    "N/A"
                  } else {
                    std
                  }
                }
              }
            }
            writeTableRow(params, writeData.toList, step)
          }
        }
      }
      case None =>
    }
  }

  def extractData(): Option[Data] = {
    if (tableExporter != null) {
      Some(extractFromTable(exporterFileNames(tableExporter)))
    } else if (spreadsheetExporter != null) {
      Some(extractFromSpreadsheet(spreadsheetExporter))
      // Some(extractFromSpreadsheet(exporterFileNames(spreadsheetExporter)))
    } else {
      // Some(extractFromSpreadsheet(spreadsheetExporter))
      Some(extractFromSpreadsheet(exporterFileNames(spreadsheetExporter)))

    }
  }

  private def extractFromTable(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    var row = 0
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    while ({line = bufferedReader.readLine; line} != null) {
      val params = ListBuffer[Any]()
      val measurements = ListBuffer[Double]()
      if (row > 6) {
        line.split(",").zipWithIndex.foreach{case (entry, col) => {
          val noQuotes = entry.split("\"")(1)
          if (col > countParams) {
            try {
              measurements += noQuotes.toDouble
            } catch {
              case _: java.lang.NumberFormatException => {
                numericMetrics(protocol.metrics(col - (countParams + 2))) = false // +2 to account for the runNumber col and step col
                measurements += Double.NaN
              }
            }
          } else {
            try {
              params += noQuotes.toDouble
            } catch {
              case _: java.lang.NumberFormatException => {
                params += noQuotes
              }
            }
          }
        }}
        val step = measurements(0).toInt
        val paramsList = params.drop(1).toList
        if (!data.contains(paramsList)) {
          data(paramsList) = new HashMap[Int, Measurements]()
        }
        if (!data(paramsList).contains(step)) {
          data(paramsList)(step) = new ListBuffer[List[Double]]()
        }
        data(paramsList)(step) += measurements.drop(1).toList
      }
      row += 1
    }
    bufferedReader.close()
    data
  }

  private def extractFromSpreadsheet(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    var reachedRunData = false
    while ({line = bufferedReader.readLine; line} != null) {
      val split = line.split("[,\n]")
      if (reachedRunData) {
        var runNumber = 1
        for (i <- 1 to split.length - 1 by protocol.metrics.length + 1) {
          val params = {
            if (protocol.sequentialRunOrder) {
              paramCombinations((runNumber - 1) / protocol.repetitions)
            } else {
              paramCombinations((runNumber - 1) % protocol.repetitions)
            }
          }
          val step = split(i).split("\"")(1).toInt
          val measurements = split.slice(i+1, protocol.metrics.length + i + 1).zipWithIndex.map{case (entry, col) => {
            try {
              val noQuotes = entry.split("\"")
              noQuotes(1).toDouble
            } catch {
              case _: java.lang.NumberFormatException => {
                numericMetrics(protocol.metrics(col)) = false
                Double.NaN
              }
            }
          }}.toList
          // val measurements = parsed.slice(i+1, i+protocol.metrics.length+1)

          if (!data.contains(params)) {
            data(params) = new HashMap[Int, Measurements]()
          }
          if (!data(params).contains(step)) {
            data(params)(step) = new ListBuffer[List[Double]]()
          }
          data(params)(step) += measurements
          runNumber += 1
        }
      } else if (split.length > 0  && ((split(0) equals "\"[all run data]\"") || (split(0) equals "\"[initial & final values]\""))) {
        reachedRunData = true
      }
    }
    data
  }

  private def extractFromSpreadsheet(spreadsheet: SpreadsheetExporter): Data = {
    val data = new HashMap[List[Any], DataPerStep]()
    val spreadsheetData = spreadsheet.runs
    for ((runNumber, run) <- spreadsheetData) {
      val params = run.settings.map(_._2)
      if (!data.contains(params)) {
        data(params) = new HashMap[Int, Measurements]()
      }
      for (runData <- run.measurements) {
        val step = runData(0).toString.toInt
        val measurements = runData.drop(1)

        if (!data(params).contains(step)) {
          data(params)(step) = new ListBuffer[List[Double]]()
        }
        data(params)(step) += measurements.zipWithIndex.map{case (entry, col) => {
          try {
            entry.toString.toDouble
          } catch {
            case _: java.lang.NumberFormatException => {
              numericMetrics(protocol.metrics(col)) = false
              Double.NaN
            }
          }
        }}.toList.drop(1)
      }
    }
    data
  }
}