// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Model
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
  val compilable: Boolean = true
  val source: String

  override def updateModel(m: Model): Model = {
    m.withOptionalSection[PreviewCommands]("org.nlogo.modelsection.previewcommands", Some(this), PreviewCommands.Default)
  }
}

object PreviewCommands {
  val ManualSource = "need-to-manually-make-preview-for-this-model"

  case class Manual(override val source: String, override val compilable: Boolean) extends PreviewCommands

  object Manual {
    val Empty = Manual(ManualSource, false)
  }

  case object Default extends PreviewCommands {
    override val source = "setup repeat 75 [ go ]"
  }

  case class Custom(override val source: String) extends PreviewCommands

  def apply(source: List[String]): PreviewCommands = {
    apply(source.mkString(""))
  }

  def apply(source: String, manual: Boolean = false): PreviewCommands = {
    val strippedSource = source.stripTrailingWhiteSpace

    if (manual) {
      Manual(strippedSource, true)
    } else {
      strippedSource.toLowerCase match {
        case ""                              => Default
        case Default.source                  => Default
        case s if s.startsWith(ManualSource) => Manual.Empty
        case _                               => Custom(strippedSource)
      }
    }
  }

  def DEFAULT = Default // for access through GUIWorkspace.java
}
