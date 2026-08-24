// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ AutomationUtils, BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(nameWrapper)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(typeOptions, new HorizontalStrut(6), oldSize)))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, typeOptions, oldSize)

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }

  override def autoFill(): Boolean =
    AutomationUtils.sendChars(nameWrapper, s"test-${System.currentTimeMillis}")
}
