// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.api.CompilerServices
import org.nlogo.editor.{ AbstractEditorArea, Colorizer }

class DummyInputBoxWidget(textArea: AbstractEditorArea, dialogTextArea: AbstractEditorArea,
                          nextComponent: Component, compiler: CompilerServices) extends
  InputBox(textArea, dialogTextArea, compiler, nextComponent) {

  override def createEditPanel(compiler: CompilerServices, colorizer: Colorizer): EditPanel =
    null
}
