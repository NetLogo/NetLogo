// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.io.File
import java.nio.file.Files

object Utils {

  def getStackTrace(throwable: Throwable): String = {
    val stringWriter = new java.io.StringWriter
    val printWriter = new java.io.PrintWriter(stringWriter)
    throwable.printStackTrace(printWriter)
    // I don't know if this flushing is ever necessary, but I'm having an intermittent issue (ticket
    // #847) with incomplete stack traces (first line only), so I'm adding these flush() calls on
    // the off chance that could conceivably fix it - ST 12/15/09
    printWriter.flush()
    stringWriter.flush()
    stringWriter.toString.replace('\t', ' ')
  }

  ///

  def escapeSpacesInURL(url: String): String =
    url.flatMap{case ' ' => "%20"
                case c => c.toString}
  def unescapeSpacesInURL(url: String): String =
    url.replaceAll("%20", " ")

  ///

  // separate method with configurable bufferSize for easy testing with ScalaCheck.
  // for now we can't just use a default argument because most of our callers
  // are from Java - ST 12/22/09
  private[util] def reader2String(reader: java.io.Reader, bufferSize: Int): String = {
    assert(bufferSize > 0)
    val sb = new StringBuilder
    val buffer = Array.fill(bufferSize)('\u0000')
    Iterator.continually(reader.read(buffer))
      .takeWhile(_ != -1)
      .foreach(sb.appendAll(buffer, 0, _))
    reader.close()
    sb.toString
  }

  def isSymlink(file: File): Boolean = {
    Files.isSymbolicLink(file.toPath)
  }
}
