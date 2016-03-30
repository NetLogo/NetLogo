// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

trait ExtendableWorkspace {
  def setProfilingTracer(tracer: org.nlogo.nvm.Tracer): Unit
  def compilerTestingMode: Boolean
  @throws(classOf[java.io.IOException])
  def getSource(filename: String): String
  def profilingEnabled: Boolean
  def fileManager: org.nlogo.nvm.FileManager
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String
  def warningMessage(message: String): Boolean
  def readFromString(path: String): AnyRef
}
