package org.nlogo.window

import org.nlogo.core.Model
import org.nlogo.api.PreviewCommands

trait PreviewCommandsEditorInterface {
  val title: String
  def getPreviewCommands(model: Model, modelPath: String): PreviewCommands
}
