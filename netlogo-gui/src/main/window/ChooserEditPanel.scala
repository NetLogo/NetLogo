// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxRow }

class ChooserEditPanel(target: ChooserWidget, compiler: CompilerServices, colorizer: Colorizer,
                       extensionManager: ExtensionManager) extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        loc"Global variable",
        () => target.name,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val choicesWrapper =
    new LogoListEditor(
      new PropertyAccessor(
        target,
        loc"Choices",
        () => target.choicesWrapper,
        _.foreach(target.setChoicesWrapper),
        () => apply()),
      compiler, colorizer)

  private val choicesLabeled =
    new LabeledEditor(choicesWrapper, loc"<html>example: &quot;a&quot; &quot;b&quot; &quot;c&quot; 3 4 5</html>")

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(nameWrapper)
  add(choicesLabeled)
  add(new BoxRow(oldSize, BoxAlign.Start))

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
