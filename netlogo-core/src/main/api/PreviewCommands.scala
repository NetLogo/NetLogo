// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ I18N, Model }
import org.nlogo.util.Implicits.RichString

/**
 * A model's "preview commands" are the commands that are used to
 * generate the preview that appears in the Models Library dialog,
 * the NetLogo website (http://ccl.northwestern.edu/netlogo/models/),
 * the Modeling Commons (http://www.modelingcommons.org/) and potentially
 * other places. By default, they're just `setup repeat 75 [ go ]` but
 * they can be customized. Some models require manually generated previews.
 *
 * Preview commands are always run in a fresh headless workspace with
 * `random-seed 0` called before opening the model. The `startup`
 * procedure, if present, runs before the preview commands.
 */
sealed trait PreviewCommands extends ModelSections.ModelSaveable {
  def source: String
  def description: String
  override def toString = description
  override def updateModel(m: Model): Model = {
    m.withOptionalSection[PreviewCommands]("org.nlogo.modelsection.previewcommands", Some(this), PreviewCommands.Default)
  }
}

object PreviewCommands {
  case object Manual extends PreviewCommands {
    override val source = "need-to-manually-make-preview-for-this-model"
    override val description = I18N.gui.get("tools.previewCommands.manual")
  }
  trait Compilable extends PreviewCommands
  case object Default extends Compilable {
    override val source = "setup repeat 75 [ go ]"
    override val description = I18N.gui.get("tools.previewCommands.default")
  }
  case class Custom(override val source: String) extends Compilable {
    override val description = I18N.gui.get("tools.previewCommands.custom")
  }
  def apply(source: List[String]): PreviewCommands = {
    apply(source.mkString(""))
  }
  def apply(source: String): PreviewCommands = {
    val strippedSource = source.stripTrailingWhiteSpace
    strippedSource.toLowerCase match {
      case ""                               => Default
      case Default.source                   => Default
      case s if s.startsWith(Manual.source) => Manual
      case _                                => Custom(strippedSource)
    }
  }
  def DEFAULT = Default // for access through GUIWorkspace.java
}
