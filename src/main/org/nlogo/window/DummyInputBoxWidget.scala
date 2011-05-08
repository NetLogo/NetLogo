package org.nlogo.window

import java.util.List
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.api.{CompilerServices, Property}
import java.awt.Component

class DummyInputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                          nextComponent: Component, compiler: CompilerServices) extends
  InputBox(textArea, dialogTextArea, compiler, nextComponent) {

  override def propertySet: List[Property] = return Properties.dummyInput
}

