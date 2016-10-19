// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Container
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.{ Document, JTextComponent }

// binds to a container and ensures that changes to that container's backing document
// propagate to the `onChange` callback.
class EditorListener(onChange: DocumentEvent => Unit) extends PropertyChangeListener with DocumentListener {
  def install(component: JTextComponent): Unit = {
    component.addPropertyChangeListener(this)
    component.getDocument.addDocumentListener(this)
  }

  override def propertyChange(evt: PropertyChangeEvent): Unit = {
    if (evt.getPropertyName == "document") {
      evt.getOldValue match {
        case d: Document => d.removeDocumentListener(this)
        case _ =>
      }
      evt.getNewValue match {
        case d: Document => d.addDocumentListener(this)
        case _ =>
      }
    }
  }

  def insertUpdate(e: DocumentEvent): Unit = { onChange(e) }
  def removeUpdate(e: DocumentEvent): Unit = { onChange(e) }
  def changedUpdate(e: DocumentEvent): Unit = { onChange(e) }
}
