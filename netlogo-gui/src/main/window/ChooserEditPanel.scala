// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }
import javax.swing.JLabel

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.theme.InterfaceColors

class ChooserEditPanel(target: ChooserWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.globalVar"),
        () => target.name,
        target.setNameWrapper(_),
        () => apply()),
      compiler)

  private val choicesWrapper =
    new LogoListEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.choices"),
        () => target.choicesWrapper,
        target.setChoicesWrapper(_),
        () => apply()),
      compiler, colorizer)

  private val choicesLabel = new JLabel(I18N.gui.get("edit.chooser.example")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.insets = new Insets(0, 6, 3, 6)

    add(choicesWrapper, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(choicesLabel, c)
    add(oldSize, c)

    nameWrapper.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(nameWrapper, choicesWrapper, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    choicesLabel.setForeground(InterfaceColors.dialogText)
  }
}
