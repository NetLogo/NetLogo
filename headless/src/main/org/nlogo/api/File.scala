// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract class File {
  var eof = false
  var pos = 0L
  var reader: java.io.BufferedReader = null
  var mode: FileMode = FileMode.None

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
      open(FileMode.Read)
    org.nlogo.util.Utils.reader2String(reader)
  }
}
