package org.nlogo.window

import org.nlogo.api.PreviewCommands

trait PreviewCommandsEditorInterface {
  val title: String
  def getPreviewCommands(modelContent: String, modelPath: String): PreviewCommands
}
