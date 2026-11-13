// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummyChooserEditPanel(target: DummyChooserWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

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

  add(name)
  add(choicesLabeled)
  add(new BoxRow(oldSize, BoxAlign.Start))

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
