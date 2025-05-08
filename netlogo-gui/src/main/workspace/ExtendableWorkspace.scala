// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.StringReader

trait ExtendableWorkspace extends StringReader {
  def setProfilingTracer(tracer: org.nlogo.nvm.Tracer): Unit
  def compilerTestingMode: Boolean
  def profilingEnabled: Boolean
  def fileManager: org.nlogo.nvm.FileManager
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String
  def warningMessage(message: String): Boolean
}
