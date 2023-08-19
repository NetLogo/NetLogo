// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.immutable.{ ListMap, SortedMap }

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
        // scala.io.Source.fromFile(fileName).getLines
      }
      case ListsExporter.TableFormat(fileName) => {
        val lines = scala.io.Source.fromFile(fileName).getLines.drop(6)
        val header = lines.next.split(",")
        val runIndex = header.indexWhere(_.contains("[run number]"))
        val stepIndex = header.indexWhere(_.contains("[step]"))
        var map = SortedMap[Int, ListMap[String, SortedMap[Int, String]]]()
        for (line <- lines) {
          val els = line.split(",")
          for (i <- 0 until els.length) {
            if (els(i).contains("[")) {
              val runKey = els(runIndex).replaceAll("\\D", "").toInt
              val stepKey = els(stepIndex).replaceAll("\\D", "").toInt
              if (!map.contains(runKey))
                map = map.updated(runKey, ListMap[String, SortedMap[Int, String]]())
              if (!map(runKey).contains(header(i)))
                map(runKey) = map(runKey).updated(header(i), SortedMap[Int, String]())
              map(runKey)(header(i)) = map(runKey)(header(i))
                                         .updated(stepKey, els(i).replaceAll("[\"\\[\\]]", "").replace(" ", ","))
            }
          }
        }
        out.print("[reporter],[run number],[step]")
        for (i <- 0 until map.map(_._2.map(_._2.map(_._2.split(",").length)).flatten).flatten.max) {
          out.print(s",[$i]")
        }
        out.println()
        for (run <- map) {
          for (name <- run._2.toSeq.reverse) {
            for (step <- name._2) {
              out.print(name._1 + "," + run._1 + "," + step._1 + "," + step._2)
              out.println()
            }
          }
        }
      }
    }
    out.close()
  }

  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
}