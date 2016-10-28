// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import java.beans.PropertyChangeListener
import javax.swing.{ AbstractAction, Action, KeyStroke}, Action.{ ACCELERATOR_KEY, NAME, SHORT_DESCRIPTION }
import UserAction.{ ActionCategoryKey, ActionGroupKey }

/** The purpose of this class is to allow an action which doesn't know about the NetLogo menu system
 *  to be placed into a NetLogo menu. It's especially important in cases where the action is defined
 *  by somwhere which doesn't (or can't) know about the various keys in UserAction. This includes
 *  packages which aren't allowed to depend on org.nlogo.swing (editor, for instance) or external
 *  libraries which provide actions (swing or RSyntaxTextArea) */
class WrappedAction(base: Action, menu: String, group: String, accelerator: KeyStroke) extends Action {
  val wrappedValues = Map[String, AnyRef](
    ACCELERATOR_KEY   -> accelerator,
    ActionCategoryKey -> menu,
    ActionGroupKey    -> group
  )

  override def getValue(k: String): AnyRef =
    (Option(base.getValue(k)) orElse wrappedValues.get(k)).orNull

  override def putValue(k: String, v: AnyRef): Unit =
    base.putValue(k, v)

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit =
    base.removePropertyChangeListener(listener)

  def setEnabled(isEnabled: Boolean): Unit =
    base.setEnabled(isEnabled)

  override def actionPerformed(e: ActionEvent): Unit =
    base.actionPerformed(e)

  override def isEnabled: Boolean =
    base.isEnabled

  override def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    base.addPropertyChangeListener(listener)
  }
}
