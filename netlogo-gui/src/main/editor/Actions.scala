// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, Action }, Action.{ ACCELERATOR_KEY, ACTION_COMMAND_KEY, NAME }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.text._
import javax.swing.text.DefaultEditorKit.{ CutAction, CopyAction, PasteAction, InsertContentAction, SelectAllAction }

import org.nlogo.api.Refreshable
import org.nlogo.core.I18N
import org.nlogo.swing.UserAction //TODO: Depend won't like this...

import KeyBinding._
import RichDocument._

object Actions {
  /// default editor kit actions
  private val actionMap =
    new DefaultEditorKit().getActions.map{ a => (a.getValue(Action.NAME), a) }.toMap

  val commentToggleAction = new CommentToggleAction()
  val shiftLeftAction     = new ShiftLeftAction()
  val shiftRightAction    = new ShiftRightAction()
  val tabKeyAction        = new TabKeyAction()
  val shiftTabKeyAction   = new ShiftTabKeyAction()
  val CutAction           = new NetLogoCutAction()
  val CopyAction          = new NetLogoCopyAction()
  val PasteAction         = new NetLogoPasteAction()
  val DeleteAction        = new NetLogoDeleteAction()
  val SelectAllAction     = new NetLogoSelectAllAction()

  def getDefaultEditorKitAction(name:String) = actionMap(name)

  def setEnabled(enabled:Boolean){
    List(commentToggleAction,shiftLeftAction,shiftRightAction).foreach(_.setEnabled(enabled))
  }

  class TabKeyAction extends MyTextAction(I18N.gui.get("menu.edit.format"), _.indentSelection()) {
    putValue(UserAction.ActionGroupKey,    UserAction.EditFormatGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke(java.awt.event.KeyEvent.VK_TAB))
  }

  class MyTextAction(name:String, f: EditorArea => Unit) extends TextAction(name) {
    override def actionPerformed(e:ActionEvent){
      val component = getTextComponent(e)
      if(component.isInstanceOf[EditorArea]) f(component.asInstanceOf[EditorArea])
    }
  }


  abstract class DocumentAction(name: String) extends TextAction(name) {
    override def actionPerformed(e: ActionEvent): Unit = {
      Option(getTextComponent(e)).foreach { component =>
        try {
          perform(component, component.getDocument, e)
        } catch {
          case ex: BadLocationException => throw new IllegalStateException(ex)
        }
      }
    }

    def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit
  }

