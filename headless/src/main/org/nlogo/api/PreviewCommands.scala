package org.nlogo.api

import org.nlogo.util.Implicits.RichString

sealed trait PreviewCommands {
  def source: String
  def description: String
  override def toString = description
}

object PreviewCommands {
  case object Manual extends PreviewCommands {
    override val source = "need-to-manually-make-preview-for-this-model"
    override val description = "Manually make preview"
  }
  trait Compilable extends PreviewCommands
  case object Default extends Compilable {
    override val source = "setup repeat 75 [ go ]"
    override val description = "Default preview commands"
  }
  case class Custom(override val source: String) extends Compilable {
    override val description = "Custom preview commands"
  }
  def apply(source: String): PreviewCommands = {
    val strippedSource = source.stripTrailingWhiteSpace
    strippedSource.toLowerCase match {
      case "" => Default
      case Default.source => Default
      case s if s startsWith Manual.source => Manual
      case _ => Custom(strippedSource)
    }
  }
  def DEFAULT = Default // for access through GUIWorkspaceJ.java
}
