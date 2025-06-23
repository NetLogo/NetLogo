// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.{ I18N, Chooseable, Chooser => CoreChooser, CompilerException, LogoList, Widget => CoreWidget }
import org.nlogo.editor.Colorizer

class DummyChooserWidget(val compiler: CompilerServices, colorizer: Colorizer) extends Chooser with Editable {
  override def updateConstraints(): Unit = {
    // we never update constraints in a dummy widget -- CLB
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.chooser")

  override def editPanel: EditPanel = new DummyChooserEditPanel(this, compiler, colorizer)

  override def getEditable: Option[Editable] = Some(this)

  override def editFinished(): Boolean = {
    super.editFinished()
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

      populate()

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


  override def load(model: CoreWidget): Unit = {
    model match {
      case chooser: CoreChooser =>
        oldSize(chooser.oldSize)
        setSize(chooser.width, chooser.height)
        setVarName(chooser.varName)
        setChoicesWrapper(chooser.choices.map(c => Dump.logoObject(c.value, true, false)).mkString("\n"))
        index(chooser.currentChoice)

      case _ =>
    }
  }

  override def model: CoreWidget = {
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
