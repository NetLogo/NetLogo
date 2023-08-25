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

  override def writeExperimentHeader() {
    val metrics = ListBuffer[String]()
    for (m <- protocol.metrics) {
      metrics += (f"[${m}]-mean")
      metrics += (f"[${m}]-std")
    }
    val headers = protocol.valueSets(0).map(_.variableName) ::: "[step]" :: metrics.toList
    out.println(headers.map(csv.header).mkString(","))
    out.flush()
  }

  def writeTableRow(params: List[Any], stats: List[Double], step: Int) {
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
            val writeData = ListBuffer[Double]()
            for (i <- 0 until numMetrics) {
              val metricValues = values.map(_(i)).toList
              val mean = StatsCalculator.mean(metricValues)
              val std = StatsCalculator.std(metricValues)
              writeData += mean
              writeData += std
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
      // Some(extractFromSpreadsheet(spreadsheetExporter))
      Some(extractFromSpreadsheet(exporterFileNames(spreadsheetExporter)))
    } else {
      Some(extractFromSpreadsheet(spreadsheetExporter))
      // None
    }
  }

  private def extractFromTable(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    var row = 0
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    while ({line = bufferedReader.readLine; line} != null) {
      if (row > 6) {
        val parsed = line.split(",").map(entry => {
          try {
            entry.split("\"")(1).toDouble
          } catch {
            case _: java.lang.NumberFormatException => 0
          }
        }).toList
        val params = parsed.slice(1,countParams+1).toList
        val step = parsed(countParams+1).toInt
        if (!data.contains(params)) {
          data(params) = new HashMap[Int, Measurements]()
        }
        if (!data(params).contains(step)) {
          data(params)(step) = new ListBuffer[List[Double]]()
        }
        data(params)(step) += parsed.takeRight(parsed.length - (1 + countParams))
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
        // parsed.foreach(p => print(f"${p},"))
        // println
        var runNumber = 1
        for (i <- 1 to split.length - 1 by protocol.metrics.length + 1) {
          // println(f"${runNumber} ${split}")
          val parsed = split.map(entry => {
            try {
              if (entry.length > 1) {
                entry.split("\"")(1).toDouble
              } else {
                0
              }
            } catch {
              case _: java.lang.NumberFormatException => 0
            }
          }).toList
          val step = parsed(i).toInt
          val metrics = parsed.slice(i+1, i+protocol.metrics.length+1)
          val params = {
            if (protocol.sequentialRunOrder) {
              // println(f"${runNumber} ${(runNumber - 1) / protocol.repetitions} ${i}")
              paramCombinations((runNumber - 1) / protocol.repetitions)
            } else {
              paramCombinations((runNumber - 1) % protocol.repetitions)
            }
          }
          if (!data.contains(params)) {
            data(params) = new HashMap[Int, Measurements]()
          }
          if (!data(params).contains(step)) {
            data(params)(step) = new ListBuffer[List[Double]]()
          }
          data(params)(step) += metrics
          runNumber += 1
        }
      } else if (split.length > 0  && (split(0) equals "\"[all run data]\"")) {
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
      for (measurement <- run.measurements) {
        val step = measurement(0).toString.toInt
        if (!data(params).contains(step)) {
          data(params)(step) = new ListBuffer[List[Double]]()
        }
        data(params)(step) += measurement.map(entry => {
          try {
            entry.toString.toDouble
          } catch {
            case _: java.lang.NumberFormatException => 0
          }
        }).toList.takeRight(measurement.length - 1)
      }
    }
    data
  }
}