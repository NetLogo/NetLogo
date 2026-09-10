// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxColumn, BoxRow }

class SliderEditPanel(target: SliderWidget, compiler: CompilerServices, colorizer: Colorizer,
                      extensionManager: ExtensionManager) extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.globalVar"),
        () => target.name,
        name => target.setNameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val minimumCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.minimum"),
        () => target.minimumCode,
        _.foreach(target.setMinimumCode),
        () => apply()),
      compiler, colorizer, false, () => target.error(SliderConstraint.Min.fieldName))

  private val minimumLabeled = new LabeledEditor(minimumCode, I18N.gui.get("edit.slider.minmax.message"))

  private val incrementCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.increment"),
        () => target.incrementCode,
        _.foreach(target.setIncrementCode),
        () => apply()),
      compiler, colorizer, false, () => target.error(SliderConstraint.Inc.fieldName))

  private val maximumCode: ReporterLineEditor =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.maximum"),
        () => target.maximumCode,
        _.foreach(target.setMaximumCode),
        () => apply()),
      compiler, colorizer, false, () => target.error(SliderConstraint.Max.fieldName))

  private val value =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.value"),
        () => target.value,
        _.foreach(target.setValue),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.units"),
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val vertical: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.vertical"),
        () => target.vertical,
        _.foreach(target.setVertical),
        () => apply(vertical.get.toOption.exists(_ != vertical.originalValue))))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply(vertical.get.toOption.exists(_ != vertical.originalValue))))

  add(nameWrapper)
  add(new BoxRow(Seq(
    minimumLabeled,
    new BoxColumn(incrementCode, BoxAlign.Start),
    new BoxColumn(maximumCode, BoxAlign.Start)
  ), 6))
  add(new BoxRow(Seq(value, units), 6))
  add(new BoxRow(vertical, BoxAlign.Start))
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, minimumCode, incrementCode, maximumCode, value, units, vertical, oldSize)

  override def syncExtraComponents(): Unit = {
    minimumLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }

  override def autoFill(): Boolean =
    AutomationUtils.sendChars(nameWrapper, s"test-${System.currentTimeMillis}")
}
