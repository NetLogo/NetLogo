// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.I18N
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.window.Events.{ InterfaceGlobalEvent, PeriodicUpdateEvent, WidgetEditedEvent }

class InputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                     compiler: CompilerServices, nextComponent: Component)
  extends InputBox(textArea, dialogTextArea, compiler, nextComponent)
  with InterfaceGlobalWidget
  with PeriodicUpdateEvent.Handler {

  override def editPanel: EditPanel = new InputEditPanel(this, compiler)

  override def getEditable: Option[Editable] = Some(this)

  override def name(name: String, sendEvent: Boolean): Unit = {
    this.name_=(name)
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    if (name == "") {
      displayName(I18N.gui.get("edit.input.previewName"))
    } else {
      displayName(name)
    }
    if (sendEvent) new InterfaceGlobalEvent(this, true, false, false, false).raise(this)
    widgetLabel.setText(displayName)
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    if (!editing) new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def valueObject(value: Any, raiseEvent: Boolean): Unit = {
    if (! this.value.contains(toAnyRef(value))) {
      oldText = text
      text = Dump.logoObject(toAnyRef(value))
      this.value = Option(toAnyRef(value))
      if (!text.equals(textArea.getText())) textArea.setText(text)
      if (raiseEvent) new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
      inputType.colorPanel(colorSwatch)
      new WidgetEditedEvent(this).raise(this)
    }
  }
}
