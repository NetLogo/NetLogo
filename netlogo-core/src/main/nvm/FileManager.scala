// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ World, OutputObject }
import org.nlogo.core.{ File, FileMode }

trait FileManager {
  @throws(classOf[java.net.MalformedURLException])
  def attachPrefix(filename: String): String
  def prefix: String
  def setPrefix(newPrefix: String): Unit
  def setPrefix(newPrefix: java.net.URL): Unit
  @throws(classOf[java.io.IOException])
  def eof: Boolean
  def currentFile: Option[File]
  def findOpenFile(fileName: String): Option[File]
  def hasCurrentFile: Boolean
  @throws(classOf[java.io.IOException])
  def closeCurrentFile(): Unit
  @throws(classOf[java.io.IOException])
  def flushCurrentFile(): Unit
  @throws(classOf[java.io.IOException])
  def deleteFile(filename: String): Unit
  @throws(classOf[java.io.IOException])
  def closeAllFiles(): Unit
  @throws(classOf[java.io.IOException])
  def fileExists(filePath: String): Boolean
  @throws(classOf[java.io.IOException])
  def openFile(newFileName: String): Unit
  def getFile(newFileName: String): File
  @throws(classOf[java.io.IOException])
  def ensureMode(openMode: FileMode): Unit
  @throws(classOf[java.io.IOException])
  def getErrorInfo: String
  @throws(classOf[java.io.IOException])
  def read(world: World): AnyRef
  @throws(classOf[java.io.IOException])
  def readLine(): String
  @throws(classOf[java.io.IOException])
  def readChars(num: Int): String
  def handleModelChange(): Unit
  def writeOutputObject(oo: OutputObject): Unit
}
