// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.{ Box, BoxLayout }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ AutomationUtils, BoxColumn, BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

class MonitorEditPanel(target: MonitorWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val wrapSource =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.reporter"),
        () => target.wrapSource,
        name => target.setWrapSource(name.getOrElse("")),
        () => apply()),
      compiler, colorizer, () => target.error())

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.name"),
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val decimalPlaces =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.decimalPlaces"),
        () => target.decimalPlaces,
        _.foreach(target.setDecimalPlaces),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.units"),
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val decimalLabeled = new LabeledEditor(decimalPlaces, I18N.gui.get("edit.monitor.precision"))

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.fontSize"),
        () => target.fontSize,
        _.foreach(target.setFontSize),
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

  add(wrapSource)
  add(new VerticalStrut(6))
  add(name)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(
    decimalLabeled,
    new HorizontalStrut(6),
    new BoxColumn(Seq(
      units,
      Box.createVerticalGlue
    ))
  )) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })
  add(new VerticalStrut(6))
  add(fontSize)
  add(new VerticalStrut(6))
  add(oldSize)

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
