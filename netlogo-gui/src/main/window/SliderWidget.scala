// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.lang.NumberFormatException

import org.nlogo.agent.SliderConstraint
import org.nlogo.api.{ CompilerServices, Editable, MersenneTwisterFast }
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent }

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
      if (checkRecursive(compiler, minimumCode, name)) {
        return Seq((I18N.gui.get("edit.slider.minimum"), I18N.gui.get("edit.general.recursive")))
      } else if (checkRecursive(compiler, maximumCode, name)) {
        return Seq((I18N.gui.get("edit.slider.maximum"), I18N.gui.get("edit.general.recursive")))
      } else if (checkRecursive(compiler, incrementCode, name)) {
        return Seq((I18N.gui.get("edit.slider.increment"), I18N.gui.get("edit.general.recursive")))
      } else if (minimumCode.toDouble >= maximumCode.toDouble) {
        return Seq((I18N.gui.get("edit.slider.maximum"), I18N.gui.get("edit.slider.invalidBounds")))
      } else if (incrementCode.toDouble > maximumCode.toDouble - minimumCode.toDouble) {
        return Seq((I18N.gui.get("edit.slider.increment"), I18N.gui.get("edit.slider.invalidIncrement")))
      }
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
  override def value_=(v: Double, buttonRelease: Boolean): Unit = {
    val valueChanged = v != value || v < minimum || v > effectiveMaximum
    if (valueChanged) {
      super.value_=(v, buttonRelease)
      if (!eventOnReleaseOnly) new InterfaceGlobalEvent(this, false, false, true, buttonRelease).raise(this)
    }
    if (eventOnReleaseOnly && buttonRelease) {
      new InterfaceGlobalEvent(this, false, false, valueChanged, buttonRelease).raise(this)
    }
  }

  def forceValue(v: Double): Unit = {
    super.value = v
    new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
  }

  def valueObject(v: Object): Unit = {
    if (v.isInstanceOf[Double]) { value_=(v.asInstanceOf[Double], true) }
  }

  // NAME RELATED METHODS
  private var nameChanged: Boolean = false
  def nameWrapper: String = name
  def nameWrapper(n: String): Unit = {
    nameChanged = !n.equals(name) || nameChanged
    setName(n, false)
  }

  override def name_=(n: String){
    setName(n, true)
    repaint()
  }

  private def setName(name: String, sendEvent: Boolean): Unit = {
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
    this.value=defaultValue
  }
  def handle(e: PeriodicUpdateEvent): Unit = {
    new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
    if(!anyErrors) this.setSliderConstraint(constraint)
  }

  // LOADING AND SAVING

  override def load(model: WidgetModel): AnyRef = {
    loading = true
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
    setSize(model.width, model.height)
    loading = false
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
