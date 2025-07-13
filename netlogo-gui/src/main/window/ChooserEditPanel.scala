// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class ChooserEditPanel(target: ChooserWidget, compiler: CompilerServices, colorizer: Colorizer,
                       extensionManager: ExtensionManager) extends EditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.globalVar"),
        () => target.name,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

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

    add(nameWrapper, c)

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(choicesLabeled, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weighty = 0

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, choicesWrapper, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    choicesLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
