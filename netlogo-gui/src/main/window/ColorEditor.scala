// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Frame, Graphics }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.swing.{ HorizontalStrut, PreferredSize, RoundedBorderPanel, Utils, Zoomable }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.util.Success

class ColorEditor(accessor: PropertyAccessor[Color], frame: Frame) extends PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name)
  private val colorButton = new ColorButton

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(label)
  add(new HorizontalStrut(6))
  add(Box.createHorizontalGlue)
  add(colorButton)

  setColor(originalValue)

  def setColor(color: Color): Unit = {
    colorButton.setColor(color)
  }

  override def get = Success(colorButton.getColor)
  override def set(value: Color): Unit = { setColor(value) }
  override def requestFocus(): Unit = { colorButton.requestFocus() }

  override def revert(): Unit = {
    setColor(originalValue)
    super.revert()
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    colorButton.syncTheme()
  }

  private class ColorButton extends JPanel with RoundedBorderPanel with PreferredSize with Zoomable with ThemeSync {
    private var color = Color.BLACK

    private val panel = new JPanel with PreferredSize {
      override def getPreferredSize: Dimension =
        new Dimension(Utils.zoom(16), Utils.zoom(16))

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

    setDiameter(Utils.zoom(6))
    enableHover()
    enablePressed()

    add(panel)

    addMouseListener(new MouseAdapter {

      override def mousePressed(e: MouseEvent): Unit = {

        val initialOpt = Option(RGBA.fromJavaColor(color))
        val colorOpt   = initialOpt.flatMap(NLNumber.fromRGBA).orElse(initialOpt)

        new JFXColorPicker(frame, true, NumAndRGBA, colorOpt,
          (x: String) => {

            val SimpleDouble = """^(\d{1,3}(?:\.\d)?)$""".r
            val AdvRGB       = """^\[(\d{1,3}) (\d{1,3}) (\d{1,3})\]$""".r
            val AdvRGBA      = """^\[(\d{1,3}) (\d{1,3}) (\d{1,3}) (\d{1,3})\]$""".r

            val thisValue =
              x match {
                case SimpleDouble(d) =>
                  NLNumber(d.toDouble)
                case AdvRGB(r, g, b) =>
                  RGB(r.toInt, g.toInt, b.toInt)
                case AdvRGBA(r, g, b, a) =>
                  RGBA(r.toInt, g.toInt, b.toInt, a.toInt)
                case _ =>
                  throw new Exception(s"Color picker returned unrecognized color format: $x")
              }

            ColorEditor.this.setColor(thisValue.toColor)

          }
        ).setVisible(true)

      }

    })

    def setColor(color: Color): Unit = {
      this.color = color

      repaint()
    }

    def getColor: Color =
      color

    override def zoom(oldZoom: Float): Unit = {
      setDiameter(Utils.zoom(6))
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())
    }
  }
}
