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

  def setEnabled(enabled: Boolean) {
    this.enabled = enabled

    update()
  }

  private def update() {
    target.setEnabled(enabled && fields.forall(_.getDocument.getLength > 0));
  }

  def changedUpdate(e: DocumentEvent) {
    update()
  }

  def insertUpdate(e: DocumentEvent) {
    update()
  }

  def removeUpdate(e: DocumentEvent) {
    update()
  }
}
