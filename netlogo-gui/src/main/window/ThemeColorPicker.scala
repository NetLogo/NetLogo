// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Container, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets,
                  LinearGradientPaint }
import java.awt.event.{ FocusAdapter, FocusEvent, MouseAdapter, MouseEvent }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.api.Approximate
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, ComboBox, Implicits, PackedLayout, TextField, Transparent, Utils },
  Implicits.thunk2documentListener
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class ThemeColorPicker(updateColor: Color => Unit) extends JPanel(new GridBagLayout) with Transparent with ThemeSync {
  private var mode: Mode = RGBA

  private var copied: Option[Color] = None

  private val rhSlider = new ColorSlider(0)
  private val gsSlider = new ColorSlider(1)
  private val bSlider = new ColorSlider(2)
  private val aSlider = new ColorSlider(3)

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

  locally {
    val c = new GridBagConstraints

    c.gridwidth = 3
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(0, 0, 6, 0)

    add(new PackedLayout(Seq(modeLabel, modeDropdown, copyButton, pasteButton), spacing = 6), c)

    c.gridx = GridBagConstraints.RELATIVE
    c.gridy = 1
    c.gridwidth = 1
    c.insets = new Insets(0, 0, 6, 6)

    rhSlider.addComponents(this, c)

    c.gridy = 2

    gsSlider.addComponents(this, c)

    c.gridy = 3

    bSlider.addComponents(this, c)

    c.gridy = 4

    aSlider.addComponents(this, c)

    c.gridx = 1
    c.gridy = 5
    c.weighty = 1
    c.anchor = GridBagConstraints.NORTHWEST

    add(new PreviewPanel, c)
  }

  private def setMode(mode: Mode): Unit = {
    val color = getColor

    this.mode = mode

    setColor(color)

    rhSlider.updateLabel()
    gsSlider.updateLabel()

    repaint()
  }

  private def getColor: Color = {
    mode match {
      case RGBA =>
        new Color(rhSlider.intValue, gsSlider.intValue, bSlider.intValue, aSlider.intValue)

      case HSBA =>
        hsba(rhSlider.floatValue, gsSlider.floatValue, bSlider.floatValue, aSlider.intValue)
    }
  }

  def setColor(color: Color): Unit = {
    mode match {
      case RGBA =>
        rhSlider.setValue(IntValue(color.getRed))
        gsSlider.setValue(IntValue(color.getGreen))
        bSlider.setValue(IntValue(color.getBlue))

      case HSBA =>
        val comps = Color.RGBtoHSB(color.getRed, color.getGreen, color.getBlue, null)

        rhSlider.setValue(FloatValue(comps(0)))
        gsSlider.setValue(FloatValue(comps(1)))
        bSlider.setValue(FloatValue(comps(2)))
    }

    aSlider.setValue(IntValue(color.getAlpha))

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
    modeLabel.setForeground(InterfaceColors.dialogText())

    rhSlider.syncTheme()
    gsSlider.syncTheme()
    bSlider.syncTheme()
    aSlider.syncTheme()
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

  private sealed abstract trait ColorValue {
    def intValue: Int
    def floatValue: Float
  }

  private case class IntValue(value: Int) extends ColorValue {
    override def intValue: Int =
      value

    override def floatValue: Float =
      value.toFloat
  }

  private case class FloatValue(value: Float) extends ColorValue {
    override def intValue: Int =
      value.toInt

    override def floatValue: Float =
      value
  }

  private class ColorField(which: Int, slider: ColorSlider) extends TextField(3) {
    private var updating = false

    getDocument.addDocumentListener(() => updateSlider())

    addFocusListener(new FocusAdapter {
      override def focusLost(e: FocusEvent): Unit = {
        slider.updateField()
      }
    })

    override def setText(text: String): Unit = {
      if (!updating) {
        updating = true

        super.setText(text)

        updating = false
      }
    }

    private def getValue(): Option[ColorValue] = {
      mode match {
        case RGBA =>
          getText.toIntOption.filter(n => n >= 0 && n <= 255).map(IntValue(_))

        case HSBA =>
          which match {
            case 0 =>
              getText.toFloatOption.filter(n => n >= 0 && n <= 360).map(n => FloatValue(n / 360))

            case 3 =>
              getText.toIntOption.filter(n => n >= 0 && n <= 255).map(IntValue(_))

            case _ =>
              getText.toFloatOption.filter(n => n >= 0 && n <= 1).map(FloatValue(_))
          }
      }
    }

    private def updateSlider(): Unit = {
      if (!updating) {
        updating = true

        getValue().foreach { value =>
          slider.setValue(value)
          setColor(getColor)
        }

        updating = false
      }
    }
  }

  private class ColorSlider(which: Int) extends JPanel with Transparent with ThemeSync {
    private val label = new JLabel
    private val field = new ColorField(which, this)

    private var value: ColorValue = IntValue(0)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        updateValue(e.getX)
      }
    })

    addMouseMotionListener(new MouseAdapter {
      override def mouseDragged(e: MouseEvent): Unit = {
        updateValue(e.getX)
      }
    })

    updateLabel()
    updateField()

    def intValue: Int =
      value.intValue

    def floatValue: Float =
      value.floatValue

    def setValue(value: ColorValue): Unit = {
      this.value = value

      updateField()
    }

    def updateLabel(): Unit = {
      label.setText(mode.toString(which).toString)
    }

    private def updateValue(pos: Int): Unit = {
      val position = pos.min(getWidth).max(0)

      mode match {
        case HSBA if which != 3 =>
          value = FloatValue(position.toFloat / getWidth)

        case _ =>
          value = IntValue(position * 255 / getWidth)
      }

      ThemeColorPicker.this.repaint()

      updateColor(getColor)
      updateField()
    }

    def updateField(): Unit = {
      value match {
        case IntValue(n) =>
          field.setText(n.toString)

        case FloatValue(n) =>
          if (which == 0) {
            field.setText((n * 360).round.toString)
          } else {
            field.setText(Approximate.approximate(n, 3).toString)
          }
      }
    }

    def addComponents(container: Container, c: GridBagConstraints): Unit = {
      container.add(label, c)
      container.add(this, c)
      container.add(field, c)
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
          val (r, g, b) = (rhSlider.intValue, gsSlider.intValue, bSlider.intValue)

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
          val (h, s, l) = (rhSlider.floatValue, gsSlider.floatValue, bSlider.floatValue)

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

      val position: Int = {
        value match {
          case IntValue(n) =>
            n * getWidth / 255

          case FloatValue(n) =>
            (n * getWidth).toInt
        }
      }

      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect((position - 2).min(getWidth - 4).max(0), 1, 4, getHeight - 2, 6, 6)
    }

    override def syncTheme(): Unit = {
      label.setForeground(InterfaceColors.dialogText())

      field.syncTheme()
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
