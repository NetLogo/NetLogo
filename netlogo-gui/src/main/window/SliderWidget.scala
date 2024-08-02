// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets, RadialGradientPaint }
import java.awt.event.{ ActionEvent, ActionListener, FocusAdapter, FocusEvent, MouseAdapter, MouseEvent,
                        MouseMotionAdapter }
import javax.swing.{ BorderFactory, JLabel, JSlider, JTextField, SwingConstants }
import javax.swing.plaf.basic.BasicSliderUI

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.{ Dump, Editable, MersenneTwisterFast }
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }
import org.nlogo.swing.Utils
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent,
                                 InputBoxLoseFocusEvent }

trait AbstractSliderWidget extends MultiErrorWidget {
  private class SliderUI(slider: JSlider) extends BasicSliderUI(slider) {
    private var hover = false

    slider.setOpaque(false)

    slider.addMouseListener(new MouseAdapter {
      override def mouseReleased(e: MouseEvent) {
        if (!thumbRect.contains(e.getPoint))
          hover = false
      }
    })

    slider.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent) {
        hover = thumbRect.contains(e.getPoint)
      }
    })

    override def paintTrack(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      val startY = trackRect.y + trackRect.height / 2 - 3
      g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND_FILLED)
      g2d.fillRoundRect(trackRect.x, startY, thumbRect.x, 6, 6, 6)
      g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND)
      g2d.fillRoundRect(thumbRect.x, startY, trackRect.width - thumbRect.x, 6, 6, 6)
    }

    override def paintThumb(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      if (hover) {
        g2d.setPaint(new RadialGradientPaint(thumbRect.getCenterX.toInt, thumbRect.getCenterY.toInt,
                                             getThumbSize.width, Array[Float](0, 1),
                                             Array(InterfaceColors.SLIDER_SHADOW, InterfaceColors.TRANSPARENT)))
        g2d.fillOval(thumbRect.x + getThumbSize.width / 2 - getThumbSize.height / 2, thumbRect.y, getThumbSize.height,
                    getThumbSize.height)
      }
      val startY = getThumbSize.height / 2 - getThumbSize.width / 2
      g2d.setColor(InterfaceColors.SLIDER_THUMB_BORDER)
      g2d.fillOval(thumbRect.x, startY, getThumbSize.width, getThumbSize.width)
      if (isDragging)
        g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND_PRESSED)
      else
        g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND)
      g2d.fillOval(thumbRect.x + 1, startY + 1, getThumbSize.width - 2, getThumbSize.width - 2)
      repaint()
    }

    override def paintFocus(g: Graphics) {
      // no focus
    }

    override def scrollDueToClickInTrack(dir: Int) {
      // implemented in track listener
    }

    override def createTrackListener(slider: JSlider): TrackListener =
      new TrackListener {
        override def mousePressed(e: MouseEvent) {
          if (thumbRect.contains(e.getPoint))
            super.mousePressed(e)
          else if (e.getButton == MouseEvent.BUTTON1)
            slider.setValue(valueForXPosition(e.getPoint.x))
        }
      }
  }

  protected class TextField extends JTextField {
    setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0))
    setBackground(InterfaceColors.TRANSPARENT)

    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        try {
          value = getText.toDouble
        }

        catch {
          case e: NumberFormatException =>
        }

        setText(value.toString)
      }
    })

    addFocusListener(new FocusAdapter {
      override def focusLost(e: FocusEvent) {
        fireActionPerformed()
      }
    })

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      g2d.setColor(InterfaceColors.INPUT_BORDER)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect(1, 1, getWidth - 2, getHeight - 2, 6, 6)
      super.paintComponent(g)
    }
  }

  protected var _name = ""
  private var _units = ""
  private var _vertical = false
  private val sliderData = new SliderData(this)

  val nameComponent = new JLabel
  val valueComponent = new TextField
  var slider = new JSlider(0, ((maximum - minimum) / increment).asInstanceOf[Int], 0)

  nameComponent.setForeground(InterfaceColors.WIDGET_TEXT)
  valueComponent.setForeground(InterfaceColors.WIDGET_TEXT)

  slider.setUI(new SliderUI(slider))

  backgroundColor = InterfaceColors.SLIDER_BACKGROUND

  setLayout(new GridBagLayout)

  locally {
    val margin = 6

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridy = 0
    c.anchor = GridBagConstraints.NORTHWEST
    c.gridwidth = 1
    c.weightx = 1
    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(-margin, margin, 0, margin)

    add(nameComponent, c)

    c.gridx = 1
    c.anchor = GridBagConstraints.NORTHEAST
    c.insets = new Insets(-margin, 0, 0, margin)

    add(valueComponent, c)

    c.gridx = 0
    c.gridy = 1
    c.anchor = GridBagConstraints.SOUTHWEST
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(0, 0, -margin, 0)

    add(slider, c)

    slider.addChangeListener(new javax.swing.event.ChangeListener() {
      override def stateChanged(e: javax.swing.event.ChangeEvent): Unit = {
        if (slider.hasFocus) {
          value = minimum + slider.getValue * increment
        }
      }
    })

    nameComponent.setFont(nameComponent.getFont.deriveFont(11f))
    valueComponent.setFont(valueComponent.getFont.deriveFont(11f))

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        new InputBoxLoseFocusEvent().raise(AbstractSliderWidget.this)
      }
    })
  }

  def constraint = sliderData.constraint
  def setSliderConstraint(con: SliderConstraint) = {
    slider.setMinimum(0)
    slider.setMaximum(((maximum - minimum) / increment).asInstanceOf[Int])
    sliderData.setSliderConstraint(con)
  }
  def name = _name
  def name_=(name:String) { _name = name; repaint() }
  def minimum = sliderData.minimum
  def maximum = sliderData.maximum
  def effectiveMaximum = sliderData.effectiveMaximum
  def increment = sliderData.increment
  def value = sliderData.value
  def value_=(d: Double) {
    sliderData.value = d
    valueComponent.setText(value.toString)
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d: Double, inc: Double) {
    sliderData.value = d
    valueComponent.setText(value.toString)
    slider.setValue(((value - minimum) / inc).asInstanceOf[Int])
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d: Double, buttonRelease: Boolean) {
    sliderData.value_=(d, buttonRelease)
    valueComponent.setText(value.toString)
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def coerceValue(value: Double): Double = {
    val ret = sliderData.coerceValue(value)
    valueComponent.setText(value.toString)
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    ret
  }

  def units = _units
  def units_=(units:String){ _units = units; repaint() }

  def valueSetter(v: Double) = {
    if (sliderData.valueSetter(v)) {
      revalidate()
      repaint()
      true
    }
    else false
  }

  def vertical: Boolean = _vertical
  def vertical_=(vert: Boolean): Unit = {
    if (vert != vertical) {
      _vertical = vert
      resetZoomInfo()
      resetSizeInfo()
      if (vert)
        slider.setOrientation(SwingConstants.VERTICAL)
      else
        slider.setOrientation(SwingConstants.HORIZONTAL)
    }
  }

  def valueString(num: Double): String = {
    var numString = Dump.number(num)
    var place = numString.indexOf('.')
    val p = sliderData.precision
    if (p > 0 && place == -1) {
      numString += "."
      place = numString.length - 1
    }
    if (place != -1 && numString.indexOf('E') == -1) {
      val padding = p - (numString.length - place - 1)
      numString = numString + ("0" * padding)
    }
    if (units=="") numString else numString + " " + units
  }

  override def getMinimumSize = new Dimension(92, 33)
  override def getPreferredSize = new Dimension(150, 33)
}


