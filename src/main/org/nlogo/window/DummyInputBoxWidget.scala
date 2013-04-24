// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.AbstractEditorArea
import org.nlogo.api.ParserServices
import java.awt.Component

class DummyInputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                          nextComponent: Component, parser: ParserServices) extends
  InputBox(textArea, dialogTextArea, parser, nextComponent) {

  override def propertySet = Properties.dummyInput
}
