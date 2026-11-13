// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummyPlotEditPanel(target: DummyPlotWidget) extends WidgetEditPanel(target) {
  private val nameOptions =
    new OptionsEditor(
      new PropertyAccessor(
        target,
        loc"Name",
        () => target.nameOptions,
        _.foreach(target.setNameOptions),
        () => apply()))

  private val xLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"X axis label",
        () => target.xLabel,
        _.foreach(target.setXLabel),
        () => apply()))

  private val xMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"X axis label",
        () => target.defaultXMin,
        _.foreach(target.setDefaultXMin),
        () => apply()))

  private val xMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"X max",
        () => target.defaultXMax,
        _.foreach(target.setDefaultXMax),
        () => apply()))

  private val yLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Y axis label",
        () => target.yLabel,
        _.foreach(target.setYLabel),
        () => apply()))

  private val yMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Y min",
        () => target.defaultYMin,
        _.foreach(target.setDefaultYMin),
        () => apply()))

  private val yMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Y max",
        () => target.defaultYMax,
        _.foreach(target.setDefaultYMax),
        () => apply()))

  private val autoPlotX =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Auto scale x-axis?",
        () => target.defaultAutoPlotX,
        _.foreach(target.setDefaultAutoPlotX),
        () => apply()))

  private val autoPlotY =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Auto scale y-axis?",
        () => target.defaultAutoPlotY,
        _.foreach(target.setDefaultAutoPlotY),
        () => apply()))

  private val showLegend =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Show legend?",
        () => target.showLegend,
        _.foreach(target.setShowLegend),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(new BoxRow(nameOptions, BoxAlign.Start))
  add(new BoxRow(Seq(xLabel, xMin, xMax), 6))
  add(new BoxRow(Seq(yLabel, yMin, yMax), 6))
  add(new BoxRow(Seq(autoPlotX, autoPlotY, showLegend), 6, BoxAlign.Start))
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameOptions, xLabel, xMin, xMax, yLabel, yMin, yMax, autoPlotX, autoPlotY, showLegend, oldSize)

  override def requestFocus(): Unit = {
    nameOptions.requestFocus()
  }
}
