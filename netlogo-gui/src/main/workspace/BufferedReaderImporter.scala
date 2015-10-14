// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

abstract class BufferedReaderImporter(val filename: String) {
  @throws(classOf[java.io.IOException])
  def doImport(reader: java.io.BufferedReader)
}
