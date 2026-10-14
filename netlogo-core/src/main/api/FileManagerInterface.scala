// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait FileManagerInterface {
  def openFromPath(path: String, modelType: ModelType, shouldAutoInstallLibs: Boolean): Unit
}
