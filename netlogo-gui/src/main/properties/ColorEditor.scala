// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ Color, Dimension, Frame, Graphics, GridBagLayout, GridBagConstraints, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.lang.Double
import javax.swing.{ JLabel, JPanel }

import org.nlogo.api.{ Color => NLColor }
import org.nlogo.swing.{ RoundedBorderPanel, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ JFXColorPicker, NumAndRGBA }

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

    add(label, c)

    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(0, 0, 0, 0)

    add(colorButton, c)

    setColor(originalColor)
  }

  def setColor(color: Color): Unit = {
    colorButton.setColor(color)
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
    private var color = Color.BLACK

    private val panel = new JPanel {
      override def getPreferredSize: Dimension =
        new Dimension(16, 16)

      override def paintComponent(g: Graphics): Unit = {
        val g2d = Utils.initGraphics2D(g)

        g2d.setColor(Color.WHITE)
        g2d.fillRect(0, 0, getWidth / 2, getHeight / 2)
        g2d.fillRect(getWidth / 2, getHeight / 2, getWidth / 2, getHeight / 2)

        g2d.setColor(new Color(200, 200, 200))
        g2d.fillRect(getWidth / 2, 0, getWidth / 2, getHeight / 2)
        g2d.fillRect(0, getHeight / 2, getWidth / 2, getHeight / 2)

        g2d.setColor(color)
        g2d.fillRect(0, 0, getWidth, getHeight)
      }
    }

    setDiameter(6)
    enableHover()
    enablePressed()

    add(panel)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        new JFXColorPicker(frame, true, NumAndRGBA,
          (x: String) => {

            val SimpleDouble = """^(\d{1,3}(?:\.\d)?)$""".r
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
      this.color = color

      repaint()
    }

    def getColor: Color =
      color

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground)
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover)
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed)
      setBorderColor(InterfaceColors.toolbarControlBorder)
    }
  }
}
