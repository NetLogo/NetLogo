// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.MalformedURLException

import org.nlogo.api.WorkspaceContext
import org.nlogo.nvm.{ FileManager, Tracer }

trait ExtendableWorkspace {
  def setProfilingTracer(tracer: Tracer): Unit
  def compilerTestingMode: Boolean
  def profilingEnabled: Boolean
  def fileManager: FileManager
  @throws(classOf[MalformedURLException])
  def attachModelDir(filePath: String): String
  def warningMessage(message: String): Boolean
  def readFromString(path: String): AnyRef
  def workspaceContext: WorkspaceContext
}
