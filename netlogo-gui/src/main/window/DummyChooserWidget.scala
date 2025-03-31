// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.{ I18N, Chooseable, Chooser => CoreChooser, CompilerException, LogoList }
import org.nlogo.editor.Colorizer

class DummyChooserWidget(val compiler: CompilerServices, colorizer: Colorizer) extends Chooser with Editable {
  type WidgetModel = CoreChooser

  private var _name = ""

  def name: String = _name

  def setVarName(newName: String): Unit = {
    _name = newName
    repaint()
  }

  override def updateConstraints(): Unit = {
    // we never update constraints in a dummy widget -- CLB
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.chooser")

  override def editPanel: EditPanel = new DummyChooserEditPanel(this, compiler, colorizer)

  override def editFinished: Boolean = {
    super.editFinished
    setVarName(name)
    true
  }

  def choicesWrapper: String =
    constraint.acceptedValues
      .map(v => Dump.logoObject(v, true, false))
      .mkString("", "\n", "\n")

  def setChoicesWrapper(choicesString: String): Unit = {
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
    oldSize(model.oldSize)
    setSize(model.width, model.height)
    setVarName(model.varName)
    setChoicesWrapper(model.choices.map(c => Dump.logoObject(c.value, true, false)).mkString("\n"))
    index(model.currentChoice)
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    CoreChooser(
      display  = name.potentiallyEmptyStringToOption,
      x        = b.x,     y      = b.y,
      width    = b.width, height = b.height,
      oldSize  = _oldSize,
      variable = name.potentiallyEmptyStringToOption,
      choices  = constraint.acceptedValues.map(Chooseable.apply).toList,
      currentChoice = index)
  }
}
