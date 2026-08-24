// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

class DummyPlotEditPanel(target: DummyPlotWidget) extends WidgetEditPanel(target) {
  private val nameOptions =
    new OptionsEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.name"),
        () => target.nameOptions,
        _.foreach(target.setNameOptions),
        () => apply()))

  private val xLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xLabel"),
        () => target.xLabel,
        _.foreach(target.setXLabel),
        () => apply()))

  private val xMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xmin"),
        () => target.defaultXMin,
        _.foreach(target.setDefaultXMin),
        () => apply()))

  private val xMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xmax"),
        () => target.defaultXMax,
        _.foreach(target.setDefaultXMax),
        () => apply()))

  private val yLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.yLabel"),
        () => target.yLabel,
        _.foreach(target.setYLabel),
        () => apply()))

  private val yMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.ymin"),
        () => target.defaultYMin,
        _.foreach(target.setDefaultYMin),
        () => apply()))

  private val yMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.ymax"),
        () => target.defaultYMax,
        _.foreach(target.setDefaultYMax),
        () => apply()))

  private val autoPlotX =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.autoScaleX"),
        () => target.defaultAutoPlotX,
        _.foreach(target.setDefaultAutoPlotX),
        () => apply()))

  private val autoPlotY =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.autoScaleY"),
        () => target.defaultAutoPlotY,
        _.foreach(target.setDefaultAutoPlotY),
        () => apply()))

  private val showLegend =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.showLegend"),
        () => target.showLegend,
        _.foreach(target.setShowLegend),
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

  add(nameOptions)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(xLabel, new HorizontalStrut(6), xMin, new HorizontalStrut(6), xMax)))
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(yLabel, new HorizontalStrut(6), yMin, new HorizontalStrut(6), yMax)))
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(autoPlotX, new HorizontalStrut(6), autoPlotY, new HorizontalStrut(6), showLegend)))
  add(new VerticalStrut(6))
  add(oldSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameOptions, xLabel, xMin, xMax, yLabel, yMin, yMax, autoPlotX, autoPlotY, showLegend, oldSize)

  override def requestFocus(): Unit = {
    nameOptions.requestFocus()
  }
}
