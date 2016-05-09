// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.util.Implicits.RichString
import org.nlogo.util.Implicits.RichStringLike
import org.nlogo.core.Model
import org.nlogo.api.{ ComponentSerialization, PreviewCommands, Version }


// NOTE: Preview commands is both a field of the model as well as
// an optional section.
class NLogoPreviewCommandsFormat extends ComponentSerialization[Array[String], NLogoFormat] {
  def componentName: String = "org.nlogo.modelsection.previewcommands"

  override def addDefault = { (m: Model) =>
    m.copy(previewCommands = Some(PreviewCommands.Default.source))
      .withOptionalSection[PreviewCommands](componentName, None, PreviewCommands.Default)
  }

  def serialize(m: Model): Array[String] =
    m.optionalSectionValue[PreviewCommands](componentName) match {
      case Some(PreviewCommands.Default) => Array("")
      case Some(commands) => Array(
        commands.source.stripTrailingWhiteSpace + "\n")
      case _ => Array("")
    }

  def validationErrors(m: Model) = None

  override def deserialize(commands: Array[String]) = { (m: Model) =>
    m.copy(previewCommands =
      if (commands.nonEmpty) Some(commands.mkString("\n")) else None)
      .withOptionalSection[PreviewCommands](componentName, Some(PreviewCommands(commands.toList)), PreviewCommands.Default)
  }
}

object NLogoThreeDPreviewCommandsFormat extends ComponentSerialization[Array[String], NLogoThreeDFormat] {
  def componentName: String = "org.nlogo.modelsection.previewcommands"

  override def addDefault = { (m: Model) =>
    m.copy(previewCommands = Some(PreviewCommands.Default.source))
      .withOptionalSection[PreviewCommands](componentName, None, PreviewCommands.Manual)
  }

  def serialize(m: Model): Array[String] = {
    m.optionalSectionValue[PreviewCommands](componentName) match {
      case Some(PreviewCommands.Default) => Array("")
      case Some(commands) =>
        commands.source.lines.map(_.stripTrailingWhiteSpace).toArray
      case _ => PreviewCommands.Manual.source.lines.map(_.stripTrailingWhiteSpace).toArray
    }
  }

  def validationErrors(m: Model) = None

  override def deserialize(commands: Array[String]) = { (m: Model) =>
    m.copy(previewCommands = if (commands.nonEmpty) Some(commands.mkString("\n")) else None)
     .withOptionalSection[PreviewCommands](componentName, Some(PreviewCommands(commands.mkString("\n"))), PreviewCommands.Manual)
  }
}
