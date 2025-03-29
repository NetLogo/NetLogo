// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ Color, Dimension, Frame, GridBagLayout, GridBagConstraints, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.lang.Double
import javax.swing.{ JLabel, JPanel }

import org.nlogo.api.Approximate.approximate
import org.nlogo.api.{ Color => NLColor, Dump }, NLColor.{ getClosestColorNumberByARGB, getColorNameByIndex }
import org.nlogo.swing.{ RoundedBorderPanel, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ JFXColorPicker, RGBAOnly }

abstract class ColorEditor(accessor: PropertyAccessor[Color], frame: Frame)
  extends PropertyEditor(accessor) {

  private val colorButton = new ColorButton
  private val originalColor: Color = accessor.get

  private val label = new JLabel(accessor.displayName)

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 0, 0, 6)

    add(new JPanel(new GridBagLayout) with Transparent {
      add(label, new GridBagConstraints)
    }, c)

    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 0, 0, 0)

    add(colorButton, c)

    setColor(originalColor)
  }

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

  override def get = Some(colorButton.getColor)
  override def set(value: Color) { setColor(value) }
  override def requestFocus() { colorButton.requestFocus() }

  override def revert() {
    setColor(originalColor)
    super.revert()
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText)

    colorButton.syncTheme()
  }

  private class ColorButton extends JPanel with RoundedBorderPanel with ThemeSync {
    private val label = new JLabel("0 (black)")
    private val panel = new JPanel {
      setBackground(Color.black)

      override def getPreferredSize: Dimension =
        new Dimension(10, 10)
    }

    setDiameter(6)
    enableHover()
    enablePressed()

    add(panel)
    add(label)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new JFXColorPicker(frame, true, RGBAOnly,
          (x: String) => {

            val SimpleDouble = """^(\d{1,3}(?:\.\d))$""".r
            val AdvRGBA      = """^\[(\d{1,3}) (\d{1,3}) (\d{1,3}) (\d{1,3})\]$""".r

            x match {
              case SimpleDouble(d)     => ColorEditor.this.setColor(NLColor.getColor(d.toDouble.asInstanceOf[Double]))
              case AdvRGBA(r, g, b, a) => ColorEditor.this.setColor(new Color(r.toInt, g.toInt, b.toInt, a.toInt))
              case _                   => throw new Exception(s"Color picker returned unrecognized color format: $x")
            }

          }
        ).setVisible(true)
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
      setBackgroundColor(InterfaceColors.toolbarControlBackground)
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover)
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed)
      setBorderColor(InterfaceColors.toolbarControlBorder)

      label.setForeground(InterfaceColors.toolbarText)
    }
  }
}
