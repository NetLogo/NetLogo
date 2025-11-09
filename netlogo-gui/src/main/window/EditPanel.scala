// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.GridBagLayout
import javax.swing.JPanel

import org.nlogo.core.I18N
import org.nlogo.swing.{ OptionPane, Transparent }
import org.nlogo.theme.ThemeSync

// This is the contents of an EditDialog, except for the buttons at the bottom (OK/Apply/Cancel).
abstract class EditPanel(target: Editable) extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
  // allows EditDialog to reset its minimum size if this panel's minimum size changes
  private var layoutListener: () => Unit = () => {}

  def setLayoutListener(listener: () => Unit): Unit = {
    layoutListener = listener
  }

  def propertyEditors: Seq[PropertyEditor[?]]

  def isResizable: Boolean = false

  // allows slider edit panel to indicate whether the vertical property was changed (Isaac B 5/1/25)
  def apply(swapSizes: Boolean = false): Unit = {
    propertyEditors.foreach(_.apply())
  }

  def revert(): Unit = {
    propertyEditors.foreach { editor =>
      editor.revert()
      editor.refresh()
    }
  }

  def changed: Boolean =
    propertyEditors.exists(_.changed)

  def valid(silent: Boolean): Boolean = {
    propertyEditors.find(editor => editor.get.isEmpty && !editor.handlesOwnErrors) match {
      case Some(editor) =>
        if (!silent) {
          new OptionPane(this, I18N.gui.get("edit.general.invalidSettings"),
                         I18N.gui.getN("edit.general.invalidValue", editor.accessor.name), OptionPane.Options.Ok,
                         OptionPane.Icons.Error)
        }

        false

      case None =>
        (target.errorString match {
          case Some(error) =>
            if (!silent) {
              new OptionPane(this, I18N.gui.get("edit.general.invalidSettings"), error, OptionPane.Options.Ok,
                             OptionPane.Icons.Error)
            }

            false

          case None =>
            true
        })
    }
  }

  override def doLayout(): Unit = {
    super.doLayout()

    layoutListener()
  }

  // helper for any EditPanel that has GUI components that aren't PropertyEditors (Isaac B 3/31/25)
  def syncExtraComponents(): Unit = {}

  override def syncTheme(): Unit = {
    propertyEditors.foreach(_.syncTheme())

    syncExtraComponents()
  }

  // used by GUI tests to automate widget creation (Isaac B 11/8/25)
  def autoFill(): Boolean =
    true
}
