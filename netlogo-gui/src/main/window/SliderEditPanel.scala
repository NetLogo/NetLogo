// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class SliderEditPanel(target: SliderWidget, compiler: CompilerServices, colorizer: Colorizer,
                      extensionManager: ExtensionManager) extends EditPanel(target) {

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
      compiler, colorizer, false)

  private val minimumLabeled = new LabeledEditor(minimumCode, I18N.gui.get("edit.slider.minmax.message"))

  private val incrementCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.increment"),
        () => target.incrementCode,
        _.foreach(target.setIncrementCode),
        () => apply()),
      compiler, colorizer, false)

  private val maximumCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.maximum"),
        () => target.maximumCode,
        _.foreach(target.setMaximumCode),
        () => apply()),
      compiler, colorizer, false)

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
        () => apply(vertical.get.exists(_ != vertical.originalValue))))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply(vertical.get.exists(_ != vertical.originalValue))))

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.gridy = 1
    c.gridwidth = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 6, 6, 6)

    add(minimumLabeled, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(incrementCode, c)
    add(maximumCode, c)

    c.gridy = 2
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(value, c)

    c.gridwidth = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 0, 6, 6)

    add(units, c)

    c.gridy = 3
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(vertical, c)

    c.gridy = 4

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, minimumCode, incrementCode, maximumCode, value, units, vertical, oldSize)

  override def syncExtraComponents(): Unit = {
    minimumLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
