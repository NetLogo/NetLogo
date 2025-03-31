// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class PlotEditPanel(target: PlotWidget, colorizer: Colorizer) extends WidgetEditPanel(target) {
  private val plotName =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.name"),
        () => target.plotName,
        target.setPlotName(_),
        () => apply())) {

      override def get: Option[String] =
        super.get.filter(_.nonEmpty)
    }

  private val xLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xLabel"),
        () => target.xLabel,
        target.setXLabel(_),
        () => apply()))

  private val xMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xmin"),
        () => target.defaultXMin,
        target.setDefaultXMin(_),
        () => apply()))

  private val xMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.xmax"),
        () => target.defaultXMax,
        target.setDefaultXMax(_),
        () => apply()))

  private val yLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.yLabel"),
        () => target.yLabel,
        target.setYLabel(_),
        () => apply()))

  private val yMin =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.ymin"),
        () => target.defaultYMin,
        target.setDefaultYMin(_),
        () => apply()))

  private val yMax =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.ymax"),
        () => target.defaultYMax,
        target.setDefaultYMax(_),
        () => apply()))

  private val autoPlotX =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.autoScaleX"),
        () => target.defaultAutoPlotX,
        target.setDefaultAutoPlotX(_),
        () => apply()))

  private val autoPlotY =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.autoScaleY"),
        () => target.defaultAutoPlotY,
        target.setDefaultAutoPlotY(_),
        () => apply()))

  private val showLegend =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.showLegend"),
        () => target.showLegend,
        target.setShowLegend(_),
        () => apply()))

  private val runtimeError =
    new RuntimeErrorDisplay(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.error.runtimeError"),
        () => target.runtimeError,
        target.setRuntimeError(_),
        () => apply()))

  private val setupCode =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.setupCode"),
        () => target.setupCode,
        target.setSetupCode(_),
        () => apply()),
      colorizer, true, true)

  private val updateCode =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.updateCode"),
        () => target.updateCode,
        target.setUpdateCode(_),
        () => apply()),
      colorizer, true, true)

  private val editPlotPens =
    new PlotPensEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.plot.pen.plotPens"),
        () => target.editPlotPens,
        target.setEditPlotPens(_),
        () => apply()),
      colorizer, target)

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(plotName, c)

    c.gridy = 1
    c.gridwidth = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(xLabel, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(xMin, c)
    add(xMax, c)

    c.gridy = 2
    c.insets = new Insets(0, 6, 6, 6)

    add(yLabel, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(yMin, c)
    add(yMax, c)

    c.gridy = 3
    c.insets = new Insets(0, 6, 6, 6)

    add(autoPlotX, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(autoPlotY, c)
    add(showLegend, c)

    c.gridy = 4
    c.gridwidth = 3
    c.insets = new Insets(0, 6, 6, 6)

    add(runtimeError, c)

    c.gridy = 5

    add(setupCode, c)

    c.gridy = 6

    add(updateCode, c)

    c.gridy = 7

    add(editPlotPens, c)

    c.gridy = 8
    c.anchor = GridBagConstraints.WEST

    add(oldSize, c)

    plotName.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(plotName, xLabel, xMin, xMax, yLabel, yMin, yMax, autoPlotX, autoPlotY, showLegend, runtimeError, setupCode,
        updateCode, editPlotPens, oldSize)

  override def isResizable: Boolean = true

  override def syncTheme(): Unit = {
    plotName.syncTheme()
    xLabel.syncTheme()
    xMin.syncTheme()
    xMax.syncTheme()
    yLabel.syncTheme()
    yMin.syncTheme()
    yMax.syncTheme()
    autoPlotX.syncTheme()
    autoPlotY.syncTheme()
    showLegend.syncTheme()
    runtimeError.syncTheme()
    setupCode.syncTheme()
    updateCode.syncTheme()
    editPlotPens.syncTheme()
    oldSize.syncTheme()
  }
}
