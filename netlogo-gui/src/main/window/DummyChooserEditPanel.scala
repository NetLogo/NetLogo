// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class DummyChooserEditPanel(target: DummyChooserWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val choicesWrapper =
    new LogoListEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.choices"),
        () => target.choicesWrapper,
        _.foreach(target.setChoicesWrapper),
        () => apply()),
      compiler, colorizer)

  private val choicesLabeled = new LabeledEditor(choicesWrapper, I18N.gui.get("edit.chooser.example"))

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

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(name, c)

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(choicesLabeled, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weighty = 0

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, choicesWrapper, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    choicesLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
