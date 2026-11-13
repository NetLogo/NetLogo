// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxRow }
import org.nlogo.window.{ EditPanel, IdentifierEditor, NonEmptyCodeEditor, OptionsEditor, PropertyAccessor,
                          PropertyEditor }

class RateEditPanel(target: RateConnection, compiler: CompilerServices, colorizer: Colorizer,
                    extensionManager: ExtensionManager) extends EditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        loc"Name",
        () => target.nameWrapper,
        name => target.nameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val inputs =
    new OptionsEditor[String](
      new PropertyAccessor(
        target,
        loc"Inputs",
        () => target.inputs,
        _.foreach(target.inputs),
        () => apply()))

  private val expressionWrapper =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        loc"Expression",
        () => target.expressionWrapper,
        _.foreach(target.expressionWrapper),
        () => apply()),
      compiler, colorizer)

  add(nameWrapper)
  add(new BoxRow(inputs, BoxAlign.Start))
  add(expressionWrapper)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, inputs, expressionWrapper)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
