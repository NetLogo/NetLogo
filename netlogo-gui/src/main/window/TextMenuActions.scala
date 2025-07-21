// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.{ ActionEvent, KeyEvent }
import java.lang.IllegalStateException
import javax.swing.Action
import javax.swing.text.{ Document, JTextComponent }

import org.nlogo.api.Refreshable
import org.nlogo.core.I18N
import org.nlogo.editor.{ Actions, Colorizer, DocumentAction, QuickHelpAction }
import org.nlogo.swing.{ WrappedAction, UserAction },
  UserAction.{ EditCategory, EditClipboardGroup, EditSelectionGroup, HelpCategory,
    HelpContextGroup, KeyBindings, MenuAction },
    KeyBindings.keystroke

object TextMenuActions {
  val CutAction       =
    new WrappedAction(Actions.CutAction, EditCategory, null, EditClipboardGroup, keystroke('X', withMenu = true))
  val CopyAction      =
    new WrappedAction(Actions.CopyAction, EditCategory, null, EditClipboardGroup, keystroke('C', withMenu = true))
  val PasteAction     = new WrappedPasteAction(Actions.PasteAction)
  val DeleteAction    =
    new WrappedAction(
      Actions.DeleteAction, EditCategory, null, EditClipboardGroup, keystroke(KeyEvent.VK_DELETE))
  val SelectAllAction =
    new WrappedAction(
      Actions.SelectAllAction, EditCategory, null, EditSelectionGroup, keystroke('A', withMenu = true))

  def keyboardQuickHelp(colorizer: Colorizer) =
    new KeyboardQuickHelpAction(colorizer)

  class WrappedPasteAction(base: Action)
    extends WrappedAction(base, EditCategory, null, EditClipboardGroup, keystroke('V', withMenu = true))
    with Refreshable {

    def refresh(): Unit = {
      try {
        setEnabled(Toolkit.getDefaultToolkit.getSystemClipboard
          .isDataFlavorAvailable(DataFlavor.stringFlavor))
      } catch {
        case _: IllegalStateException =>
          setEnabled(false)
      }
    }
  }

  class KeyboardQuickHelpAction(val colorizer: Colorizer)
    extends DocumentAction(I18N.gui.get("menu.help.lookUpInDictionary"))
    with QuickHelpAction
    with MenuAction {

    category    = HelpCategory
    group       = HelpContextGroup
    accelerator = KeyBindings.keystroke(KeyEvent.VK_F1)

    override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      val targetOffset = component.getSelectionEnd
      doHelp(component.getDocument, targetOffset, component)
    }
  }
}
