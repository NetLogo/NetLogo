// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxRow }

class SwitchEditPanel(target: SwitchWidget, compiler: CompilerServices, extensionManager: ExtensionManager)
  extends WidgetEditPanel(target) {

  private val name =
    new IdentifierEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.switch.globalVar"),
        () => target.nameWrapper,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val oldSize =
    new BooleanEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(name)
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }

  override def autoFill(): Boolean =
    AutomationUtils.sendChars(name, s"test-${System.currentTimeMillis}")
}
