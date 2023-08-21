// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.core.WorldDimensions
import scala.collection.mutable.Seq

class ListsExporter(modelFileName: String,
                          initialDims: WorldDimensions,
                          protocol: LabProtocol,
                          out: java.io.PrintWriter)
  extends Exporter(modelFileName, initialDims, protocol, out)
{
  def finish() {
    writeExportHeader()
    val lines = scala.io.Source.fromFile(fileName).getLines.drop(6)
    val header = lines.next.split(",")
    val stepIndex = header.indexWhere(_.contains("[step]"))
    val parameterIndices = 1 until stepIndex
    // (reporter, run, parameters, step, data)
    var sortedLines = Seq[(String, Int, String, Int, String)]()
    for (line <- lines) {
      val els = line.split(",")
      for (i <- stepIndex + 1 until els.length) {
        // only add line if reporter returned a list
        if (els(i).contains("[")) {
          sortedLines = sortedLines :+ ((header(i),
                                          els(0).replaceAll("\\D", "").toInt,
                                          parameterIndices.map(els(_)).mkString(","),
                                          els(stepIndex).replaceAll("\\D", "").toInt,
                                          els(i).replaceAll("[\"\\[\\]]", "").replace(" ", ",")))
        }
      }
    }
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
    out.close()
  }

  override def experimentCompleted() { finish() }
  override def experimentAborted() { finish() }
}