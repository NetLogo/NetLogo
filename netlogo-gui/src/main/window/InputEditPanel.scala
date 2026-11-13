// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxRow }

class InputEditPanel(target: InputBoxWidget, compiler: CompilerServices, extensionManager: ExtensionManager)
  extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        loc"Global Variable",
        () => target.name,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val typeOptions =
    new InputBoxEditor(
      new PropertyAccessor(
        target,
        loc"Type",
        () => target.typeOptions,
        _.foreach(target.setTypeOptions),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
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
