// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ KeyboardFocusManager, Toolkit }
import java.awt.datatransfer.DataFlavor
import java.awt.event.{ ActionEvent, KeyEvent }
import java.lang.IllegalStateException
import javax.swing.Action

import org.nlogo.api.Refreshable
import org.nlogo.editor.Actions
import org.nlogo.swing.{ ClipboardUtils, UserAction, WrappedAction },
  UserAction.{ EditCategory, EditClipboardGroup, EditSelectionGroup, KeyBindings },
    KeyBindings.keystroke

object TextMenuActions {
  val CutAction       =
    new WrappedAction(Actions.CutAction, EditCategory, null, EditClipboardGroup, keystroke('X', withMenu = true))
  val CopyAction      =
    new WrappedAction(Actions.CopyAction, EditCategory, null, EditClipboardGroup, keystroke('C', withMenu = true)) {

    override def actionPerformed(e: ActionEvent): Unit = {
      KeyboardFocusManager.getCurrentKeyboardFocusManager.getPermanentFocusOwner match {
        case target: CopyPasteTarget =>
          target.copy()

        case _ =>
          super.actionPerformed(e)
      }
    }
  }
  val PasteAction     = new WrappedPasteAction(Actions.PasteAction)
  val DeleteAction    =
    new WrappedAction(
      Actions.DeleteAction, EditCategory, null, EditClipboardGroup, keystroke(KeyEvent.VK_DELETE))
  val SelectAllAction =
    new WrappedAction(
      Actions.SelectAllAction, EditCategory, null, EditSelectionGroup, keystroke('A', withMenu = true))

  class WrappedPasteAction(base: Action)
    extends WrappedAction(base, EditCategory, null, EditClipboardGroup, keystroke('V', withMenu = true))
    with Refreshable {

    def refresh(): Unit = {
      try {
        val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard

        setEnabled(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor) ||
                   clipboard.isDataFlavorAvailable(ClipboardUtils.widgetsFlavor))
      } catch {
        case _: IllegalStateException =>
          setEnabled(false)
      }
    }

    override def actionPerformed(e: ActionEvent): Unit = {
      KeyboardFocusManager.getCurrentKeyboardFocusManager.getPermanentFocusOwner match {
        case target: CopyPasteTarget =>
          target.paste()

        case _ =>
          super.actionPerformed(e)
      }
    }
  }
}
