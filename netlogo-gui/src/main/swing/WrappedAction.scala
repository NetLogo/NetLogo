// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.{ Action, KeyStroke }

/** The purpose of this class is to allow an action which doesn't know about the NetLogo menu system
 *  to be placed into a NetLogo menu. It's especially important in cases where the action is defined
 *  by somewhere which doesn't (or can't) know about the various keys in UserAction. This includes
 *  packages which aren't allowed to depend on org.nlogo.swing (editor, for instance) or external
 *  libraries which provide actions (swing or RSyntaxTextArea) */
class WrappedAction(base: Action) extends UserAction.MenuAction {
  def this(base: Action, category: String, group: String, accelerator: KeyStroke) = {
    this(base)

    this.category = category
    this.group = group
    this.accelerator = accelerator
  }

  override def getValue(k: String): AnyRef =
    Option(base.getValue(k)).getOrElse(super.getValue(k))

  override def putValue(k: String, v: AnyRef): Unit =
    base.putValue(k, v)

  override def setEnabled(isEnabled: Boolean): Unit =
    base.setEnabled(isEnabled)

  override def actionPerformed(e: ActionEvent): Unit =
    base.actionPerformed(e)
}
