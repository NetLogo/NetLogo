// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ Chooser => CoreChooser, CompilerException }
import org.nlogo.api.CompilerServices
import org.nlogo.api.Dump
import org.nlogo.api.Editable
import org.nlogo.core.I18N
import org.nlogo.core.LogoList
import org.nlogo.api.Property

import java.util.{ Iterator => JIterator }
import java.util.{ List => JList }

class DummyChooserWidget(compiler: CompilerServices)
    extends Chooser(compiler)
    with Editable {

  type WidgetModel = CoreChooser

  setBorder(widgetBorder)

  override def updateConstraints(): Unit = {
    // we never update constraints in a dummy widget -- CLB
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.chooser")

  def propertySet: JList[Property] =
    Properties.dummyChooser

  override def editFinished: Boolean = {
    super.editFinished
    name(name())
    true
  }

  def choicesWrapper: String =
    constraint.acceptedValues
      .map(v => Dump.logoObject(v, true, false))
      .mkString("", "\n", "\n")

  def choicesWrapper(choicesString: String): Unit = {
    try {
      val oldValue = value
      val newChoices =
        compiler.readFromString("[ " + choicesString + " ]").asInstanceOf[LogoList]

      constraint.acceptedValues(newChoices)

      val newIndex = constraint.indexForValue(oldValue)
      if (newIndex == -1)
        index(0)
      else
        index(newIndex)

    } catch  {
      case e: CompilerException =>
        // this should never happen because LogoListStringEditor
        // checks it for us first when the user types it in
        throw new IllegalStateException(e)
    }
  }


  override def load(model: WidgetModel, helper: Widget.LoadHelper): Object = {
    setSize(model.right - model.left, model.bottom - model.top)
    name(model.varName)
    choicesWrapper(model.choices.map(c => Dump.logoObject(c.value)).mkString("\n"))
    index(model.currentChoice)
    this
  }

  override def save: String = {
    val s = new StringBuilder()

    s.append("CHOOSER\n")
    s.append(getBoundsString)
    // the file format has separate entries for name and display name,
    // but at least at present, they are always equal, so we just
    // write out the name twice - ST 6/3/02
    if ((null != name()) && (!name().trim.equals(""))) {
      s.append(name() + "\n")
      s.append(name() + "\n")
    } else {
      s.append("NIL\n")
      s.append("NIL\n")
    }
    s.append(choicesWrapper.trim.replaceAll("\n", " ") + "\n");
    s.append(index + "\n");
    s.toString
  }
}
