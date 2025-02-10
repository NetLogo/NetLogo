// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.event.{ DocumentEvent, DocumentListener }

/**
 * Makes a Button enabled only if a set of TextFields are not empty.
 */
class NonemptyTextFieldButtonEnabler(target: Button, fields: List[TextField]) extends DocumentListener {
  private var enabled = true

  fields.foreach(_.getDocument.addDocumentListener(this))

  update()

  def setEnabled(enabled: Boolean): Unit = {
    this.enabled = enabled

    update()
  }

  private def update(): Unit = {
    target.setEnabled(enabled && fields.forall(_.getDocument.getLength > 0))
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
