// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ World, OutputObject }
import org.nlogo.api.{ File, FileMode }

trait FileManager {
  def prefix: String
  @throws(classOf[java.net.MalformedURLException])
  def attachPrefix(filename: String): String
  def setPrefix(newPrefix: String)
  def setPrefix(newPrefix: java.net.URL)
  @throws(classOf[java.io.IOException])
  def eof: Boolean
  def hasCurrentFile: Boolean
  @throws(classOf[java.io.IOException])
  def closeCurrentFile()
  @throws(classOf[java.io.IOException])
  def flushCurrentFile()
  @throws(classOf[java.io.IOException])
  def deleteFile(filename: String)
  @throws(classOf[java.io.IOException])
  def closeAllFiles()
  @throws(classOf[java.io.IOException])
  def fileExists(filePath: String): Boolean
  @throws(classOf[java.io.IOException])
  def openFile(newFileName: String)
  def getFile(newFileName: String): File
  @throws(classOf[java.io.IOException])
  def ensureMode(openMode: FileMode)
  @throws(classOf[java.io.IOException])
  def getErrorInfo: String
  @throws(classOf[java.io.IOException])
  def read(world: World): AnyRef
  @throws(classOf[java.io.IOException])
  def readLine(): String
  @throws(classOf[java.io.IOException])
  def readChars(num: Int): String
  def handleModelChange()
  def writeOutputObject(oo: OutputObject)
}
