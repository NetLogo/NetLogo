// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.Seq

object ListsExporter {
  trait Format
  case class SpreadsheetFormat(fileName: String) extends Format
  case class TableFormat(fileName: String) extends Format
}

class ListsExporter(modelFileName: String,
                    initialDims: WorldDimensions,
                    protocol: LabProtocol,
                    out: java.io.PrintWriter,
                    in: ListsExporter.Format)
  extends Exporter(modelFileName, initialDims, protocol, out)
{
  def finish() {
    writeExportHeader()
    in match {
      case ListsExporter.SpreadsheetFormat(fileName) => {
        var lines = scala.io.Source.fromFile(fileName).getLines.drop(6)
        val runNumbers = lines.next.split(",").tail.toList
        val parameters = lines.takeWhile(x => !x.split(",")(0).contains("[reporter]") &&
                                              !x.split(",")(0).contains("[step]"))
                              .map(_.split(",").filter(!_.isEmpty).toList).toList
        val runWidth = runNumbers.length / parameters(0).tail.length
        lines = lines.dropWhile(x => !x.split(",")(0).contains("[all run data]") &&
                                     !x.split(",")(0).contains("[initial & final values]"))
        val reporters = lines.next.split(",").tail.toList
        val data = lines.map(_.split(",").tail.toList).toList
        out.print("[reporter],[run number]")
        for (parameter <- parameters) {
          out.print("," + parameter(0))
        }
        out.print(",[step]")
        for (i <- 0 until data.map(_.map(_.split(" ").length)).flatten.max) {
          out.print(s",[$i]")
        }
        out.println()
        for (i <- 0 until runNumbers.length by runWidth) {
          for (j <- 0 until runWidth) {
            if (data(0)(i + j).contains("[")) {
              for (k <- 0 until data.length) {
                out.println(s"${reporters(i + j)},${runNumbers(i)},${parameters.map(_(i / runWidth + 1)).mkString(",")}" +
                s",$k,${data(k)(i + j).replaceAll("[\"\\[\\]]", "").split(" ").mkString(",")}")
              }
            }
          }
        }
      }
      case ListsExporter.TableFormat(fileName) => {
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
        if (sortedLines.length > 0) {
          // sort lines by run, then by reporter, then by step
          sortedLines = sortedLines.sortWith((a, b) =>
            if (a._2 == b._2) {
              if (a._1 == b._1) {
                a._4 < b._4
              } else header.indexOf(a._1) < header.indexOf(b._1)
            } else a._2 < b._2
          )
          out.print("[reporter],[run number]")
          parameterIndices.foreach(i => out.print("," + header(i)))
          out.print(",[step]")
          for (i <- 0 until sortedLines.map(_._5.split(",").length).max) {
            out.print(s",[$i]")
          }
          out.println()
          sortedLines.foreach(line => out.println(line.productIterator.mkString(",")))
        }
      }
    }
    out.close()
  }

  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
}
