// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import org.nlogo.api.Dump
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.LabInterface.ProgressListener

// abstract superclass of SpreadsheetExporter and TableExporter.
// subclasses must implement ProgressListener's methods.

abstract class Exporter(modelFileName: String,
                        initialDims: WorldDimensions,
                        protocol: LabProtocol,
                        versionString: String,
                        out: java.io.PrintWriter)
  extends ProgressListener
{
  def writeExportHeader() {
    out.println(
      Dump.csv.header(
        "BehaviorSpace results (" + versionString + ")"))
    out.println(
      Dump.csv.header(modelFileName))
    out.println(
      Dump.csv.header(protocol.name))
    out.println(
      Dump.csv.header(
        new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS Z")
          .format(new java.util.Date)))
    out.println(
      Dump.csv.headerRow(
        Array("min-pxcor", "max-pxcor", "min-pycor", "max-pycor")))
    out.println{
      import initialDims._
      List(minPxcor, maxPxcor, minPycor, maxPycor)
        .map(Dump.csv.number(_))
        .mkString(",")
    }
  }
}
