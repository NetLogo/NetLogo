// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.swing.AutomationUtils

class InputEditPanel(target: InputBoxWidget, compiler: CompilerServices, extensionManager: ExtensionManager)
  extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.input.globalVar"),
        () => target.name,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val typeOptions =
    new InputBoxEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.input.type"),
        () => target.typeOptions,
        _.foreach(target.setTypeOptions),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
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
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, typeOptions, oldSize)

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }

  override def autoFill(): Boolean =
    AutomationUtils.sendChars(nameWrapper, s"test-${System.currentTimeMillis}")
}
