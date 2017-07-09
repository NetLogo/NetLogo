// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action },
  Action.{ ACTION_COMMAND_KEY, NAME }
import javax.swing.text.DefaultEditorKit,
  DefaultEditorKit.{ CutAction, CopyAction, PasteAction, InsertContentAction }

import org.nlogo.core.I18N

object Actions {
  /// default editor kit actions
  private val actionMap =
    new DefaultEditorKit().getActions.map{ a => (a.getValue(NAME), a) }.toMap

  val CutAction           = new NetLogoCutAction()
  val CopyAction          = new NetLogoCopyAction()
  val PasteAction         = new NetLogoPasteAction()
  val DeleteAction        = new NetLogoDeleteAction()
  val SelectAllAction     = new NetLogoSelectAllAction()

  def getDefaultEditorKitAction(name:String) = actionMap(name)

  class NetLogoPasteAction extends PasteAction {
    putValue(NAME, I18N.gui.get("menu.edit.paste"))
  }

  class NetLogoCopyAction extends CopyAction {
    putValue(NAME, I18N.gui.get("menu.edit.copy"))
  }

  class NetLogoCutAction extends CutAction {
    putValue(NAME, I18N.gui.get("menu.edit.cut"))
  }

  class NetLogoDeleteAction extends InsertContentAction {
    putValue(NAME, I18N.gui.get("menu.edit.delete"))
    putValue(ACTION_COMMAND_KEY, "")
  }

  class NetLogoSelectAllAction extends AbstractAction {
    putValue(NAME, I18N.gui.get("menu.edit.selectAll"))

    val defaultAction =
      getDefaultEditorKitAction(DefaultEditorKit.selectAllAction)

    override def actionPerformed(event: ActionEvent): Unit = {
      defaultAction.actionPerformed(event)
    }
  }
}

