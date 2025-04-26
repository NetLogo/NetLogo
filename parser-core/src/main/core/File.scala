// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

abstract class File {
  var eof = false
  var pos = 0L
  var reader: java.io.BufferedReader = null
  var mode: FileMode = FileMode.None

  // abstract methods
  def getPrintWriter: java.io.PrintWriter
  @throws(classOf[java.io.IOException])
  def open(mode: FileMode): Unit
  @throws(classOf[java.io.IOException])
  def print(str: String): Unit
  @throws(classOf[java.io.IOException])
  def println(line: String): Unit
  @throws(classOf[java.io.IOException])
  def println(): Unit
  def flush(): Unit
  @throws(classOf[java.io.IOException])
  def close(ok: Boolean): Unit
  @throws(classOf[java.io.IOException])
  def getInputStream: java.io.InputStream
  def getAbsolutePath: String
  def getPath: String
}
