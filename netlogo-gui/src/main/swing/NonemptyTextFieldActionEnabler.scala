// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.Action
import javax.swing.event.{ DocumentEvent, DocumentListener }

/**
 * Makes an Action enabled only if a set of TextFields are not empty.
 */
class NonemptyTextFieldActionEnabler(target: Action, fields: List[TextField]) extends DocumentListener {
  fields.foreach(_.getDocument.addDocumentListener(this))

  update()

  private def update(): Unit = {
    target.setEnabled(fields.forall(_.getDocument.getLength > 0));
  }

  def changedUpdate(e: DocumentEvent): Unit = {
    update()
  }

  def insertUpdate(e: DocumentEvent): Unit = {
    update()
  }

  def removeUpdate(e: DocumentEvent): Unit = {
    update()
  }
}
