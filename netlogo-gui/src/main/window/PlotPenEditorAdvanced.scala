// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.api.CompilerServices
import org.nlogo.awt.Hierarchy
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.plot.PlotManagerInterface
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CheckBox, ComboBox, MaximumHeight, OptionPane, TextField,
                         ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class PlotPenEditorAdvanced(inputPen: PlotPensEditor.Pen, compiler: CompilerServices, colorizer: Colorizer,
                            plotManager: PlotManagerInterface) extends BoxColumn(6) {

  // pieces of the UI
  private val intervalField = new TextField(8) with MaximumHeight

  private val penModes = new ComboBox(List(loc"Line", loc"Bar", loc"Point"))

  private val showPenInLegend = new CheckBox(loc"Show in legend") {
    setForeground(InterfaceColors.dialogText())
  }

  val setupCode = CodeEditor(loc"Pen setup commands", compiler, colorizer, columns = 65,
                             err = () => inputPen.setupError)

  val updateCode = CodeEditor(loc"Pen update commands", compiler, colorizer, columns = 65,
                              err = () => inputPen.updateError)

  val runtimeErrorPanel =
    inputPen.runtimeError.map(
      new RuntimeErrorPanel(_, { panel =>
        inputPen.originalPen.runtimeError = None
        remove(panel)
        revalidate()
        repaint()
      }))

  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(new BoxRow(Seq(
    new JLabel(loc"Mode") {
      setForeground(InterfaceColors.dialogText())
    },
    penModes
  ), 6) with MaximumHeight)

  add(new BoxRow(Seq(
    new JLabel(loc"Interval") {
      setForeground(InterfaceColors.dialogText())
    },
    intervalField
  ), 6) with MaximumHeight)

  add(new BoxRow(showPenInLegend, BoxAlign.Start))

  runtimeErrorPanel.foreach(panel => {
    add(panel)

    panel.syncTheme()
  })

  add(setupCode)
  add(updateCode)

  penModes.syncTheme()
  setupCode.syncTheme()
  updateCode.syncTheme()

  /**
   * set the values of all the inputs to the values of the input pen
   */
  intervalField.setText(inputPen.interval.toString)
  penModes.setSelectedIndex(inputPen.mode)
  showPenInLegend.setSelected(inputPen.inLegend)
  setupCode.set(inputPen.setupCode)
  updateCode.set(inputPen.updateCode)

  /**
   * creates the result by getting values out of each of the inputs
   */
  def getResult: Option[PlotPensEditor.Pen] = {
    val validInterval = {
      try {intervalField.getText.toDouble; true}
      catch {
        case ex: NumberFormatException =>
          new OptionPane(Hierarchy.getWindow(this), loc"Invalid Entry", loc"Invalid value for the pen interval",
                         OptionPane.Options.Ok, OptionPane.Icons.error)
          false
      }
    }

    if (validInterval)
      Some(inputPen.copy(
        interval = intervalField.getText.toDouble,
        mode = penModes.getSelectedIndex,
        inLegend = showPenInLegend.isSelected,
        setupCode = setupCode.get.getOrElse(""),
        updateCode = updateCode.get.getOrElse("")))
      else None
  }
}
