// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, Dump, Editable, Property }
import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.core.{ Chooseable, Chooser => CoreChooser, CompilerException }

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


  override def load(model: WidgetModel): AnyRef = {
    setSize(model.right - model.left, model.bottom - model.top)
    name(model.varName)
    choicesWrapper(model.choices.map(c => Dump.logoObject(c.value)).mkString("\n"))
    index(model.currentChoice)
    this
  }

  override def model: WidgetModel = {
    val bounds = getBounds()
    val savedName = if (name().trim != null && name().trim != "") Some(name()) else None
    CoreChooser(display = savedName,
      left     = bounds.x,                top    = bounds.y,
      right    = bounds.x + bounds.width, bottom = bounds.y + bounds.height,
      variable = savedName,
      choices  = constraint.acceptedValues.map(Chooseable.apply).toList,
      currentChoice = index)
  }
}
