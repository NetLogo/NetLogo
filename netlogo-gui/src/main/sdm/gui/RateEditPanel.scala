// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
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

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(0, 6, 6, 6)

    add(inputs, c)

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1

    add(expressionWrapper, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, inputs, expressionWrapper)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
