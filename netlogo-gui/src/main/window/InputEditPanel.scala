// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N

class InputEditPanel(target: InputBoxWidget, compiler: CompilerServices) extends WidgetEditPanel(target) {
  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.input.globalVar"),
        () => target.name,
        target.setNameWrapper(_),
        () => apply()),
      compiler)

  private val typeOptions =
    new InputBoxEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.input.type"),
        () => target.typeOptions,
        target.setTypeOptions(_),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.gridy = 1
    c.gridwidth = 1
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 6, 6, 6)

    add(typeOptions, c)

    c.weightx = 1
    c.insets = new Insets(0, 0, 6, 6)

    add(oldSize, c)

    nameWrapper.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(nameWrapper, typeOptions, oldSize)
}
