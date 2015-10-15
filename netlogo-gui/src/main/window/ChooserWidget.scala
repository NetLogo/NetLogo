// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.window.Events.{AfterLoadEvent, PeriodicUpdateEvent, InterfaceGlobalEvent}
import org.nlogo.api.{I18N, CompilerServices, Dump, Editable}
import org.nlogo.core.LogoList

class ChooserWidget(compiler: CompilerServices) extends Chooser(compiler) with Editable with
        InterfaceGlobalWidget with PeriodicUpdateEvent.Handler {
  setBorder(widgetBorder)

  override def propertySet = Properties.chooser
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.chooser")
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

  override def name(newName: String) = name(newName, true)
  private def name(newName: String, sendEvent: Boolean) {
    super.name(newName)
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(newName)
    if (sendEvent) {new InterfaceGlobalEvent(this, true, false, false, false).raise(this)}
  }
  def nameWrapper = name()
  // name needs a wrapper because we don't want to recompile until editFinished()
  def nameWrapper(newName: String) {
    nameChanged = !name().equals(newName) || nameChanged
    name(newName, false)
  }

  def choicesWrapper = {
    import collection.JavaConverters._
    constraint.acceptedValues.map(v => Dump.logoObject(v, true, false)).mkString("\n")
  }

  def choicesWrapper(choicesString: String) {
    var obj: Object = compiler.readFromString("[ " + choicesString + " ]")
    if (obj.isInstanceOf[LogoList]) {setChoices(obj.asInstanceOf[LogoList])}
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
    name(name(), nameChanged)
    updateConstraints
    nameChanged = false
    true
  }

  protected[window] override def index(index: Int) {
    // Let's check to see if the value is different than the old value
    // before we raise an InterfaceGlobalEvent.  This will cut
    // down on the number of events generated.
    if (this.index() != index) {
      super.index(index)
      new InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }

  def load(strings: Array[String], helper: Widget.LoadHelper): Object = {
    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt
    setSize(x2 - x1, y2 - y1)
    name(org.nlogo.api.ModelReader.restoreLines(strings(5)))
    choicesWrapper(strings(7))
    index(Integer.parseInt(strings(8)))
    this
  }

  def save: String = {
    var s: StringBuilder = new StringBuilder
    s.append("CHOOSER\n")
    s.append(getBoundsString)
    // the file format has separate entries for name and display name,
    // but at least at present, they are always equal, so we just
    // write out the name twice - ST 6/3/02
    if ((null != name()) && (!name().trim.equals(""))) {
      s.append(name() + "\n")
      s.append(name() + "\n")
    }
    else {
      s.append("NIL\n")
      s.append("NIL\n")
    }
    s.append(choicesWrapper.trim.replaceAll("\n", " ") + "\n")
    s.append(index() + "\n")
    s.toString
  }
}
