// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.awt.image.BufferedImage
import org.nlogo.{ api, agent }
import org.nlogo.api.{ Activation => ApiActivation, MersenneTwisterFast }

/**
 * workspace and nvmContext are public vals, even though that violates the org.nlogo.api
 * abstraction, so extensions can go around that if they must.  As the extensions API
 * improves, this should become less often necessary.
 */
class ExtensionContext(val workspace: Workspace, val modelTracker: ModelTracker, val nvmContext: Context)
extends api.Context {
  def getAgent: api.Agent =
    nvmContext.agent

  def getRNG: MersenneTwisterFast =
    nvmContext.job.random

  def getDrawing: BufferedImage =
    workspace.getAndCreateDrawing()

  def world: api.World =
    workspace.world

  @throws(classOf[java.net.MalformedURLException])
  def attachCurrentDirectory(path: String): String =
    workspace.fileManager.attachPrefix(path)

  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String =
    modelTracker.attachModelDir(filePath)

  def importPcolors(image: BufferedImage, asNetLogoColors: Boolean) {
    org.nlogo.agent.ImportPatchColors.doImport(
      image, workspace.world.asInstanceOf[agent.World], asNetLogoColors)
  }

  def activation: ApiActivation =
    nvmContext.activation

}
