// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.lang.NumberFormatException

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.{ CompilerServices, MersenneTwisterFast }
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }
import org.nlogo.editor.Colorizer
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent }

class SliderWidget(eventOnReleaseOnly: Boolean, random: MersenneTwisterFast,
                   compiler: CompilerServices, colorizer: Colorizer)
  extends MultiErrorWidget with AbstractSliderWidget with InterfaceGlobalWidget with Editable
  with PeriodicUpdateEvent.Handler with AfterLoadEvent.Handler {

  type WidgetModel = CoreSlider

  def this(random: MersenneTwisterFast, compiler: CompilerServices, colorizer: Colorizer) =
    this(false, random, compiler, colorizer)

  private var _minimumCode: String = "0"
  private var _maximumCode: String = "100"
  private var _incrementCode: String = "1"

  private var defaultValue = 1d

  def minimumCode: String = _minimumCode
  def setMinimumCode(s: String): Unit = {
    _minimumCode = s
  }

  def maximumCode: String = _maximumCode
  def setMaximumCode(s: String): Unit = {
    _maximumCode = s
  }

  def incrementCode: String = _incrementCode
  def setIncrementCode(s: String): Unit = {
    _incrementCode = s
  }

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.slider")

  override def editPanel: EditPanel = new SliderEditPanel(this, compiler, colorizer)

  // VALUE RELATED METHODS
  def valueObject: Object = super.value.asInstanceOf[AnyRef]

  override def setValue(v: Double): Unit = {
    if (!anyErrors && (v != value || v < minimum || v > effectiveMaximum)) {
      super.setValue(v)
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }
  override def setValue(v: Double, buttonRelease: Boolean): Unit = {
    val valueChanged = v != value || v < minimum || v > effectiveMaximum
    if (valueChanged) {
      super.setValue(v, buttonRelease)
      if (!eventOnReleaseOnly) new InterfaceGlobalEvent(this, false, false, true, buttonRelease).raise(this)
    }
    if (eventOnReleaseOnly && buttonRelease)
      new InterfaceGlobalEvent(this, false, false, valueChanged, buttonRelease).raise(this)
  }

  def forceValue(v: Double): Unit = {
    super.setValue(v)
    new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
  }

  def valueObject(v: Object): Unit = {
    if (v.isInstanceOf[Double]) { setValue(v.asInstanceOf[Double], true) }
  }

  // NAME RELATED METHODS
  private var nameChanged: Boolean = false

  def setNameWrapper(n: String): Unit = {
    nameChanged = !n.equals(name) || nameChanged
    setName(n, false)
  }

  override def setName2(n: String): Unit = {
    setName(n, true)
    repaint()
  }

  private def setName(name: String, sendEvent: Boolean): Unit = {
    this._name = name
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
    } else {
      false
    }
  }

  override def updateConstraints(): Unit = {
    new AddSliderConstraintEvent(this, name, minimumCode, maximumCode, incrementCode, defaultValue).raise(this)
  }

  // EVENT HANDLING
  def handle(e: AfterLoadEvent): Unit = {
    updateConstraints()
    setValue(defaultValue)
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
    if (!anyErrors) this.setSliderConstraint(constraint)
  }

  // LOADING AND SAVING

  override def load(model: WidgetModel): AnyRef = {
    val min: String = model.min
    val max: String = model.max
    val v = model.default
    val inc: String = model.step
    setUnits(model.units.optionToPotentiallyEmptyString)
    setVertical(model.direction == Vertical)

    setName2(model.display.optionToPotentiallyEmptyString)
    setMinimumCode(min)
    setMaximumCode(max)
    // i think this next line is here because of some weird bounds checking
    // it needs to be tested more and maybe we can get rid of it. JC - 9/23/10
    setMinimumCode(min)
    setIncrementCode(inc)
    try {
      setValue(v, inc.toDouble)
    } catch {
      case e: NumberFormatException =>
        setValue(v)
    }
    defaultValue = v
    oldSize(model.oldSize)
    setSize(model.width, model.height)
    this
  }

  override def model: WidgetModel = {
    val savedName = super.name.potentiallyEmptyStringToOption
    val savedUnits = units.potentiallyEmptyStringToOption
    val dir = if (vertical) Vertical else Horizontal
    val b = getUnzoomedBounds
    CoreSlider(display = savedName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable = savedName, min = minimumCode, max = maximumCode,
      default = value, step = incrementCode,
      units = savedUnits, direction = dir)
  }
}
