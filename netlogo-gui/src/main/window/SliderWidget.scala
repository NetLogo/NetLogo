// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.MersenneTwisterFast
import org.nlogo.core.{ Horizontal, Slider => CoreSlider, Vertical }
import java.awt.event.{ MouseAdapter, MouseEvent }
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent, InputBoxLoseFocusEvent }
import org.nlogo.api.{ Dump, Editable }
import org.nlogo.core.I18N
import org.nlogo.agent.SliderConstraint
import java.awt.{Graphics, Font}

trait AbstractSliderWidget extends MultiErrorWidget {

  protected var _name = ""
  private var _units = ""
  private var _vertical = false
  private val sliderData = new SliderData(this)

  // The painter is used to draw the slider and handle interactions.
  // It comes in two flavors, horizontal and vertical.
  var painter:SliderPainter = new SliderHorizontalPainter(this)

  locally {
    setOpaque(true)
    setLayout(null)
    setBackground(InterfaceColors.SLIDER_BACKGROUND)
    org.nlogo.awt.Fonts.adjustDefaultFont(this)
    doLayout()
    setBorder(widgetBorder)
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        new InputBoxLoseFocusEvent().raise(AbstractSliderWidget.this)
      }
    })
  }

  def constraint = sliderData.constraint
  def setSliderConstraint(con: SliderConstraint) = sliderData.setSliderConstraint(con)
  def name = _name
  def name_=(name:String) { _name = name; repaint() }
  def minimum = sliderData.minimum
  def maximum = sliderData.maximum
  def effectiveMaximum = sliderData.effectiveMaximum
  def increment = sliderData.increment
  def value = sliderData.value
  def value_=(d:Double){
    sliderData.value = d;
    revalidate();
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def value_=(d:Double, buttonRelease:Boolean){
    sliderData.value_=(d, buttonRelease);
    revalidate();
    repaint()
    new Events.WidgetEditedEvent(this).raise(this)
  }
  def coerceValue(value: Double): Double = sliderData.coerceValue(value)

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
  def vertical_=(vert:Boolean): Unit = {
    if(vert != vertical){
      _vertical = vert
      resetZoomInfo()
      resetSizeInfo()
      painter.dettach()
      painter = if (vert) new SliderVerticalPainter(this) else new SliderHorizontalPainter(this)
      validate()
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

  /// size calculations
  override def getMinimumSize = painter.getMinimumSize()
  override def getPreferredSize(font:Font) =painter.getPreferredSize(font)
  override def getMaximumSize = painter.getMaximumSize
  override def doLayout { super.doLayout(); painter.doLayout() }
  override def paintComponent(g:Graphics) = { super.paintComponent(g); painter.paintComponent(g) }
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
    value = v
    defaultValue = v
    setSize(model.width, model.height)
    this
  }

  override def model: WidgetModel = {
    val savedName = name.potentiallyEmptyStringToOption
    val savedUnits = units.potentiallyEmptyStringToOption
    val dir = if (vertical) Vertical else Horizontal
    val b = getUnzoomedBounds
    CoreSlider(display = savedName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      variable = savedName, min = minimumCode, max = maximumCode,
      default = value, step = incrementCode,
      units = savedUnits, direction = dir)
  }
}
