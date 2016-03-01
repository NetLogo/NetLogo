// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.AbstractEditorArea
import org.nlogo.api.CompilerServices
import java.awt.Component

class DummyInputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                          nextComponent: Component, compiler: CompilerServices) extends
  InputBox(textArea, dialogTextArea, compiler, nextComponent) {

  override def propertySet = Properties.dummyInput
}

