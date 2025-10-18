// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.lang.NumberFormatException

import org.nlogo.agent.{ ConstantSliderConstraint, SliderConstraint }
import org.nlogo.api.{ CompilerServices, ExtensionManager, MersenneTwisterFast }
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical, Widget => CoreWidget }
import org.nlogo.editor.Colorizer
import org.nlogo.window.Events.{ InterfaceGlobalEvent, AfterLoadEvent, PeriodicUpdateEvent, AddSliderConstraintEvent }

class SliderWidget(eventOnReleaseOnly: Boolean, random: MersenneTwisterFast,
                   compiler: CompilerServices, colorizer: Colorizer, extensionManager: ExtensionManager)
  extends MultiErrorWidget with AbstractSliderWidget with InterfaceGlobalWidget with Editable
  with PeriodicUpdateEvent.Handler with AfterLoadEvent.Handler {

  def this(random: MersenneTwisterFast, compiler: CompilerServices, colorizer: Colorizer,
           extensionManager: ExtensionManager) =
    this(false, random, compiler, colorizer, extensionManager)

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

  override def editPanel: EditPanel = new SliderEditPanel(this, compiler, colorizer, extensionManager)

  override def getEditable: Option[Editable] = Some(this)

  // VALUE RELATED METHODS
  def valueObject(): Object = super.value.asInstanceOf[AnyRef]

  override def setValue(v: Double): Unit = {
    if (!anyErrors && (v != value || v < minimum || v > effectiveMaximum)) {
      super.setValue(v)
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }

  def setValue(v: Double, buttonRelease: Boolean): Unit = {
    val valueChanged = v != value || v < minimum || v > effectiveMaximum
    if (valueChanged) {
      super.setValue(v)
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

  override def setVarName(n: String): Unit = {
    setName(n, true)
  }

  private def setName(name: String, sendEvent: Boolean): Unit = {
    super.setVarName(name)
    if (sendEvent) new InterfaceGlobalEvent(this, true, false, false, false).raise(this)
  }

  // EDITING, CONSTRAINTS, AND ERROR HANDLING
  override def editFinished(): Boolean = {
    super.editFinished()
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

  override def errorString: Option[String] = {
    // if everything can be parsed as a number, might as well check that the range is valid
    // otherwise, it's probably code, so ignore it and let the compiler figure it out
    // (Isaac B 2/11/25)
    try {
      if (checkRecursive(compiler, minimumCode, name) ||
          checkRecursive(compiler, maximumCode, name) ||
          checkRecursive(compiler, incrementCode, name)) {
        return Some(I18N.gui.get("edit.general.recursive"))
      } else if (minimumCode.toDouble >= maximumCode.toDouble) {
        return Some(I18N.gui.get("edit.slider.invalidBounds"))
      } else if (incrementCode.toDouble > maximumCode.toDouble - minimumCode.toDouble) {
        return Some(I18N.gui.get("edit.slider.invalidIncrement"))
      }
    } catch {
      case e: NumberFormatException =>
    }

    None
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

  override def load(model: CoreWidget): Unit = {
    model match {
      case s: CoreSlider =>
        val min: String = s.min
        val max: String = s.max
        val v = s.default
        val inc: String = s.step

        setSliderConstraint(new ConstantSliderConstraint(min.toDouble, max.toDouble, inc.toDouble))
        setUnits(s.units.optionToPotentiallyEmptyString)
        setVertical(s.direction == Vertical)

        setVarName(s.display.optionToPotentiallyEmptyString)
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
        oldSize(s.oldSize)
        setSize(s.width, s.height)

      case _ =>
    }
  }

  override def model: CoreWidget = {
    val savedName = name.potentiallyEmptyStringToOption
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
