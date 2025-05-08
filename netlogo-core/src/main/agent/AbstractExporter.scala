// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.io.{ IOException, PrintWriter }

import
  org.nlogo.{ core, api },
    core.FileMode,
    api.{ Dump, Exceptions, Version },
      Dump.csv,
      Exceptions.ignoring

object AbstractExporter {

  def exportHeader(writer: PrintWriter, tpe: String, modelFileName: String, extraHeader: String): Unit = {
    import writer.println
    println(csv.header("export-" + tpe + " data (" + Version.version + ")"))
    println(csv.header(modelFileName))
    if(extraHeader.nonEmpty)
      println(csv.header(extraHeader))
    // date & time
    val currentDate = new java.util.Date // Use Date to avoid month bug in GregorianCalendar
    val dateFormatter = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS Z")
    println(csv.header(dateFormatter.format(currentDate)))
    println()
  }

  def exportWithHeader(writer: PrintWriter, tpe: String, modelFileName: String, extraHeader: String)
                      (exportBody: (PrintWriter) => Unit): Unit = {
    exportHeader(writer, tpe, modelFileName, extraHeader)
    exportBody(writer)
  }

}

abstract class AbstractExporter(filename: String) {

  @throws(classOf[IOException])
  def `export`(writer: PrintWriter): Unit // abstract

  @throws(classOf[IOException])
  def `export`(tpe: String, modelFileName: String, extraHeader: String): Unit = {
    val file = new org.nlogo.api.LocalFile(filename)
    try {
      file.open(FileMode.Write)
      val writer = file.getPrintWriter
      AbstractExporter.exportWithHeader(writer, tpe, modelFileName, extraHeader)(`export`)
    }
    finally ignoring(classOf[IOException]) {
      file.close(false)
    }
  }

}
