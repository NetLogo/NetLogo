// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.Window
import java.awt.event.{ WindowAdapter, WindowEvent }

import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.editor.Colorizer
import org.nlogo.window.EditDialogFactoryInterface

// see commentary in EditDialogFactoryInterface

class EditDialogFactory(compiler: CompilerServices, colorizer: Colorizer) extends EditDialogFactoryInterface {
  var dialog: EditDialog = null

  def canceled(window: Window, target: Editable, useTooltips: Boolean) = {
    (new EditDialog(window, target, useTooltips, true, compiler, colorizer) {
      override def getPreferredSize = limit(super.getPreferredSize)
    }).canceled
  }

  def create(window: Window, target: Editable, finish: (Boolean) => Unit, useTooltips: Boolean) = {
    dialog =
      new EditDialog(window, target, useTooltips, false, compiler, colorizer) {
        override def getPreferredSize = limit(super.getPreferredSize)

        addWindowListener(new WindowAdapter {
          override def windowClosed(e: WindowEvent): Unit = {
            finish(dialog != null && !dialog.canceled)
          }
        })
      }
  }

  def getDialog =
    dialog

  def clearDialog() = {
    if (dialog != null) {
      dialog.abort()
      dialog = null
    }
  }

  override def syncTheme(): Unit = {
    if (dialog != null)
      dialog.syncTheme()
  }
}
