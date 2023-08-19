package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.{ HashMap, ListBuffer }
import org.nlogo.api.{ CSV, Dump }

// import scala.math.sqrt

class StatsProcessor(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          tableExporter: TableExporter,
                          spreadsheetExporter: SpreadsheetExporter,
                          exporterFileNames: HashMap[Exporter, String],
                          out: java.io.PrintWriter)
  extends PostProcessor(tableExporter, spreadsheetExporter, exporterFileNames)
{
  val csv = new CSV({
    // boxed integers are used here, but illegal logoObjects -- NP 2018-02-23
    case i: java.lang.Integer => i.toString
    case x => Dump.logoObject(x.asInstanceOf[AnyRef], false, true)
  })

  def writeExperimentHeader() {
    val headers = protocol.valueSets(0).map(_.variableName) ::: "[step]" :: protocol.metrics.map(m => m + "-mean")
    out.println(headers.map(csv.header).mkString(","))
    out.flush()
  }

  override def process: Unit = {
    val data = extractData()
    data match {
      case Some(d) => {
        writeExperimentHeader()
        val averages = new HashMap[List[Any], HashMap[Int, List[Double]]]()
        for ((params, runData) <- d) {
          // println(params)
          for ((tick, values) <- runData) {
            // println(f"${tick}: ${values}")
            if (!averages.contains(params)) {
              averages(params) = new HashMap[Int, List[Double]]()
            }

            val tickAverages = new ListBuffer[Double]()
            val numMetrics = values(0).length
            for (i <- 0 until numMetrics) {
              var currentTotal = 0.0
              for (value <- values) {
                currentTotal += value(i)
                // println(f"${value} ${i}, ${value(i)}")
              }
              tickAverages += currentTotal.toDouble / values.length
            }
            averages(params)(tick) = tickAverages.toList
            val writeValues = (params :+ tick) ::: tickAverages.toList
            out.println(writeValues.map(csv.data).mkString(","))
            out.flush()
          }
        }
      }
      case None =>
    }
  }
}