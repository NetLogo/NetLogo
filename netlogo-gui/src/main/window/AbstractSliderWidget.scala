// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, Point }
import java.awt.event.{ ActionEvent, ActionListener, FocusAdapter, FocusEvent, KeyAdapter, KeyEvent, MouseAdapter,
                        MouseEvent, MouseWheelEvent, MouseWheelListener }
import java.lang.NumberFormatException
import javax.swing.{ BorderFactory, JLabel, JSlider, JTextField, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.text.{ AttributeSet, PlainDocument }

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.InputBoxLoseFocusEvent

import scala.math.Pi

trait AbstractSliderWidget extends MultiErrorWidget with ThemeSync {
  protected class Label(text: String) extends JLabel(text) {
    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)
      if (vertical) {
        g2d.setClip(null) // this does not feel right but it's the only thing that works for now (Isaac B 8/11/24)
        g2d.rotate(-Pi / 2)
      }
      super.paintComponent(g)
    }
  }

  protected class TextField extends JTextField("50", 3) with Transparent {
    setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2))
    setFont(getFont.deriveFont(11f))
    setHorizontalAlignment(SwingConstants.RIGHT)

    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent): Unit = {
        try {
          value = getText.toDouble
        } catch {
          case e: NumberFormatException =>
        }

        setText(valueString(value))

        getParent.requestFocus()
      }
    })

    addFocusListener(new FocusAdapter {
      override def focusLost(e: FocusEvent): Unit = {
        fireActionPerformed()
      }
    })

    override def getPreferredSize: Dimension =
      new Dimension(25.max(getFontMetrics(getFont).stringWidth(getText) + 6), super.getPreferredSize.height)

    protected override def createDefaultModel =
      new PlainDocument {
        override def insertString(offset: Int, str: String, attributes: AttributeSet): Unit = {
          super.insertString(offset, str, attributes)

          AbstractSliderWidget.this.revalidate()
        }
      }

    override def contains(x: Int, y: Int): Boolean = {
      if (vertical) {
        -y > 0 && x > 0 && -y < getWidth && x < getHeight
      } else {
        super.contains(x, y)
      }
    }

    override def contains(point: Point): Boolean = {
      if (vertical) {
        -point.y > 0 && point.x > 0 && -point.y < getWidth && point.x < getHeight
      } else {
        super.contains(point)
      }
    }

    override def processMouseEvent(e: MouseEvent): Unit = {
      e.translatePoint(-e.getPoint.y - e.getPoint.x, e.getPoint.x - e.getPoint.y)
      super.processMouseEvent(e)
    }

    override def processMouseMotionEvent(e: MouseEvent): Unit = {
      e.translatePoint(-e.getPoint.y - e.getPoint.x, e.getPoint.x - e.getPoint.y)
      super.processMouseMotionEvent(e)
    }

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)
      if (vertical) {
        g2d.setClip(null) // this does not feel right but it's the only thing that works for now (Isaac B 8/11/24)
        g2d.rotate(-Pi / 2)
      }
      g2d.setColor(InterfaceColors.inputBorder)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      g2d.setColor(InterfaceColors.displayAreaBackground)
      g2d.fillRoundRect(1, 1, getWidth - 2, getHeight - 2, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      super.paintComponent(g)
    }
  }

  protected var loading = false

  protected var _name = ""
  private var _units = ""
  private var _vertical = false
  private val sliderData = new SliderData(this)

  val nameComponent = new Label(I18N.gui.get("edit.slider.previewName"))
  val valueComponent = new TextField
  val unitsComponent = new Label("")
  val slider = new JSlider(0, ((maximum - minimum) / increment).toInt, 50)

  locally {
    val mouseListener = new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        new InputBoxLoseFocusEvent().raise(AbstractSliderWidget.this)

        requestFocus()
      }
    }

    addMouseListener(mouseListener)
    nameComponent.addMouseListener(mouseListener)
    unitsComponent.addMouseListener(mouseListener)
    slider.addMouseListener(mouseListener)

    val mouseWheelListener = new MouseWheelListener {
      def mouseWheelMoved(e: MouseWheelEvent): Unit = {
        slider.setValue(slider.getValue - e.getWheelRotation)
      }
    }

    addMouseWheelListener(mouseWheelListener)
    nameComponent.addMouseWheelListener(mouseWheelListener)
    unitsComponent.addMouseWheelListener(mouseWheelListener)
    slider.addMouseWheelListener(mouseWheelListener)

    val keyListener = new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        if (e.getKeyCode == KeyEvent.VK_LEFT) {
          slider.setValue(slider.getValue - 1)
        } else if (e.getKeyCode == KeyEvent.VK_RIGHT) {
          slider.setValue(slider.getValue + 1)
        }
      }
    }

    addKeyListener(keyListener)
    nameComponent.addKeyListener(keyListener)
    unitsComponent.addKeyListener(keyListener)
    slider.addKeyListener(keyListener)
  }

  slider.setUI(new SliderWidgetUI(this, slider))

  setLayout(null)

  add(nameComponent)
  add(valueComponent)
  add(unitsComponent)
  add(slider)

  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent): Unit = {
      if (!loading)
        value = minimum + slider.getValue * increment
    }
  })

  def constraint = sliderData.constraint
  def setSliderConstraint(con: SliderConstraint) = {
    slider.setMinimum(0)
    slider.setMaximum(((maximum - minimum) / increment).asInstanceOf[Int])
    sliderData.setSliderConstraint(con)
  }
  def name = _name
  def name_=(name:String): Unit = { _name = name; repaint() }
  def minimum = sliderData.minimum
  def maximum = sliderData.maximum
  def effectiveMaximum = sliderData.effectiveMaximum
  def increment = sliderData.increment
  def value = sliderData.value
  def value_=(d: Double): Unit = {
    sliderData.value = d
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).round.asInstanceOf[Int])
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d: Double, inc: Double): Unit = {
    sliderData.value = d
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / inc).round.asInstanceOf[Int])
    repaint()
  }
  def value_=(d: Double, buttonRelease: Boolean): Unit = {
    sliderData.value_=(d, buttonRelease)
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).round.asInstanceOf[Int])
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def coerceValue(value: Double): Double = {
    val ret = sliderData.coerceValue(value)
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).round.asInstanceOf[Int])
    repaint()
    ret
  }

  def units = _units
  def units_=(units: String): Unit = {
    _units = units.trim
    unitsComponent.setText(units.trim)
    revalidate()
    repaint()
  }

  def valueSetter(v: Double) = {
    if (sliderData.valueSetter(v)) {
      revalidate()
      repaint()
      true
    } else {
      false
    }
  }

  def vertical: Boolean = _vertical
  def vertical_=(vert: Boolean): Unit = {
    if (vert != vertical) {
      _vertical = vert
      resetZoomInfo()
      resetSizeInfo()
      if (vert) {
        slider.setOrientation(SwingConstants.VERTICAL)
      } else {
        slider.setOrientation(SwingConstants.HORIZONTAL)
      }
    }
  }

  def valueString(num: Double): String = {
    var numString = Dump.number(num)
    var place = numString.indexOf('.')
    val p = sliderData.precision
    if (p > 0) {
      if (place == -1) {
        numString += "."
        place = numString.length - 1
      } else {
        numString = numString.substring(0, numString.size.min(place + p + 1))
      }
    }
    if (place != -1 && numString.indexOf('E') == -1) {
      val padding = p - (numString.length - place - 1)
      numString = numString + ("0" * padding)
    }
    numString
  }

  override def doLayout(): Unit = {
    if (_oldSize) {
      if (vertical) {
        nameComponent.setBounds(0, getHeight - 6, nameComponent.getPreferredSize.width.min(
                                                    getHeight - unitsComponent.getPreferredSize.width -
                                                    valueComponent.getPreferredSize.width - 18),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(0, unitsComponent.getPreferredSize.width + 6, unitsComponent.getPreferredSize.width,
                                 unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(0, unitsComponent.getPreferredSize.width + valueComponent.getPreferredSize.width + 12,
                                 valueComponent.getPreferredSize.width, valueComponent.getPreferredSize.height)
        slider.setBounds(getWidth - (slider.getPreferredSize.width * zoomFactor).toInt, 0,
                         (slider.getPreferredSize.width * zoomFactor).toInt, getHeight)
      } else {
        nameComponent.setBounds(6, 0, nameComponent.getPreferredSize.width.min(
                                        getWidth - unitsComponent.getPreferredSize.width -
                                        valueComponent.getPreferredSize.width - 18),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width - 6, 0,
                                 unitsComponent.getPreferredSize.width, unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width -
                                 valueComponent.getPreferredSize.width - 12, 0, valueComponent.getPreferredSize.width,
                                 valueComponent.getPreferredSize.height)
        slider.setBounds(0, getHeight - (slider.getPreferredSize.height * zoomFactor).toInt, getWidth,
                         (slider.getPreferredSize.height * zoomFactor).toInt)
      }
    } else {
      if (vertical) {
        nameComponent.setBounds(6, getHeight - 12, nameComponent.getPreferredSize.width.min(
                                                     getHeight - unitsComponent.getPreferredSize.width -
                                                     valueComponent.getPreferredSize.width - 30),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(6, unitsComponent.getPreferredSize.width + 12, unitsComponent.getPreferredSize.width,
                                 unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(6, unitsComponent.getPreferredSize.width + valueComponent.getPreferredSize.width + 18,
                                 valueComponent.getPreferredSize.width, valueComponent.getPreferredSize.height)
        slider.setBounds(getWidth - slider.getPreferredSize.width - 6, 6, slider.getPreferredSize.width, getHeight - 12)
      } else {
        nameComponent.setBounds(12, 6, nameComponent.getPreferredSize.width.min(
                                        getWidth - unitsComponent.getPreferredSize.width -
                                        valueComponent.getPreferredSize.width - 36),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width - 12, 6,
                                 unitsComponent.getPreferredSize.width, unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width -
                                   valueComponent.getPreferredSize.width - 18, 6,
                                 valueComponent.getPreferredSize.width, valueComponent.getPreferredSize.height)
        slider.setBounds(6, getHeight - slider.getPreferredSize.height - 6, getWidth - 12, slider.getPreferredSize.height)
      }
    }

    if (nameComponent.getPreferredSize.width > nameComponent.getWidth) {
      nameComponent.setToolTipText(nameComponent.getText)
    } else {
      nameComponent.setToolTipText(null)
    }
  }

  override def getMinimumSize = {
    if (_oldSize) {
      if (vertical) {
        new Dimension(33, 92)
      } else {
        new Dimension(92, 33)
      }
    } else {
      if (vertical) {
        new Dimension(55, 150)
      } else {
        new Dimension(150, 55)
      }
    }
  }

  override def getMaximumSize = {
    if (_oldSize) {
      if (vertical) {
        new Dimension(33, 10000)
      } else {
        new Dimension(10000, 33)
      }
    } else {
      if (vertical) {
        new Dimension(55, 10000)
      } else {
        new Dimension(10000, 55)
      }
    }
  }

  override def getPreferredSize = {
    if (_oldSize) {
      if (vertical) {
        new Dimension(33, 150)
      } else {
        new Dimension(150, 33)
      }
    } else {
      if (vertical) {
        new Dimension(55, 250)
      } else {
        new Dimension(250, 55)
      }
    }
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.sliderBackground)

    valueComponent.setForeground(InterfaceColors.displayAreaText)
    unitsComponent.setForeground(InterfaceColors.widgetText)

    valueComponent.setCaretColor(InterfaceColors.displayAreaText)
  }

  override def paintComponent(g: Graphics): Unit = {
    if (anyErrors) {
      nameComponent.setForeground(InterfaceColors.widgetTextError)
    } else {
      nameComponent.setForeground(InterfaceColors.widgetText)
    }

    super.paintComponent(g)
  }
}
