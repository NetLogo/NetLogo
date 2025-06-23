// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.api.CompilerServices
import org.nlogo.editor.AbstractEditorArea

class DummyInputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                          nextComponent: Component, compiler: CompilerServices) extends
  InputBox(textArea, dialogTextArea, compiler, nextComponent) {

  override def editPanel: EditPanel = new DummyInputEditPanel(this)

  override def getEditable: Option[Editable] = Some(this)
}
