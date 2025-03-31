// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N

class SwitchEditPanel(target: SwitchWidget, compiler: CompilerServices) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      PropertyAccessor(
        I18N.gui.get("edit.switch.globalVar"),
        () => target.nameWrapper,
        s => target.nameWrapper(s.trim),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      PropertyAccessor(
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(name, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(oldSize, c)

    name.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(name, oldSize)

  override def errorString: Option[String] = {
    name.get match {
      case Some(str) =>
        if (str.isEmpty) {
          Some(I18N.gui.get("edit.switch.globalVarEmpty"))
        } else if (!compiler.isValidIdentifier(str)) {
          Some(I18N.gui.get("edit.switch.globalVarInvalid"))
        } else {
          None
        }

      case None =>
        Some(I18N.gui.get("edit.switch.globalVarInvalid"))
    }
  }

  override def syncTheme(): Unit = {
    name.syncTheme()
    oldSize.syncTheme()
  }
}
