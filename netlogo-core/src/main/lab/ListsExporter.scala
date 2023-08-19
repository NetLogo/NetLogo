// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.immutable.{ ListMap, SortedMap }
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
        val lines = scala.io.Source.fromFile(fileName).getLines.drop(6)
        val runNumbers = lines.next.split(",").tail
        var names = lines.next.split(",")
        while (lines.hasNext && !names.head.contains("[all run data]") &&
               !names.head.contains("[initial & final values]"))
          names = lines.next.split(",")
        if (!lines.hasNext) return
        names = names.tail
        var data = Seq[Seq[String]]()
        for (_ <- runNumbers)
          data = data :+ Seq[String]()
        while (lines.hasNext) {
          val line = lines.next.split(",").tail
          for (i <- 0 until line.length)
            data(i) = data(i) :+ line(i)
        }
        out.print("[reporter],[run number],[step]")
        for (i <- 0 until data.map(_.map(_.split(" ").length)).flatten.max) {
          out.print(s",[$i]")
        }
        out.println()
        for (i <- 0 until runNumbers.length) {
          for (j <- 0 until data(i).length) {
            if (data(i)(j).contains("[")) {
              out.print(names(i) + "," + runNumbers(i) + "," + j + "," +
                data(i)(j).replaceAll("[\"\\[\\]]", "").replace(" ", ","))
              out.println()
            }
          }
        }
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
                map = map.updated(runKey, map(runKey).updated(header(i), SortedMap[Int, String]()))
              map = map.updated(runKey, map(runKey)
                       .updated(header(i), map(runKey)(header(i))
                       .updated(stepKey, els(i).replaceAll("[\"\\[\\]]", "").replace(" ", ","))))
            }
          }
        }
        out.print("[reporter],[run number],[step]")
        for (i <- 0 until map.map(_._2.map(_._2.map(_._2.split(",").length)).flatten).flatten.max) {
          out.print(s",[$i]")
        }
        out.println()
        for (run <- map) {
          for (name <- run._2) {
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