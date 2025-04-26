// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{LabExporterVersion, LabProtocol}
import org.nlogo.api.{ CSV, Dump, Version }
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.LabInterface.ProgressListener

object Exporter {
  val NUM_HEADER_ROWS = 6
}

// abstract superclass of SpreadsheetExporter and TableExporter.
// subclasses must implement ProgressListener's methods.

abstract class Exporter(modelFileName: String,
                        initialDims: WorldDimensions,
                        protocol: LabProtocol,
                        out: java.io.PrintWriter,
                        exporterType: String)
  extends ProgressListener
{
  val csv = new CSV({
    // boxed integers are used here, but illegal logoObjects -- NP 2018-02-23
    case i: java.lang.Integer => i.toString
    case x => Dump.logoObject(x.asInstanceOf[AnyRef], false, true)
  })
  def writeExportHeader(): Unit = {
    out.println(
      csv.headerRow(
        Array("BehaviorSpace results (" + Version.version + ")", exporterType + " version " + LabExporterVersion.version)))
    out.println(
      csv.header(modelFileName))
    out.println(
      csv.header(protocol.name))
    out.println(
      csv.header(
        new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS Z")
          .format(new java.util.Date)))
    out.println(
      csv.headerRow(
        Array("min-pxcor", "max-pxcor", "min-pycor", "max-pycor")))
    out.println{
      import initialDims._
      List(minPxcor, maxPxcor, minPycor, maxPycor)
        .map(csv.number(_))
        .mkString(",")
    }
  }
}
