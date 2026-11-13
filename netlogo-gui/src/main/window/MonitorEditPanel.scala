// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxColumn, BoxRow, MaximumHeight }

class MonitorEditPanel(target: MonitorWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val wrapSource =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        loc"Reporter",
        () => target.wrapSource,
        name => target.setWrapSource(name.getOrElse("")),
        () => apply()),
      compiler, colorizer, () => target.error())

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Display Name",
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val decimalPlaces =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Decimal places",
        () => target.decimalPlaces,
        _.foreach(target.setDecimalPlaces),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Units (optional)",
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val decimalLabeled = new LabeledEditor(decimalPlaces, loc"full precision is 17")

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Font Size",
        () => target.fontSize,
        _.foreach(target.setFontSize),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(wrapSource)
  add(name)
  add(new BoxRow(Seq(
    decimalLabeled,
    new BoxColumn(units, BoxAlign.Start)
  ), 6) with MaximumHeight)
  add(fontSize)
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(wrapSource, name, decimalPlaces, units, fontSize, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    decimalLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    wrapSource.requestFocus()
  }

  override def autoFill(): Boolean = {
    AutomationUtils.sendChars(name, s"test-${System.currentTimeMillis}")
    AutomationUtils.sendChars(wrapSource, s"count turtles")
  }
}
