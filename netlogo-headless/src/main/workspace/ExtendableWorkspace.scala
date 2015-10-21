// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

trait ExtendableWorkspace {
  def setProfilingTracer(tracer: org.nlogo.nvm.Tracer): Unit
  def compilerTestingMode: Boolean
  def getSource(filename: String): String
  def profilingEnabled: Boolean
  def fileManager: org.nlogo.nvm.FileManager
  def attachModelDir(filePath: String): String
  def warningMessage(message: String): Boolean
  def readFromString(path: String): AnyRef
}
