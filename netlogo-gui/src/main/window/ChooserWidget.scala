// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.{ I18N, Chooseable, Chooser => CoreChooser, LogoList, Widget => CoreWidget }
import org.nlogo.editor.Colorizer
import org.nlogo.window.Events.{AfterLoadEvent, PeriodicUpdateEvent, InterfaceGlobalEvent}

class ChooserWidget(val compiler: CompilerServices, colorizer: Colorizer)
  extends Chooser with Editable with InterfaceGlobalWidget with PeriodicUpdateEvent.Handler {

  private var _name = ""

  def name: String = _name

  override def classDisplayName: String = I18N.gui.get("tabs.run.widgets.chooser")

  override def editPanel: EditPanel = new ChooserEditPanel(this, compiler, colorizer)

  // don't send an event unless the name of the variable
  // defined changes, which is the only case in which we
  // want a recompile. ev 6/15/05
  private var nameChanged = false

  def valueObject(): Object = value
  def valueObject(v: Object): Unit = {
    if (v != null) {
      val newIndex: Int = constraint.indexForValue(v)
      if (newIndex != -1) {index(newIndex)}
    }
  }

  def name(newName: String): Unit = name(newName, true)

  private def name(newName: String, sendEvent: Boolean): Unit = {
    _name = newName
    label.setText(_name)
    repaint()
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(newName)
    if (sendEvent) {new InterfaceGlobalEvent(this, true, false, false, false).raise(this)}
  }

  // name needs a wrapper because we don't want to recompile until editFinished()
  def setNameWrapper(newName: String): Unit = {
    nameChanged = _name != newName || nameChanged
    name(newName, false)
  }

  def choicesWrapper =
    constraint.acceptedValues.map(v => Dump.logoObject(v, true, false)).mkString("\n")

  def setChoicesWrapper(choicesString: String): Unit = {
    compiler.readFromString(s"[ $choicesString ]") match {
      case list: LogoList => setChoices(list)
      case _ =>
    }
    updateConstraints()
  }

  def setChoicesWrapper(choices: LogoList): Unit = {
    setChoices(choices)
    updateConstraints()
  }

  def setChoices(list: LogoList): Unit = {
    val oldValue: Object = value
    constraint.acceptedValues(list)
    populate()
    val newIndex: Int = constraint.indexForValue(oldValue)
    if (newIndex == -1) index(0) else index(newIndex)
  }

  def handle(e: AfterLoadEvent): Unit = {updateConstraints()}
  def handle(e: PeriodicUpdateEvent): Unit = {
    new InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def editFinished(): Boolean = {
    super.editFinished()
    name(name, nameChanged)
    updateConstraints()
    nameChanged = false
    true
  }

  protected[window] override def index(newIndex: Int): Unit = {
    // Let's check to see if the value is different than the old value
    // before we raise an InterfaceGlobalEvent.  This will cut
    // down on the number of events generated.
    if ((index: Int) != newIndex) {
      super.index(newIndex)
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
      new Events.WidgetEditedEvent(this).raise(this)
    }
  }

  private def chooseableListToLogoList(choices: List[Chooseable]): LogoList =
    LogoList(choices.map(_.value): _*)

  override def load(model: CoreWidget): Unit = {
    model match {
      case chooser: CoreChooser =>
        oldSize(chooser.oldSize)
        setSize(chooser.width, chooser.height)
        name(chooser.display.optionToPotentiallyEmptyString)
        setChoicesWrapper(chooseableListToLogoList(chooser.choices))
        index(chooser.currentChoice)

      case _ =>
    }
  }

  override def model: CoreWidget = {
    val b = getUnzoomedBounds
    val savedName = (name: String).potentiallyEmptyStringToOption
    CoreChooser(
      display       = savedName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize       = _oldSize,
      variable      = savedName,
      choices       = constraint.acceptedValues.map(Chooseable.apply).toList,
      currentChoice = index)
  }
}
