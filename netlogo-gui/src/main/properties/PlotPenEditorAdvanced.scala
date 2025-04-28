// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.plot.PlotManagerInterface
import org.nlogo.swing.{ CheckBox, ComboBox, OptionPane, TextField, Transparent }
import org.nlogo.theme.InterfaceColors

class PlotPenEditorAdvanced(inputPen: PlotPensEditor.Pen, colorizer: Colorizer, plotManager: PlotManagerInterface)
  extends JPanel(new GridBagLayout) with Transparent {

  private implicit val i18nPrefix = I18N.Prefix("edit.plot.pen")

  // pieces of the UI
  private val intervalField = new TextField(8)
  private val penModes = new ComboBox(List(I18N.gui("mode.line"), I18N.gui("mode.bar"), I18N.gui("mode.point")))
  private val showPenInLegend = new CheckBox(I18N.gui("showInLegend"))
  val setupCode = CodeEditor(I18N.gui("setupCommands"), colorizer, columns = 65, err=inputPen.setupError)
  val updateCode = CodeEditor(I18N.gui("updateCommands"), colorizer, columns = 65, err=inputPen.updateError)

  val runtimeErrorPanel =
    inputPen.runtimeError.map(
      new RuntimeErrorPanel(_, { panel =>
        inputPen.originalPen.runtimeError = None
        remove(panel)
        revalidate()
        repaint()
      }))

  // layout all the pieces of the ui
  addWidgets()

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
          new OptionPane(Hierarchy.getWindow(this), I18N.gui.get("edit.plot.pen.invalidEntry"),
                         I18N.gui.get("edit.plot.pen.invalidInterval"), OptionPane.Options.Ok, OptionPane.Icons.Error)
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

  private def addWidgets() {
    showPenInLegend.setForeground(InterfaceColors.dialogText())

    penModes.syncTheme()
    setupCode.syncTheme()
    updateCode.syncTheme()

    val c = new GridBagConstraints

    c.gridy = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(new JLabel(I18N.gui("mode")) {
      setForeground(InterfaceColors.dialogText())
    }, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 0, 6, 6)

    add(penModes, c)

    c.gridy = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(0, 6, 6, 6)

    add(new JLabel(I18N.gui("interval")) {
      setForeground(InterfaceColors.dialogText())
    }, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 0, 6, 6)

    add(intervalField, c)

    c.gridy = 2
    c.gridwidth = 2
    c.insets = new Insets(0, 6, 6, 6)

    add(showPenInLegend, c)

    c.gridy = 3

    runtimeErrorPanel.foreach(panel => {
      panel.syncTheme()

      add(panel, c)

      c.gridy = 4
    })

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1

    add(setupCode, c)

    c.gridy += 1

    add(updateCode, c)
  }
}
