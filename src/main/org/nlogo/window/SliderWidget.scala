// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.util.MersenneTwisterFast
import java.awt.event.{ MouseAdapter, MouseEvent }
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent, InputBoxLoseFocusEvent }
import org.nlogo.api.{ Dump, Editable, I18N, LogoException, ModelReader }
import org.nlogo.agent.SliderConstraint.SliderConstraintException
import org.nlogo.agent.SliderConstraint
import java.awt.{Graphics, Font}

trait AbstractSliderWidget extends MultiErrorWidget {

  protected var _name = ""
  private var _units = ""
  private var _vertical = false
  private val sliderData = new SliderData

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
  }
  def value_=(d:Double, buttonRelease:Boolean){
    sliderData.value_=(d, buttonRelease);
    revalidate();
    repaint()
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

  override def setToolTipText(text:String){
    super.setToolTipText(text)
    painter.setToolTipText(text)
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
  def this(random: MersenneTwisterFast) = this (false, random)

  //Changed for DeltaTick (April 3, 2013)
  var minimumCode: String = "0"
  var maximumCode: String = "1.0"
  var incrementCode: String = "0.1"
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
    this.value = StrictMath.min(StrictMath.max(value, minimum), maximum)
    nameChanged = false
    updateConstraints()
    true
  }

  override def setSliderConstraint(con: SliderConstraint): Boolean = {
    if (!anyErrors) {
      try if (super.setSliderConstraint(con)) {
        revalidate()
        repaint()
        return true
      }
      catch {
        case ex: SliderConstraint.ConstraintRuntimeException =>
          setConstraintError(ex.spec.fieldName, ex)
          org.nlogo.util.Exceptions.handle(ex)
        case ex: LogoException => org.nlogo.util.Exceptions.handle(ex)
        false
      }
    }
    false
  }

  override def updateConstraints() {
    new AddSliderConstraintEvent(this, name, minimumCode, maximumCode, incrementCode, defaultValue).raise(this)
  }
  
  def setConstraintError(constraintField: String, ex: SliderConstraintException) {
    super.error(constraintField, ex)
    setForeground(java.awt.Color.RED)
  }

  override def removeAllErrors() = { super.removeAllErrors(); setForeground(java.awt.Color.BLACK) }

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
  def load(strings: Array[String], helper: Widget.LoadHelper): Object = {
    val min: String = ModelReader.restoreLines(strings(7))
    val max: String = ModelReader.restoreLines(strings(8))
    val v = strings(9).toDouble
    val inc: String = ModelReader.restoreLines(strings(10))
    if (strings.length > 12) {
      units = strings(12)
      if (units == "NIL") { units = "" }
    }
    if (strings.length > 13 && strings(13) == "VERTICAL") vertical = true
    this.name = ModelReader.restoreLines(strings(6))
    minimumCode=min
    maximumCode=max
    // i think this next line is here because of some weird bounds checking
    // it needs to be tested more and maybe we can get rid of it. JC - 9/23/10
    minimumCode=min
    incrementCode=inc
    value=v
    defaultValue = v
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    this
  }

  def save: String = {
    val s: StringBuilder = new StringBuilder
    s.append("SLIDER\n")
    s.append(getBoundsString)
    if ((null != name) && (name.trim != "")) { s.append(name + "\n"); s.append(name + "\n") }
    else { s.append("NIL\n"); s.append("NIL\n") }
    s.append(ModelReader.stripLines(minimumCode) + "\n")
    s.append(ModelReader.stripLines(maximumCode) + "\n")
    s.append(Dump.number(value) + "\n")
    s.append(ModelReader.stripLines(incrementCode) + "\n")
    s.append("1\n")
    if ((null != units) && (units.trim!="")) s.append(units + "\n")
    else { s.append("NIL\n") }
    if (vertical) s.append("VERTICAL\n") else s.append("HORIZONTAL\n")
    s.toString
  }
}
