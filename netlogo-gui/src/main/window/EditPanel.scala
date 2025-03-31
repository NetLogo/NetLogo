// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JPanel

import org.nlogo.core.I18N
import org.nlogo.swing.{ OptionPane, Transparent }
import org.nlogo.theme.ThemeSync

// This is the contents of an EditDialog, except for the buttons at the bottom (OK/Apply/Cancel).
abstract class EditPanel(target: Editable) extends JPanel with Transparent with ThemeSync {
  def propertyEditors: Seq[PropertyEditor[_]]

  def previewChanged(field: String, value: Option[Any]) { }  // overridden in WorldEditPanel

  def isResizable: Boolean = false

  def apply() {
    propertyEditors.foreach(_.apply())
  }

  def revert() {
    propertyEditors.foreach { editor =>
      editor.revert()
      editor.refresh()
    }
  }

  protected def errorString: Option[String] = None

  def valid: Boolean = {
    errorString match {
      case Some(error) =>
        new OptionPane(this, I18N.gui.get("edit.general.invalidSettings"), error, OptionPane.Options.Ok,
                       OptionPane.Icons.Error)

        false

      case None =>
        true
    }
  }
}