class SliderWidget(eventOnReleaseOnly: Boolean, random: MersenneTwisterFast) extends MultiErrorWidget with
        AbstractSliderWidget with InterfaceGlobalWidget with Editable with
        org.nlogo.window.Events.PeriodicUpdateEvent.Handler with org.nlogo.window.Events.AfterLoadEvent.Handler {

  type WidgetModel = CoreSlider

  def this(random: MersenneTwisterFast) = this (false, random)

  var minimumCode: String = "0"
  var maximumCode: String = "100"
  var incrementCode: String = "1"
  var defaultValue = 1d
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.slider")
  override def propertySet = Properties.slider

  // VALUE RELATED METHODS
  def valueObject: Object = value.asInstanceOf[AnyRef]

  override def value_=(v: Double): Unit = {
    if (!anyErrors && (v != value || v < minimum || v > effectiveMaximum)) {
      super.value = v
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }
  override def value_=(v: Double, buttonRelease: Boolean) {
    val valueChanged = v != value || v < minimum || v > effectiveMaximum
    if (valueChanged) {
      super.value_=(v, buttonRelease)
      if (!eventOnReleaseOnly) new InterfaceGlobalEvent(this, false, false, true, buttonRelease).raise(this)
    }
    if (eventOnReleaseOnly && buttonRelease) {
      new InterfaceGlobalEvent(this, false, false, valueChanged, buttonRelease).raise(this)
    }
  }

  def valueObject(v: Object) {
    if (v.isInstanceOf[Double]) { value_=(v.asInstanceOf[Double], true) }
  }

  // NAME RELATED METHODS
  private var nameChanged: Boolean = false
  def nameWrapper: String = name
  def nameWrapper(n: String) {
    nameChanged = !n.equals(name) || nameChanged
    setName(n, false)
  }

  override def name_=(n: String){
    setName(n, true)
    repaint()
  }

  private def setName(name: String, sendEvent: Boolean) {
    this._name=name
    displayName(name)
    nameComponent.setText(displayName)
    if (sendEvent) new InterfaceGlobalEvent(this, true, false, false, false).raise(this)
  }

  // EDITING, CONSTRAINTS, AND ERROR HANDLING
  override def editFinished: Boolean = {
    super.editFinished
    removeAllErrors()
    setName(name, nameChanged)
    nameChanged = false
    updateConstraints()
    this.value = StrictMath.min(StrictMath.max(value, minimum), maximum)
    true
  }

  override def setSliderConstraint(con: SliderConstraint): Boolean = {
    if (!anyErrors && super.setSliderConstraint(con)) {
      revalidate()
      repaint()
      true
    } else
      false
  }

  override def updateConstraints() {
    new AddSliderConstraintEvent(this, name, minimumCode, maximumCode, incrementCode, defaultValue).raise(this)
  }

  override def error(key: Object, e: Exception): Unit = {
    super.error(key, e)
    setForeground(java.awt.Color.RED)
  }

  override def removeAllErrors() = {
    super.removeAllErrors()
    setForeground(java.awt.Color.BLACK)
  }

  // EVENT HANDLING
  def handle(e: AfterLoadEvent): Unit = {
    updateConstraints()
    this.value=defaultValue
  }
  def handle(e: PeriodicUpdateEvent) {
    new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
    if(!anyErrors) this.setSliderConstraint(constraint)
  }

  // LOADING AND SAVING

  override def load(model: WidgetModel): AnyRef = {
    val min: String = model.min
    val max: String = model.max
    val v = model.default
    val inc: String = model.step
    units = model.units.optionToPotentiallyEmptyString
    vertical = (model.direction == Vertical)

    this.name = model.display.optionToPotentiallyEmptyString
    minimumCode = min
    maximumCode = max
    // i think this next line is here because of some weird bounds checking
    // it needs to be tested more and maybe we can get rid of it. JC - 9/23/10
    minimumCode = min
    incrementCode = inc
    value_=(v, inc.toDouble)
    defaultValue = v
    setSize(model.right - model.left, model.bottom - model.top)
    this
  }

  override def model: WidgetModel = {
    val savedName = name.potentiallyEmptyStringToOption
    val savedUnits = units.potentiallyEmptyStringToOption
    val dir = if (vertical) Vertical else Horizontal
    val b = getBoundsTuple
    CoreSlider(display = savedName,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      variable = savedName, min = minimumCode, max = maximumCode,
      default = value, step = incrementCode,
      units = savedUnits, direction = dir)
  }
}
