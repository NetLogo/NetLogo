package org.nlogo.lab

import collection.mutable.{ HashMap, ListBuffer }
import java.io.{ BufferedReader, FileReader }

abstract class PostProcessor(tableExporter: TableExporter,
                            spreadsheetExporter: SpreadsheetExporter,
                            exporterFileNames: HashMap[Exporter, String])
{
    def process(): Unit
    def extractData(): Option[HashMap[List[Any], HashMap[Int, ListBuffer[List[Double]]]]] = {
      if (tableExporter != null) {
        Some(extractFromTable(exporterFileNames(tableExporter)))
      } else if (spreadsheetExporter != null) {
        Some(extractFromSpreadsheet(exporterFileNames(spreadsheetExporter)))
      } else {
        None
      }
    }

    private def extractFromTable(fileName: String): HashMap[List[Any], HashMap[Int, ListBuffer[List[Double]]]] = {
      val bufferedReader: BufferedReader = new BufferedReader(new FileReader(fileName))
      var line = ""
      var row = 0
      var numVary = 0 // start at -1 since runNumber is always the leftmost column
      val data = new HashMap[List[Any], HashMap[Int, ListBuffer[List[Double]]]]() // use a hashmap because we don't know how many runs there are
      while ({line = bufferedReader.readLine; line} != null) {
        if (row == 6) {
          val parsed = line.split(",")
          while (!parsed(numVary).contains("[step]")) {
            numVary += 1
          }
        }
        if (row > 6) {
          val parsed = line.split(",").map(_.split("\"")(1).toDouble).toList
          // println(line)
          // println(parsed.length)
          // println(parsed.foreach{println})
          val params = parsed.slice(1,numVary).toList
          val step = parsed(numVary).toInt
          if (!data.contains(params)) {
            data(params) = new HashMap[Int, ListBuffer[List[Double]]]()
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

    private def extractFromSpreadsheet(file: String): HashMap[List[Any], HashMap[Int, ListBuffer[List[Double]]]] = {
      val data = new HashMap[List[Any], HashMap[Int, ListBuffer[List[Double]]]]()
      data
    }
}