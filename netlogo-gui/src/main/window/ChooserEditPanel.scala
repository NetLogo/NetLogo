// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ AutomationUtils, VerticalStrut, ZoomableBorder }

class ChooserEditPanel(target: ChooserWidget, compiler: CompilerServices, colorizer: Colorizer,
                       extensionManager: ExtensionManager) extends WidgetEditPanel(target) {

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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(nameWrapper)
  add(new VerticalStrut(6))
  add(choicesLabeled)
  add(new VerticalStrut(6))
  add(oldSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, choicesWrapper, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    choicesLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }

  override def autoFill(): Boolean = {
    AutomationUtils.sendChars(nameWrapper, s"test-${System.currentTimeMillis}") &&
    AutomationUtils.sendChars(choicesWrapper, "1 \"a\" false")
  }
}
