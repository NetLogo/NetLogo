// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import java.lang.NumberFormatException
import javax.swing.JLabel

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.theme.InterfaceColors

class SliderEditPanel(target: SliderWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.globalVar"),
        () => target.name,
        target.setNameWrapper(_),
        () => apply()),
      compiler)

  private val minimumCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.minimum"),
        () => target.minimumCode,
        target.setMinimumCode(_),
        () => apply()),
      colorizer, false)

  private val minimumLabel = new JLabel(I18N.gui.get("edit.slider.minmax.message")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val incrementCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.increment"),
        () => target.incrementCode,
        target.setIncrementCode(_),
        () => apply()),
      colorizer, false)

  private val maximumCode =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.maximum"),
        () => target.maximumCode,
        target.setMaximumCode(_),
        () => apply()),
      colorizer, false)

  private val value =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.value"),
        () => target.value,
        target.setValue(_),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.units"),
        () => target.units,
        target.setUnits(_),
        () => apply()))

  private val vertical =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.vertical"),
        () => target.vertical,
        target.setVertical(_),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.gridy = 1
    c.gridwidth = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 6, 3, 6)

    add(minimumCode, c)

    c.insets = new Insets(0, 0, 3, 6)

    add(incrementCode, c)
    add(maximumCode, c)

    c.gridy = 2
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(0, 6, 6, 6)

    add(minimumLabel, c)

    c.gridy = 3
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    add(value, c)

    c.gridwidth = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 0, 6, 6)

    add(units, c)

    c.gridy = 4
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(vertical, c)

    c.gridy = 5

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(nameWrapper, minimumCode, incrementCode, maximumCode, value, units, vertical, oldSize)

  override def isResizable: Boolean = true

  override def errorString: Option[String] = {
    // if everything can be parsed as a number, might as well check that the range is valid
    // otherwise, it's probably code, so ignore it and let the compiler figure it out
    // (Isaac B 2/11/25)
    try {
      if (target.checkRecursive(compiler, minimumCode.get.get, target.name) ||
          target.checkRecursive(compiler, maximumCode.get.get, target.name) ||
          target.checkRecursive(compiler, incrementCode.get.get, target.name)) {
        return Some(I18N.gui.get("edit.general.recursive"))
      } else if (minimumCode.get.get.toDouble >= maximumCode.get.get.toDouble) {
        return Some(I18N.gui.get("edit.slider.invalidBounds"))
      } else if (incrementCode.get.get.toDouble > maximumCode.get.get.toDouble - minimumCode.get.get.toDouble) {
        return Some(I18N.gui.get("edit.slider.invalidIncrement"))
      }
    } catch {
      case e: NumberFormatException =>
    }

    None
  }

  override def syncTheme(): Unit = {
    nameWrapper.syncTheme()
    minimumCode.syncTheme()
    incrementCode.syncTheme()
    maximumCode.syncTheme()
    value.syncTheme()
    units.syncTheme()
    vertical.syncTheme()
    oldSize.syncTheme()

    minimumLabel.setForeground(InterfaceColors.dialogText)
  }
}
