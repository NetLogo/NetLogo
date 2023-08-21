package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.{ HashMap, ListBuffer }
import java.io.{ BufferedReader, FileReader }

// import scala.math.sqrt

class StatsProcessor(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          tableExporter: TableExporter,
                          spreadsheetExporter: SpreadsheetExporter,
                          exporterFileNames: HashMap[Exporter, String],
                          out: java.io.PrintWriter)
  extends TableExporter(modelFileName, initialDims, protocol, out) with PostProcessor
{
  type Measurements = ListBuffer[List[Double]]
  type DataPerStep = HashMap[Int, Measurements]
  type Data = HashMap[List[Any], DataPerStep]

  override def writeExperimentHeader() {
    val headers = protocol.valueSets(0).map(_.variableName) ::: "[step]" :: protocol.metrics.map(m => m + "-mean")
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
        writeExperimentHeader()
        val averages = new HashMap[List[Any], HashMap[Int, List[Double]]]()
        for ((params, runData) <- d) {
          // println(params)
          for ((step, values) <- runData) {
            // println(f"${step}: ${values}")
            if (!averages.contains(params)) {
              averages(params) = new HashMap[Int, List[Double]]()
            }
            val stepAverages = new ListBuffer[Double]()
            val numMetrics = values(0).length
            for (i <- 0 until numMetrics) {
              var currentTotal = 0.0
              for (value <- values) {
                currentTotal += value(i)
                // println(f"${value} ${i}, ${value(i)}")
              }
              stepAverages += currentTotal.toDouble / values.length
            }
            averages(params)(step) = stepAverages.toList
            writeTableRow(params, averages(params)(step), step)
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
      Some(extractFromSpreadsheet(exporterFileNames(spreadsheetExporter)))
    } else {
      None
    }
  }

  private def extractFromTable(fileName: String): Data = {
    val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
    var line = ""
    var row = 0
    var numVary = 0 // start at -1 since runNumber is always the leftmost column
    val data = new HashMap[List[Any], DataPerStep]() // use a hashmap because we don't know how many runs there are
    while ({line = bufferedReader.readLine; line} != null) {
      if (row == 6) {
        val parsed = line.split(",")
        while (!parsed(numVary).contains("[step]")) {
          numVary += 1
        }
      }
      if (row > 6) {
        val parsed = line.split(",").map(_.split("\"")(1).toDouble).toList
        val params = parsed.slice(1,numVary).toList
        val step = parsed(numVary).toInt
        if (!data.contains(params)) {
          data(params) = new HashMap[Int, Measurements]()
        }
        if (!data(params).contains(step)) {
          data(params)(step) = new ListBuffer[List[Double]]()
        }
        data(params)(step) += parsed.takeRight(parsed.length - (1 + numVary))
      }
      row += 1
    }
    bufferedReader.close()
    data
  }

  private def extractFromSpreadsheet(file: String): Data = {
    val data = new HashMap[List[Any], DataPerStep]()
    data
  }
}