  class NetLogoPasteAction extends PasteAction with Refreshable {
    putValue(NAME,                         I18N.gui.get("menu.edit.paste"))
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke('V', withMenu = true))
    putValue(UserAction.ActionGroupKey,    UserAction.EditClipboardGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    def refresh(): Unit = {
      setEnabled(java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
        .isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor))
    }
  }

  class NetLogoCopyAction extends CopyAction {
    putValue(NAME,                         I18N.gui.get("menu.edit.copy"))
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke('C', withMenu = true))
    putValue(UserAction.ActionGroupKey,    UserAction.EditClipboardGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
  }

  class NetLogoCutAction extends CutAction {
    putValue(NAME,                         I18N.gui.get("menu.edit.cut"))
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke('X', withMenu = true))
    putValue(UserAction.ActionGroupKey,    UserAction.EditClipboardGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
  }

  class NetLogoDeleteAction extends InsertContentAction {
    putValue(NAME,                         I18N.gui.get("menu.edit.delete"))
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke(java.awt.event.KeyEvent.VK_DELETE))
    putValue(ACTION_COMMAND_KEY,           "")
    putValue(UserAction.ActionGroupKey,    UserAction.EditClipboardGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
  }

  class NetLogoSelectAllAction extends AbstractAction {
    putValue(NAME,                         I18N.gui.get("menu.edit.selectAll"))
    putValue(ACCELERATOR_KEY,              UserAction.KeyBindings.keystroke('A', withMenu = true))
    putValue(UserAction.ActionGroupKey,    "SelectAll")
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    val defaultAction =
      getDefaultEditorKitAction(DefaultEditorKit.selectAllAction)

    override def actionPerformed(event: ActionEvent): Unit = {
      defaultAction.actionPerformed(event)
    }
  }

  class ShiftLeftAction extends DocumentAction(I18N.gui.get("menu.edit.shiftLeft")) {
    putValue(ACCELERATOR_KEY, keystroke(KeyEvent.VK_OPEN_BRACKET, menuShortcutMask))
    putValue(UserAction.ActionGroupKey,    UserAction.EditFormatGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      val (startLine, endLine) =
        document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)

      for {
        lineNum <- startLine to endLine
      } {
        val lineStart = document.lineToStartOffset(lineNum)
        if (lineStart != -1) {
          val text = document.getText(lineStart, 1)
          if (text.length > 0 && text.charAt(0) == ' ') {
            document.remove(lineStart, 1)
          }
        }
      }
    }
  }

  class ShiftRightAction extends DocumentAction(I18N.gui.get("menu.edit.shiftRight")) {
    putValue(ACCELERATOR_KEY, keystroke(KeyEvent.VK_CLOSE_BRACKET, menuShortcutMask))
    putValue(UserAction.ActionGroupKey,    UserAction.EditFormatGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      val (startLine, endLine) =
        document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)
      document.insertBeforeLinesInRange(startLine, endLine, " ")
    }
  }

  class ShiftTabKeyAction extends DocumentAction(I18N.gui.get("menu.edit.shiftTab")) {
    putValue(ACCELERATOR_KEY, UserAction.KeyBindings.keystroke(KeyEvent.VK_TAB, withShift = true))
    putValue(UserAction.ActionGroupKey,    UserAction.EditFormatGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      val (startLine, endLine) =
        document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)
      for {
        lineNum <- startLine to endLine
      } {
        val lineStart = document.lineToStartOffset(lineNum)
        if (lineStart != -1) {
          val text = document.getText(lineStart, 2)
          text.length match {
            case 0 =>
            case 1 if text.charAt(0) == ' ' => document.remove(lineStart, 1)
            case _ =>
              if (text.charAt(0) == ' ' && text.charAt(1) == ' ')
                document.remove(lineStart, 2)
              else if (text.charAt(0) == ' ')
                document.remove(lineStart, 1)
          }
        }
      }
    }
  }

  class CommentToggleAction extends DocumentAction(I18N.gui.get("menu.edit.comment") + " / " + I18N.gui.get("menu.edit.uncomment")) {
    putValue(ACCELERATOR_KEY,              keystroke(KeyEvent.VK_SEMICOLON, menuShortcutMask))
    putValue(UserAction.ActionGroupKey,    UserAction.EditFormatGroup)
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)

    override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
      val (startLine, endLine) =
        document.selectionLineRange(component.getSelectionStart, component.getSelectionEnd)

      for(currentLine <- startLine to endLine) {
        val lineStart = document.lineToStartOffset(currentLine)
        val lineEnd = document.lineToEndOffset(currentLine)
        val text = document.getText(lineStart, lineEnd - lineStart)
        val semicolonPos = text.indexOf(';')
        val allSpaces = (0 until semicolonPos)
          .forall(i => Character.isWhitespace(text.charAt(i)))
        if (!allSpaces || semicolonPos == -1) {
          document.insertBeforeLinesInRange(startLine, endLine, ";")
          return
        }
      }
      // Logic to uncomment the selected section
      for (line <- startLine to endLine) {
        val lineStart = document.lineToStartOffset(line)
        val lineEnd   = document.lineToEndOffset(line)
        val text      = document.getText(lineStart, lineEnd - lineStart)
        val semicolonPos = text.indexOf(';')
        if (semicolonPos != -1) {
          val allSpaces = (0 until semicolonPos)
            .forall(i => Character.isWhitespace(text.charAt(i)))
          if (allSpaces)
            document.remove(lineStart + semicolonPos, 1)
        }
      }
    }
  }
}

import Actions._

trait QuickHelpAction {
  def colorizer: Colorizer

  def doHelp(document: Document, offset: Int, component: Component): Unit = {
    if (offset != -1) {
      val lineNumber = document.offsetToLine(offset)
      for {
        lineText    <- document.getLineText(document.offsetToLine(offset))
        tokenString <- colorizer.getTokenAtPosition(lineText, offset - document.lineToStartOffset(lineNumber))
      } {
        colorizer.doHelp(component, tokenString)
      }
    }
  }
}
class MouseQuickHelpAction(val colorizer: Colorizer)
  extends AbstractAction(I18N.gui.get("tabs.code.rightclick.quickhelp"))
  with EditorAwareAction
  with QuickHelpAction {

  putValue(UserAction.ActionCategoryKey, UserAction.HelpCategory)

  override def actionPerformed(e: ActionEvent): Unit = {
    doHelp(editor.getDocument, documentOffset, editor)
  }
}

class KeyboardQuickHelpAction(val colorizer: Colorizer)
  extends Actions.DocumentAction(I18N.gui.get("tabs.code.rightclick.quickhelp"))
  with QuickHelpAction {

  putValue(UserAction.ActionCategoryKey, UserAction.HelpCategory)
  putValue(ACCELERATOR_KEY, keystroke(KeyEvent.VK_F1))
  putValue(ACTION_COMMAND_KEY, "org.nlogo.editor.quickHelp")

  override def perform(component: JTextComponent, document: Document, e: ActionEvent): Unit = {
    val targetOffset = component.getSelectionEnd
    doHelp(component.getDocument, targetOffset, component)
  }
}
