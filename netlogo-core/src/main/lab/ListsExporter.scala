// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{ LabListsExporterFormat, LabProtocol }
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.Seq

class ListsExporter(modelFileName: String,
                    initialDims: WorldDimensions,
                    protocol: LabProtocol,
                    out: java.io.PrintWriter,
                    in: LabListsExporterFormat.Format)
  extends Exporter(modelFileName, initialDims, protocol, out)
{
  def finish() {
    writeExportHeader()
    in match {
      case LabListsExporterFormat.SpreadsheetFormat(fileName) => {
        var lines = scala.io.Source.fromFile(fileName).getLines.drop(6)
        val runNumbers = lines.next.split(",").tail.toList
        val parameters = lines.takeWhile(x => !x.split(",")(0).contains("[reporter]") &&
                                              !x.split(",")(0).contains("[total steps]"))
                              .map(_.split(",").filter(!_.isEmpty).toList).toList
        val runWidth = runNumbers.length / runNumbers.distinct.length
        lines = lines.dropWhile(x => !x.split(",")(0).contains("[all run data]") &&
                                     !x.split(",")(0).contains("[final value]"))
        out.print(csv.header("[reporter]") + "," + csv.header("[run number]"))
        for (parameter <- parameters) {
          out.print("," + parameter(0))
        }
        out.print("," + csv.header("[step]"))
        if (!lines.hasNext) {
          out.println()
          out.close()
          return
        }
        val reporters = lines.next.split(",").tail.toList
        val data = lines.map(_.split(",").tail.toList).toList
        val count = data.map(_.filter(_.contains("[")).map(_.split(" ").length)).flatten
        if (count.length > 0) {
          for (i <- 0 until count.max) {
            out.print("," + csv.header(s"[$i]"))
          }
        }
        out.println()
        for (i <- 0 until runNumbers.length by runWidth) {
          for (j <- 1 until runWidth) {
            if (data(0)(i + j).contains("[")) {
              for (k <- 0 until data.length) {
                out.print(s"${reporters(i + j)},${runNumbers(i)},")
                if (parameters.length > 0)
                  out.print(parameters.map(_(i / runWidth + 1)).mkString(",") + ",")
                out.println(s"${data(k)(i)},${data(k)(i + j).replaceAll("[\"\\[\\]]", "").split(" ").mkString(",")}")
              }
            }
          }
        }
      }
      case LabListsExporterFormat.TableFormat(fileName) => {
        var lines = scala.io.Source.fromFile(fileName).getLines
        var header: Array[String] = null
        val first = lines.next
        if (first.contains("BehaviorSpace results")) {
          lines = lines.drop(5)
          header = lines.next.split(",")
        }
        else
          header = first.split(",")
        val stepIndex = header.indexWhere(_.contains("[step]"))
        val parameterIndices = 1 until stepIndex
        // (reporter, run, parameters, step, data)
        var sortedLines = Seq[(String, Int, String, Int, String)]()
        for (line <- lines) {
          val els = line.split(",")
          for (i <- stepIndex + 1 until els.length) {
            if (els(i).contains("[")) {
              sortedLines = sortedLines :+ ((header(i),
                                             els(0).replaceAll("\\D", "").toInt,
                                             parameterIndices.map(els(_)).mkString(","),
                                             els(stepIndex).replaceAll("\\D", "").toInt,
                                             els(i).replaceAll("[\"\\[\\]]", "").replace(" ", ",")))
            }
          }
        }
        out.print(csv.header("[reporter]") + "," + csv.header("[run number]"))
        parameterIndices.foreach(i => out.print("," + header(i)))
        out.print("," + csv.header("[step]"))
        if (sortedLines.length > 0) {
          // sort lines by run, then by reporter, then by step
          sortedLines = sortedLines.sortWith((a, b) =>
            if (a._2 == b._2) {
              if (a._1 == b._1) {
                a._4 < b._4
              } else header.indexOf(a._1) < header.indexOf(b._1)
            } else a._2 < b._2
          )
          val count = sortedLines.filter(_._5.contains(",")).map(_._5.split(",").length)
          if (count.length > 0) {
            for (i <- 0 until sortedLines.map(_._5.split(",").length).max) {
              out.print("," + csv.header(s"[$i]"))
            }
          }
          out.println()
          sortedLines.foreach(line => out.println(line.productIterator.mkString(",")))
        }
        else {
          out.println()
        }
      }
    }
    out.close()
  }

  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
}
