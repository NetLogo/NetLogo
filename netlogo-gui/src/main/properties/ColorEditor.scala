// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ Color, Dimension, Frame }
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.lang.Double
import javax.swing.{ JLabel, JPanel }

import org.nlogo.api.Approximate.approximate
import org.nlogo.api.{ Color => NLColor, Dump }, NLColor.{ getClosestColorNumberByARGB, getColorNameByIndex }
import org.nlogo.swing.RoundedBorderPanel
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.ColorDialog

abstract class ColorEditor(accessor: PropertyAccessor[Color], frame: Frame)
  extends PropertyEditor(accessor) {

  private val colorButton = new ColorButton
  private val originalColor: Color = accessor.get

  private val label = new JLabel(accessor.displayName)
  add(label)
  add(colorButton)
  setColor(originalColor)

  def setColor(color: Color) {
    colorButton.setColor(color)
    val c = getClosestColorNumberByARGB(color.getRGB)
    val colorString = c match {
      // this logic is duplicated in InputBox
      // black and white are special cases
      case 0 => "0 (black)"
      case 9.9 => "9.9 (white)"
      case _ =>
        val index = (c / 10).toInt
        val baseColor = index * 10 + 5
        Dump.number(c) + " (" + getColorNameByIndex(index) + (
          if (c > baseColor) {" + " + Dump.number(approximate(c - baseColor, 1))}
          else if (c < baseColor) {" - "} + Dump.number(approximate(baseColor - c, 1))
          else ""
        ) + ")"
    }
    colorButton.setText(colorString)
  }

  override def getMinimumSize = colorButton.getMinimumSize
  override def get = Some(colorButton.getColor)
  override def set(value: Color) { setColor(value) }
  override def requestFocus() { colorButton.requestFocus() }

  override def revert() {
    setColor(originalColor)
    super.revert()
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    colorButton.syncTheme()
  }

  private class ColorButton extends JPanel with RoundedBorderPanel with ThemeSync {
    private val label = new JLabel("0 (black)")
    private val panel = new JPanel {
      setBackground(Color.black)
      setPreferredSize(new Dimension(10, 10))
    }

    setDiameter(6)
    enableHover()

    add(panel)
    add(label)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        val colorDialog = new ColorDialog(frame, true)
        val c = colorDialog.showInputBoxDialog(getClosestColorNumberByARGB(getColor.getRGB))
        ColorEditor.this.setColor(NLColor.getColor(c: Double))
      }
    })

    def setColor(color: Color) {
      panel.setBackground(color)
    }

    def getColor: Color =
      panel.getBackground

    def setText(text: String) {
      label.setText(text)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      label.setForeground(InterfaceColors.TOOLBAR_TEXT)
    }
  }
}
