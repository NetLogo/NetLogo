package org.nlogo.api

object File {
  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }
  def restoreLines(s: String): String =
    if(s.size < 2)
      s
    else if(s.head == '\\')
      s.tail.head match {
        case 'n' => '\n' + restoreLines(s.tail.tail)
        case '\\' => '\\' + restoreLines(s.tail.tail)
        case '"' => '"' + restoreLines(s.tail.tail)
        case _ =>
          sys.error("invalid escape sequence in \"" + s + "\"")
      }
    else s.head + restoreLines(s.tail)
  def isValidName(name: String) =
    Option(name).exists(_.nonEmpty)
}

abstract class File {
  import File._
  var eof = false
  var pos = 0L
  var reader: java.io.BufferedReader = null
  var mode: FileMode = FileMode.NONE

  // abstract methods
  def getPrintWriter: java.io.PrintWriter
  @throws(classOf[java.io.IOException])
  def open(mode: FileMode)
  @throws(classOf[java.io.IOException])
  def print(str: String)
  @throws(classOf[java.io.IOException])
  def println(line: String)
  @throws(classOf[java.io.IOException])
  def println()
  def flush()
  @throws(classOf[java.io.IOException])
  def close(ok: Boolean)
  @throws(classOf[java.io.IOException])
  def getInputStream: java.io.InputStream
  def getAbsolutePath: String
  def getPath: String

  @throws(classOf[java.io.IOException])
  def readFile(): String = {
    if (reader == null)
      open(FileMode.READ)
    org.nlogo.util.Utils.reader2String(reader)
  }
}
