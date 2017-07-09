// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.ActionEvent
import javax.swing.text.{ BadLocationException, Document, JTextComponent, TextAction }

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
