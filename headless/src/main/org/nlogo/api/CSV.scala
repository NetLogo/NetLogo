// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// This class handles output of CSV files.  Input of CSV files is handled elsewhere, in
// ImportLexer. - ST 7/22/03, 2/13/08

import java.util.{ List => JList }

class CSV(dump: Any => String) {

  def blank = ""

  // this dumps a object in the exporting format:
  // --if the object is a string, all double quotes are doubled and escape
  //   chars properly escaped.
  // --all objects are returned as a string surrounded by double quotes.
  def data(obj: Any) =
    encode(dump(obj))

  def number(d: Double) =
    encode(dump(d: java.lang.Double))

  def number(i: Int) =
    encode(dump(i: java.lang.Integer))

  def dataRow[T](objs: Array[T]) =
    objs.map(data).mkString(",")

  def header(s: String) =
    encode(s)

  def headerRow(strings: Array[String]) =
    strings.map(encode).mkString(",")

  def encode(s: String) = {
    def escape(c: Char) =
      if(c == '"') "\"\"" else c.toString
    val ss = Option(s).getOrElse("null")
    '"' + ss.flatMap(escape) + '"'
  }

  def stringToCSV(writer: java.io.PrintWriter, text: String) {
    val CellWidth = 10000
    val MaxColumns = 2
    var i = 0
    while(i < text.size) {
      var line = new StringBuilder
      var k = 0
      while(k < MaxColumns && i < text.size) {
        val end = (i + CellWidth) min text.size
        line ++= text.substring(i, end)
        i += CellWidth
        k += 1
        if (i < text.size && k < MaxColumns)
          line += ','
      }
      writer.println(data(line.toString))
    }
  }

  def variableNameRow(v: JList[String]): String = {
    import collection.JavaConverters._
    variableNameRow(v.asScala)
  }

  def variableNameRow(names: Seq[String]): String =
    names.map(encode(_).toLowerCase).mkString(",")

}
