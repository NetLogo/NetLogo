// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.util.{ Iterator, List }
import org.nlogo.api.{ CompilerException, ParserServices, Dump, Editable, I18N, LogoList, Property, ModelReader }

class DummyChooserWidget(parser: ParserServices) extends Chooser(parser) with Editable {
  setBorder(widgetBorder)

  override def updateConstraints = {}
  override def classDisplayName() = I18N.gui.get("tabs.run.widgets.chooser")
  def propertySet = Properties.dummyChooser
  override def editFinished() = {
    super.editFinished()
    name = name
    true
  }
  def choicesWrapper =
    constraint.acceptedValues.toVector.map(x => Dump.logoObject(x, true, false)).mkString("\n")

  def choicesWrapper(choicesString: String) = {
    try {
      val oldValue = value
      val newChoices = parser.readFromString("[ " + choicesString + " ]").asInstanceOf[LogoList]
      constraint.acceptedValues(newChoices)
      val newIndex = constraint.indexForValue(oldValue)
      index = if(newIndex == -1) 0 else newIndex
    } catch {
      // this should never happen because LogoListStringEditor
      // checks it for us first when the user types it in
      case e: CompilerException => new IllegalStateException(e)
    }
  }


  /// load and save

  override def load(strings: scala.collection.Seq[String], helper: Widget.LoadHelper) = {
    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt
    setSize(x2-x1, y2-y1)
    name = ModelReader.restoreLines(strings(5))
    choicesWrapper(strings(7))
    index = strings(8).toInt
    this
  }

  // the file format has separate entries for name and display name,
  // but at least at present, they are always equal, so we just
  // write out the name twice - ST 6/3/02
  override def save() = "CHOOSER\n" + 
    s"$getBoundsString${((if(name!=null&&name.trim!="") name else "NIL")+'\n')*2}" +
    s"{choicesWrapper.trim.replaceAll('\n', ' ')}\n$index\n"
}
