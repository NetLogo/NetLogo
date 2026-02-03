// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Graphics, LinearGradientPaint }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, ComboBox, PackedLayout, Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class ThemeColorPicker(updateColor: Color => Unit) extends JPanel with Transparent with ThemeSync {
  private var mode: Mode = RGBA

  private var copied: Option[Color] = None

  private val rhLabel = new JLabel("R")
  private val gsLabel = new JLabel("G")
  private val bLabel = new JLabel("B")
  private val aLabel = new JLabel("A")

  private val rhSlider = new Slider(0)
  private val gsSlider = new Slider(1)
  private val bSlider = new Slider(2)
  private val aSlider = new Slider(3)

  private val preview = new PreviewPanel

  private val modeLabel = new JLabel(I18N.gui.get("menu.tools.themeEditor.mode"))
  private val modeDropdown = new ComboBox(Seq(RGBA, HSBA)) {
    addItemListener( _ => getSelectedItem.foreach(setMode))

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, super.getMinimumSize.height)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize
  }

  private val copyButton = new Button(I18N.gui.get("menu.tools.themeEditor.copy"), copyColor)
  private val pasteButton = new Button(I18N.gui.get("menu.tools.themeEditor.paste"), pasteColor)

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  add(new PackedLayout(Seq(modeLabel, modeDropdown, copyButton, pasteButton), alignment = PackedLayout.Leading,
                       spacing = 6))
  add(Box.createVerticalStrut(6))
  add(new PackedLayout(Seq(rhLabel, rhSlider), spacing = 6))
  add(Box.createVerticalStrut(6))
  add(new PackedLayout(Seq(gsLabel, gsSlider), spacing = 6))
  add(Box.createVerticalStrut(6))
  add(new PackedLayout(Seq(bLabel, bSlider), spacing = 6))
  add(Box.createVerticalStrut(6))
  add(new PackedLayout(Seq(aLabel, aSlider), spacing = 6))
  add(Box.createVerticalStrut(6))
  add(new PackedLayout(Seq(preview), alignment = PackedLayout.Trailing))
  add(Box.createVerticalGlue)

  private def setMode(mode: Mode): Unit = {
    val color = getColor

    this.mode = mode

    setColor(color)

    rhLabel.setText(mode.toString()(0).toString)
    gsLabel.setText(mode.toString()(1).toString)

    repaint()
  }

  private def getColor: Color = {
    mode match {
      case RGBA =>
        new Color(rhSlider.rgbValue, gsSlider.rgbValue, bSlider.rgbValue, aSlider.rgbValue)

      case HSBA =>
        hsba(rhSlider.value, gsSlider.value, bSlider.value, aSlider.rgbValue)
    }
  }

  def setColor(color: Color): Unit = {
    mode match {
      case RGBA =>
        rhSlider.setValue(color.getRed / 255f)
        gsSlider.setValue(color.getGreen / 255f)
        bSlider.setValue(color.getBlue / 255f)
        aSlider.setValue(color.getAlpha / 255f)

      case HSBA =>
        val comps = Color.RGBtoHSB(color.getRed, color.getGreen, color.getBlue, null)

        rhSlider.setValue(comps(0))
        gsSlider.setValue(comps(1))
        bSlider.setValue(comps(2))
        aSlider.setValue(color.getAlpha / 255f)
    }

    repaint()
    updateColor(getColor)
  }

  private def copyColor(): Unit = {
    copied = Option(getColor)
  }

  private def pasteColor(): Unit = {
    copied.foreach(setColor)
  }

  private def hsba(h: Float, s: Float, b: Float, a: Int = 255): Color = {
    val hsb = Color.getHSBColor(h, s, b)

    new Color(hsb.getRed, hsb.getGreen, hsb.getBlue, a)
  }

  override def syncTheme(): Unit = {
    rhLabel.setForeground(InterfaceColors.dialogText())
    gsLabel.setForeground(InterfaceColors.dialogText())
    bLabel.setForeground(InterfaceColors.dialogText())
    aLabel.setForeground(InterfaceColors.dialogText())
    modeLabel.setForeground(InterfaceColors.dialogText())

    modeDropdown.syncTheme()
    copyButton.syncTheme()
    pasteButton.syncTheme()
  }

  private sealed abstract trait Mode(str: String) {
    override def toString: String =
      str
  }

  private case object RGBA extends Mode("RGBA")
  private case object HSBA extends Mode("HSBA")

  private class Slider(which: Int) extends JPanel with Transparent {
    private var position = getWidth / 2

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        position = e.getX.min(getWidth).max(0)

        ThemeColorPicker.this.repaint()
        updateColor(getColor)
      }
    })

    addMouseMotionListener(new MouseAdapter {
      override def mouseDragged(e: MouseEvent): Unit = {
        position = e.getX.min(getWidth).max(0)

        ThemeColorPicker.this.repaint()
        updateColor(getColor)
      }
    })

    def value: Float =
      (position.toFloat / getWidth).min(1).max(0)

    def rgbValue: Int =
      (value * 255).toInt

    def setValue(value: Float): Unit = {
      position = (value * getWidth).toInt
    }

    override def getPreferredSize: Dimension =
      new Dimension(200, 25)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarControlBorder())
      g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)

      mode match {
        case RGBA =>
          val (r, g, b) = (rhSlider.rgbValue, gsSlider.rgbValue, bSlider.rgbValue)

          which match {
            case 0 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(new Color(0, g, b), new Color(255, g, b))))

            case 1 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(new Color(r, 0, b), new Color(r, 255, b))))

            case 2 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(new Color(r, g, 0), new Color(r, g, 255))))

            case 3 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(new Color(r, g, b, 0), new Color(r, g, b, 255))))
          }

        case HSBA =>
          val (h, s, l) = (rhSlider.value, gsSlider.value, bSlider.value)

          which match {
            case 0 =>
              val fracs: Array[Float] = (0 to 10).map(_ / 10f).toArray

              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, fracs, fracs.map(hsba(_, 1, 1))))

            case 1 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(hsba(h, 0, l), hsba(h, 1, l))))

            case 2 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(hsba(h, s, 0), hsba(h, s, 1))))

            case 3 =>
              g2d.setPaint(new LinearGradientPaint(0, 0, getWidth.toFloat, 0, Array(0f, 1f),
                                                   Array(hsba(h, s, l, 0), hsba(h, s, l, 255))))
          }
      }

      g2d.fillRoundRect(1, 1, getWidth - 2, getHeight - 2, 6, 6)

      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect((position - 2).min(getWidth - 4).max(0), 1, 4, getHeight - 2, 6, 6)
    }
  }

  private class PreviewPanel extends JPanel with Transparent {
    override def getPreferredSize: Dimension =
      new Dimension(rhSlider.getPreferredSize.width, 150)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(getColor)
      g2d.fillRoundRect(1, 1, getWidth - 2, getHeight - 2, 6, 6)

      g2d.setColor(InterfaceColors.toolbarControlBorder())
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)
    }
  }
}
