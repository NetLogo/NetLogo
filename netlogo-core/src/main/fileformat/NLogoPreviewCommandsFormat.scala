// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.util.Implicits.RichString
import org.nlogo.util.Implicits.RichStringLike
import org.nlogo.core.Model
import org.nlogo.api.{ ComponentSerialization, PreviewCommands, Version }

import scala.util.Try

class NLogoPreviewCommandsFormat extends ComponentSerialization[Array[String], NLogoFormat] {
  def componentName: String = "org.nlogo.modelsection.previewcommands"

  override def addDefault = { (m: Model) =>
    m.withOptionalSection[PreviewCommands](componentName, None, PreviewCommands.Default)
  }

  def serialize(m: Model): Array[String] = {
    m.optionalSectionValue[PreviewCommands](componentName) match {
      case Some(PreviewCommands.Default) => Array("")
      case Some(commands) =>
        commands.source.lines.map(_.stripTrailingWhiteSpace).toArray
      case _ => Array("")
    }
  }

  def validationErrors(m: Model) = None

  override def deserialize(commands: Array[String]) = { (m: Model) =>
    Try {
      m.withOptionalSection[PreviewCommands](componentName, Some(PreviewCommands(commands.mkString("\n"))), PreviewCommands.Default)
    }
  }
}

object NLogoThreeDPreviewCommandsFormat extends ComponentSerialization[Array[String], NLogoThreeDFormat] {
  def componentName: String = "org.nlogo.modelsection.previewcommands"

  override def addDefault = { (m: Model) =>
    m.withOptionalSection[PreviewCommands](componentName, None, PreviewCommands.Manual)
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
    Try {
      m.withOptionalSection[PreviewCommands](componentName, Some(PreviewCommands(commands.mkString("\n"))), PreviewCommands.Manual)
    }
  }
}
