// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.{ FocusAdapter, FocusEvent }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxRow, MaximumHeight }

import scala.util.Try

class PlotEditPanel(target: PlotWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val plotName =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.name"),
        () => target.plotName,
        name => target.setPlotName(name.getOrElse("")),
        () => apply())) {

      override def get: Try[String] =
        super.get.map(_.trim).filter(_.nonEmpty).orElse(defaultError)
    }

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

  private val runtimeError =
    new RuntimeErrorDisplay(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.error.runtimeError"),
        () => target.runtimeError,
        _.foreach(target.setRuntimeError),
        () => apply()))

  private val setupCode =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.setupCode"),
        () => target.setupCode,
        _.foreach(target.setSetupCode),
        () => apply()),
      compiler, colorizer, true, true, err = () => target.error("setupCode"))

  private val updateCode =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.updateCode"),
        () => target.updateCode,
        _.foreach(target.setUpdateCode),
        () => apply()),
      compiler, colorizer, true, true, err = () => target.error("updateCode"))

  private val editPlotPens =
    new PlotPensEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.pen.plotPens"),
        () => target.editPlotPens,
        _.foreach(target.setEditPlotPens),
        () => apply()),
      compiler, colorizer, target)

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  setupCode.addFocusListener(new FocusAdapter {
    override def focusLost(e: FocusEvent): Unit = {
      target.compile()
      setupCode.resetError()
    }
  })

  updateCode.addFocusListener(new FocusAdapter {
    override def focusLost(e: FocusEvent): Unit = {
      target.compile()
      updateCode.resetError()
    }
  })

  add(plotName)
  add(new BoxRow(Seq(xLabel, xMin, xMax), 6) with MaximumHeight)
  add(new BoxRow(Seq(yLabel, yMin, yMax), 6) with MaximumHeight)
  add(new BoxRow(Seq(autoPlotX, autoPlotY, showLegend), 6, BoxAlign.Start) with MaximumHeight)
  add(runtimeError)
  add(setupCode)
  add(updateCode)
  add(editPlotPens)
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(plotName, xLabel, xMin, xMax, yLabel, yMin, yMax, autoPlotX, autoPlotY, showLegend, runtimeError, setupCode,
        updateCode, editPlotPens, oldSize)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    plotName.requestFocus()
  }
}
