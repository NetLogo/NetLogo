// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, Point, RadialGradientPaint }
import java.awt.event.{ ActionEvent, ActionListener, FocusAdapter, FocusEvent, MouseAdapter, MouseEvent,
                        MouseMotionAdapter, MouseWheelEvent, MouseWheelListener }
import java.lang.NumberFormatException
import javax.swing.{ BorderFactory, JLabel, JSlider, JTextField, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.basic.BasicSliderUI
import javax.swing.text.{ AttributeSet, PlainDocument }

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.{ CompilerServices, Dump, Editable, MersenneTwisterFast }
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }
import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent,
                                 InputBoxLoseFocusEvent }

import scala.math.Pi

trait AbstractSliderWidget extends MultiErrorWidget with ThemeSync {
  private class SliderUI(slider: JSlider) extends BasicSliderUI(slider) {
    private var hover = false
    private var pressed = false

    slider.setOpaque(false)

    slider.addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        if (thumbRect.contains(e.getPoint)) {
          hover = false
          pressed = true

          repaint()
        }
      }

      override def mouseReleased(e: MouseEvent) {
        hover = thumbRect.contains(e.getPoint)
        pressed = false

        repaint()
      }

      override def mouseExited(e: MouseEvent) {
        hover = false
        pressed = false

        repaint()
      }
    })

    slider.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent) {
        hover = thumbRect.contains(e.getPoint) && !pressed

        repaint()
      }
    })

    override def paintTrack(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      val thickness = (6 * zoomFactor).toInt
      if (vertical) {
        val startX = trackRect.x + trackRect.width / 2 - thickness / 2
        g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND_FILLED)
        g2d.fillRoundRect(startX, thumbRect.y, thickness, trackRect.height - thumbRect.y, thickness, thickness)
        g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND)
        g2d.fillRoundRect(startX, trackRect.y, thickness, thumbRect.y, thickness, thickness)
      }
      else {
        val startY = trackRect.y + trackRect.height / 2 - thickness / 2
        g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND_FILLED)
        g2d.fillRoundRect(trackRect.x, startY, thumbRect.x, thickness, thickness, thickness)
        g2d.setColor(InterfaceColors.SLIDER_BAR_BACKGROUND)
        g2d.fillRoundRect(thumbRect.x, startY, trackRect.width - thumbRect.x, thickness, thickness, thickness)
      }
    }

    override def paintThumb(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      if (hover) {
        if (vertical) {
          g2d.setPaint(new RadialGradientPaint(thumbRect.getCenterX.toInt, thumbRect.getCenterY.toInt + 3,
                                              getThumbSize.height / 2f, Array[Float](0, 1),
                                              Array(InterfaceColors.WIDGET_HOVER_SHADOW, InterfaceColors.TRANSPARENT)))
          g2d.fillOval(thumbRect.x, thumbRect.y + getThumbSize.width / 2 - getThumbSize.height / 2 + 3,
                      getThumbSize.height, getThumbSize.height)
        }
        else {
          g2d.setPaint(new RadialGradientPaint(thumbRect.getCenterX.toInt, thumbRect.getCenterY.toInt + 3,
                                              getThumbSize.width / 2f, Array[Float](0, 1),
                                              Array(InterfaceColors.WIDGET_HOVER_SHADOW, InterfaceColors.TRANSPARENT)))
          g2d.fillOval(thumbRect.x, thumbRect.y + getThumbSize.height / 2 - getThumbSize.width / 2 + 3,
                      getThumbSize.width, getThumbSize.width)
        }
      }
      if (vertical) {
        val height = (getThumbSize.height * zoomFactor).toInt
        val startX = thumbRect.getCenterX.toInt - height / 2
        if (pressed)
          g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND_PRESSED)
        else
          g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND)
        g2d.fillOval(startX, thumbRect.y + thumbRect.height / 2 - height / 2, height, height)
        g2d.setColor(InterfaceColors.SLIDER_THUMB_BORDER)
        g2d.drawOval(startX, thumbRect.y + thumbRect.height / 2 - height / 2, height - 1, height - 1)
      }
      else {
        val width = (getThumbSize.width * zoomFactor).toInt
        val startY = thumbRect.getCenterY.toInt - width / 2
        if (pressed)
          g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND_PRESSED)
        else
          g2d.setColor(InterfaceColors.SLIDER_THUMB_BACKGROUND)
        g2d.fillOval(thumbRect.x + thumbRect.width / 2 - width / 2, startY, width, width)
        g2d.setColor(InterfaceColors.SLIDER_THUMB_BORDER)
        g2d.drawOval(thumbRect.x + thumbRect.width / 2 - width / 2, startY, width - 1, width - 1)
      }
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
          else if (e.getButton == MouseEvent.BUTTON1) {
            slider.requestFocus()

            if (vertical)
              slider.setValue(valueForYPosition(e.getPoint.y))
            else
              slider.setValue(valueForXPosition(e.getPoint.x))
          }
        }
      }
  }

  protected class Label(text: String) extends JLabel(text) {
    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      if (vertical) {
        g2d.setClip(null) // this does not feel right but it's the only thing that works for now (IB 8/11/24)
        g2d.rotate(-Pi / 2)
      }
      super.paintComponent(g)
    }
  }

  protected class TextField extends JTextField("50", 3) with Transparent {
    setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0))
    setFont(getFont.deriveFont(11f))
    setHorizontalAlignment(SwingConstants.RIGHT)

    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        try {
          value = getText.toDouble
        }

        catch {
          case e: NumberFormatException =>
        }

        setText(valueString(value))

        getParent.requestFocus()
      }
    })

    addFocusListener(new FocusAdapter {
      override def focusLost(e: FocusEvent) {
        fireActionPerformed()
      }
    })

    override def getPreferredSize: Dimension =
      new Dimension(25.max(getFontMetrics(getFont).stringWidth(getText) + 6), super.getPreferredSize.height)

    protected override def createDefaultModel =
      new PlainDocument {
        override def insertString(offset: Int, str: String, attributes: AttributeSet) {
          super.insertString(offset, str, attributes)

          AbstractSliderWidget.this.revalidate()
        }
      }

    override def contains(x: Int, y: Int): Boolean = {
      if (vertical)
        -y > 0 && x > 0 && -y < getWidth && x < getHeight
      else
        super.contains(x, y)
    }
    
    override def contains(point: Point): Boolean = {
      if (vertical)
        -point.y > 0 && point.x > 0 && -point.y < getWidth && point.x < getHeight
      else
        super.contains(point)
    }

    override def processMouseEvent(e: MouseEvent) {
      e.translatePoint(-e.getPoint.y - e.getPoint.x, e.getPoint.x - e.getPoint.y)
      super.processMouseEvent(e)
    }

    override def processMouseMotionEvent(e: MouseEvent) {
      e.translatePoint(-e.getPoint.y - e.getPoint.x, e.getPoint.x - e.getPoint.y)
      super.processMouseMotionEvent(e)
    }

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      if (vertical) {
        g2d.setClip(null) // this does not feel right but it's the only thing that works for now (IB 8/11/24)
        g2d.rotate(-Pi / 2)
      }
      g2d.setColor(InterfaceColors.INPUT_BORDER)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      g2d.setColor(InterfaceColors.DISPLAY_AREA_BACKGROUND)
      g2d.fillRoundRect(1, 1, getWidth - 2, getHeight - 2, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      super.paintComponent(g)
    }
  }

  protected var _name = ""
  private var _units = ""
  private var _vertical = false
  private val sliderData = new SliderData(this)

  val nameComponent = new Label(I18N.gui.get("edit.slider.previewName"))
  val valueComponent = new TextField
  val unitsComponent = new Label("")
  var slider = new JSlider(0, ((maximum - minimum) / increment).toInt, 50) {
    addMouseWheelListener(new MouseWheelListener {
      def mouseWheelMoved(e: MouseWheelEvent) {
        value = minimum.max(value - increment * e.getWheelRotation).min(effectiveMaximum)
      }
    })
  }

  slider.setUI(new SliderUI(slider))

  setLayout(null)

  add(nameComponent)
  add(valueComponent)
  add(unitsComponent)
  add(slider)

  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent): Unit = {
      value = minimum + slider.getValue * increment
    }
  })

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      requestFocus()

      new InputBoxLoseFocusEvent().raise(AbstractSliderWidget.this)
    }
  })

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
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d: Double, inc: Double) {
    sliderData.value = d
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / inc).asInstanceOf[Int])
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d: Double, buttonRelease: Boolean) {
    sliderData.value_=(d, buttonRelease)
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def coerceValue(value: Double): Double = {
    val ret = sliderData.coerceValue(value)
    valueComponent.setText(valueString(value))
    slider.setValue(((value - minimum) / increment).asInstanceOf[Int])
    repaint()
    ret
  }

  def units = _units
  def units_=(units: String) {
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
    if (p > 0) {
      if (place == -1) {
        numString += "."
        place = numString.length - 1
      }
      else {
        numString = numString.substring(0, numString.size.min(place + p + 1))
      }
    }
    if (place != -1 && numString.indexOf('E') == -1) {
      val padding = p - (numString.length - place - 1)
      numString = numString + ("0" * padding)
    }
    numString
  }

  override def doLayout() {
    if (preserveWidgetSizes) {
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
      }

      else {
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
    }

    else {
      if (vertical) {
        nameComponent.setBounds(6, getHeight - 12, nameComponent.getPreferredSize.width.min(
                                                     getWidth - unitsComponent.getPreferredSize.width -
                                                     valueComponent.getPreferredSize.width - 30),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(6, unitsComponent.getPreferredSize.width + 12, unitsComponent.getPreferredSize.width,
                                 unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(6, unitsComponent.getPreferredSize.width + valueComponent.getPreferredSize.width + 18,
                                 valueComponent.getPreferredSize.width, valueComponent.getPreferredSize.height)
        slider.setBounds(getWidth - slider.getPreferredSize.width - 6, 6, slider.getPreferredSize.width, getHeight - 6)
      }

      else {
        nameComponent.setBounds(12, 6, nameComponent.getPreferredSize.width.min(
                                        getWidth - unitsComponent.getPreferredSize.width -
                                        valueComponent.getPreferredSize.width - 36),
                                nameComponent.getPreferredSize.height)
        unitsComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width - 12, 6,
                                 unitsComponent.getPreferredSize.width, unitsComponent.getPreferredSize.height)
        valueComponent.setBounds(getWidth - unitsComponent.getPreferredSize.width -
                                   valueComponent.getPreferredSize.width - 18, 6,
                                 valueComponent.getPreferredSize.width, valueComponent.getPreferredSize.height)
        slider.setBounds(6, getHeight - slider.getPreferredSize.height - 6, getWidth - 6, slider.getPreferredSize.height)
      }
    }

    if (nameComponent.getPreferredSize.width > nameComponent.getWidth)
      nameComponent.setToolTipText(nameComponent.getText)
    else
      nameComponent.setToolTipText(null)
  }

  override def getMinimumSize = {
    if (preserveWidgetSizes) {
      if (vertical)
        new Dimension(33, 92)
      else
        new Dimension(92, 33)
    }

    else {
      if (vertical)
        new Dimension(53, 150)
      else
        new Dimension(150, 53)
    }
  }

  override def getMaximumSize = {
    if (preserveWidgetSizes) {
      if (vertical)
        new Dimension(33, 10000)
      else
        new Dimension(10000, 33)
    }

    else {
      if (vertical)
        new Dimension(53, 10000)
      else
        new Dimension(10000, 53)
    }
  }

  override def getPreferredSize = {
    if (preserveWidgetSizes) {
      if (vertical)
        new Dimension(33, 150)
      else
        new Dimension(150, 33)
    }

    else {
      if (vertical)
        new Dimension(53, 250)
      else
        new Dimension(250, 53)
    }
  }

  def syncTheme() {
    setBackgroundColor(InterfaceColors.SLIDER_BACKGROUND)

    valueComponent.setForeground(InterfaceColors.DISPLAY_AREA_TEXT)
    unitsComponent.setForeground(InterfaceColors.WIDGET_TEXT)

    valueComponent.setCaretColor(InterfaceColors.DISPLAY_AREA_TEXT)
  }

  override def paintComponent(g: Graphics) {
    if (anyErrors)
      nameComponent.setForeground(InterfaceColors.WIDGET_TEXT_ERROR)
    else
      nameComponent.setForeground(InterfaceColors.WIDGET_TEXT)

    super.paintComponent(g)
  }
}


class SliderWidget(eventOnReleaseOnly: Boolean, random: MersenneTwisterFast,
                   compiler: CompilerServices)
  extends MultiErrorWidget with AbstractSliderWidget with InterfaceGlobalWidget with Editable
  with PeriodicUpdateEvent.Handler with AfterLoadEvent.Handler {

  type WidgetModel = CoreSlider

  def this(random: MersenneTwisterFast, compiler: CompilerServices) = this(false, random, compiler)

  var minimumCode: String = "0"
  var maximumCode: String = "100"
  var incrementCode: String = "1"
  var defaultValue = 1d

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.slider")
  override def propertySet = Properties.slider

  override def invalidSettings: Seq[(String, String)] = {
    try {
      if (checkRecursive(compiler, minimumCode, name))
        return Seq((I18N.gui.get("edit.slider.minimum"), I18N.gui.get("edit.general.recursive")))
      else if (checkRecursive(compiler, maximumCode, name))
        return Seq((I18N.gui.get("edit.slider.maximum"), I18N.gui.get("edit.general.recursive")))
      else if (checkRecursive(compiler, incrementCode, name))
        return Seq((I18N.gui.get("edit.slider.increment"), I18N.gui.get("edit.general.recursive")))
      else if (minimumCode.toDouble >= maximumCode.toDouble)
        return Seq((I18N.gui.get("edit.slider.maximum"), I18N.gui.get("edit.slider.invalidBounds")))
      else if (incrementCode.toDouble > maximumCode.toDouble - minimumCode.toDouble)
        return Seq((I18N.gui.get("edit.slider.increment"), I18N.gui.get("edit.slider.invalidIncrement")))
    }

    catch {
      case e: NumberFormatException =>
    }
    
    Nil
  }

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

  def forceValue(v: Double) {
    super.value = v
    new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
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
    forceValue(StrictMath.min(StrictMath.max(value, minimum), maximum))
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
