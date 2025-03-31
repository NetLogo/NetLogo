// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Window
import java.awt.event.{ WindowAdapter, WindowEvent }

import org.nlogo.api.CompilerServices
import org.nlogo.editor.Colorizer
import org.nlogo.theme.ThemeSync

// The name "canceled" describes the value returned: did the user cancel the dialog or not?  Calling
// code may need to know this, for example if we are creating an object and not just editing an
// existing one, canceling the dialog should cause the new object to be discarded.  If the return
// value is false, then the user's edits have been applied to the object edited (the "target").
// - ST 2/24/10

// There are two different methods because the JDialog created needs a parent, and JDialog has
// two different constructors for the two different possible parent types. - ST 2/24/10

class EditDialogFactory(compiler: CompilerServices, val colorizer: Colorizer) extends ThemeSync {
  private var dialog: Option[EditDialog] = None

  // used for modal dialog
  def canceled(window: Window, target: Editable, useTooltips: Boolean): Boolean = {
    (new EditDialog(window, target, useTooltips, true) {
      override def getPreferredSize = limit(super.getPreferredSize)
    }).canceled
  }

  // used for non-modal dialog
  def create(window: Window, target: Editable, finish: (Boolean) => Unit, useTooltips: Boolean): Unit = {
    dialog = Some(
      new EditDialog(window, target, useTooltips, false) {
        override def getPreferredSize = limit(super.getPreferredSize)

        addWindowListener(new WindowAdapter {
          override def windowClosed(e: WindowEvent): Unit = {
            finish(dialog.exists(!_.canceled))
          }
        })
      }
    )
  }

  def clearDialog(): Unit = {
    dialog.foreach(_.abort)
    dialog = None
  }

  override def syncTheme(): Unit = {
    dialog.foreach(_.syncTheme())
  }
}
