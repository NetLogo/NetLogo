// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import javax.swing.{ Box, BoxLayout }

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxRow, VerticalStrut, ZoomableBorder }
import org.nlogo.window.{ EditPanel, IdentifierEditor, NonEmptyCodeEditor, OptionsEditor, PropertyAccessor,
                          PropertyEditor }

class RateEditPanel(target: RateConnection, compiler: CompilerServices, colorizer: Colorizer,
                    extensionManager: ExtensionManager) extends EditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.name"),
        () => target.nameWrapper,
        name => target.nameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val inputs =
    new OptionsEditor[String](
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.inputs"),
        () => target.inputs,
        _.foreach(target.inputs),
        () => apply()))

  private val expressionWrapper =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.expression"),
        () => target.expressionWrapper,
        _.foreach(target.expressionWrapper),
        () => apply()),
      compiler, colorizer)

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(nameWrapper)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(inputs, Box.createHorizontalGlue)))
  add(new VerticalStrut(6))
  add(expressionWrapper)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, inputs, expressionWrapper)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
