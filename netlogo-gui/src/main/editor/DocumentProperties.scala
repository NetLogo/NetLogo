// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.text.{ DefaultEditorKit, Document, JTextComponent, PlainDocument }

// binds to a container and ensures that changes to that container's backing document
// propagate to the `onChange` callback.
object DocumentProperties extends PropertyChangeListener {
  def install(component: JTextComponent): Unit = {
    addProperties(component.getDocument)
    component.addPropertyChangeListener(this)
  }

  override def propertyChange(evt: PropertyChangeEvent): Unit = {
    if (evt.getPropertyName == "document") {
      evt.getNewValue match {
        case d: Document => addProperties(d)
        case _ =>
      }
    }
  }

  private def addProperties(d: Document): Unit = {
    // on Windows, prevent save() from outputting ^M characters - ST 2/23/04
    d.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n")
    d.putProperty(PlainDocument.tabSizeAttribute, Int.box(2))
  }
}
