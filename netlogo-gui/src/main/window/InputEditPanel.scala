// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxRow }

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

  add(nameWrapper)
  add(new BoxRow(Seq(typeOptions, oldSize), 6, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, typeOptions, oldSize)

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }

  override def autoFill(): Boolean =
    AutomationUtils.sendChars(nameWrapper, s"test-${System.currentTimeMillis}")
}
