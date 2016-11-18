// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.core.{ Chooseable, Chooser => CoreChooser }
import org.nlogo.window.Events.{AfterLoadEvent, PeriodicUpdateEvent, InterfaceGlobalEvent}
import org.nlogo.api.{ CompilerServices, Dump, Editable }

class ChooserWidget(val compiler: CompilerServices)
  extends Chooser
  with Editable
  with InterfaceGlobalWidget
  with PeriodicUpdateEvent.Handler {

  type WidgetModel = CoreChooser

  private var _name = ""

  def name: String = _name

  setBorder(widgetBorder)

  override def propertySet = Properties.chooser
  override def classDisplayName: String = I18N.gui.get("tabs.run.widgets.chooser")
  // don't send an event unless the name of the variable
  // defined changes, which is the only case in which we
  // want a recompile. ev 6/15/05
  private var nameChanged = false

  def valueObject: Object = value
  def valueObject(v: Object) {
    if (v != null) {
      var newIndex: Int = constraint.indexForValue(v)
      if (newIndex != -1) {index(newIndex)}
    }
  }

  def name(newName: String): Unit = name(newName, true)

  private def name(newName: String, sendEvent: Boolean): Unit = {
    _name = name
    repaint()
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(newName)
    if (sendEvent) {new InterfaceGlobalEvent(this, true, false, false, false).raise(this)}
  }

  def nameWrapper: String = name
  // name needs a wrapper because we don't want to recompile until editFinished()
  def nameWrapper(newName: String): Unit = {
    nameChanged = (name: String) != newName || nameChanged
    name(newName, false)
  }

  def choicesWrapper = {
    import collection.JavaConverters._
    constraint.acceptedValues.map(v => Dump.logoObject(v, true, false)).mkString("\n")
  }

  def choicesWrapper(choicesString: String) {
    var obj: Object = compiler.readFromString("[ " + choicesString + " ]")
    if (obj.isInstanceOf[LogoList]) { setChoices(obj.asInstanceOf[LogoList]) }
    updateConstraints()
  }

  def choicesWrapper(choices: LogoList) {
    setChoices(choices)
    updateConstraints()
  }

  def setChoices(list: LogoList): Unit = {
    var oldValue: Object = value
    constraint.acceptedValues(list)
    var newIndex: Int = constraint.indexForValue(oldValue)
    if (newIndex == -1) index(0) else index(newIndex)
  }

  def handle(e: AfterLoadEvent) {updateConstraints()}
  def handle(e: PeriodicUpdateEvent) {
    new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def editFinished(): Boolean = {
    super.editFinished
    name(name, nameChanged)
    updateConstraints
    nameChanged = false
    true
  }

  protected[window] override def index(newIndex: Int) {
    // Let's check to see if the value is different than the old value
    // before we raise an InterfaceGlobalEvent.  This will cut
    // down on the number of events generated.
    if ((index: Int) != newIndex) {
      super.index(newIndex)
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }

  private def chooseableListToLogoList(choices: List[Chooseable]): LogoList =
    LogoList(choices.map(_.value): _*)

  override def load(model: WidgetModel): AnyRef = {
    setSize(model.right - model.left, model.bottom - model.top)
    name(model.display.optionToPotentiallyEmptyString)
    choicesWrapper(chooseableListToLogoList(model.choices))
    index(model.currentChoice)
    this
  }

  override def model: WidgetModel = {
    val b = getBoundsTuple
    val savedName = (name: String).potentiallyEmptyStringToOption
    CoreChooser(
      display       = savedName,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      variable      = savedName,
      choices       = constraint.acceptedValues.map(Chooseable.apply).toList,
      currentChoice = index)
  }
}
