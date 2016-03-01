// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.AbstractEditorArea
import org.nlogo.api.Dump

class InputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                     compiler: org.nlogo.api.CompilerServices, nextComponent: java.awt.Component)
        extends InputBox(textArea, dialogTextArea, compiler, nextComponent)
                with InterfaceGlobalWidget
                with org.nlogo.window.Events.PeriodicUpdateEvent.Handler {
  def propertySet = Properties.input

  override def name(name: String, sendEvent: Boolean) {
    this.name_=(name)
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(name)
    if (sendEvent) new org.nlogo.window.Events.InterfaceGlobalEvent(this, true, false, false, false).raise(this)
    widgetLabel.setText(name)
  }

  def handle(e: org.nlogo.window.Events.PeriodicUpdateEvent) {
    if (!editing) new org.nlogo.window.Events.InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def valueObject(value: AnyRef, raiseEvent: Boolean) {
    if (this.value != value) {
      text = Dump.logoObject(value)
      this.value = value
      if (!text.equals(textArea.getText())) textArea.setText(text)
      if (raiseEvent) new org.nlogo.window.Events.InterfaceGlobalEvent(this, false, false, true, false).raise(this)
      inputType.colorPanel(colorSwatch)
    }
  }
}
