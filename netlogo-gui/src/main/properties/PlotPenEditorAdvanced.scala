// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, Color, Dimension }

import javax.swing.border.{EtchedBorder, TitledBorder}
import javax.swing._
import javax.swing.BorderFactory._

import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.editor.Colorizer
import org.nlogo.plot.PlotManagerInterface
import org.nlogo.swing.OptionDialog

class PlotPenEditorAdvanced(inputPen: PlotPensEditor.Pen, colorizer: Colorizer, plotManager: PlotManagerInterface) extends JPanel {
  private implicit val i18nPrefix = I18N.Prefix("edit.plot.pen")

  def getPlotPenModeNames = PlotPenModes.toArray

  val PlotPenModes =
    scala.List(I18N.gui("mode.line"),
               I18N.gui("mode.bar"),
               I18N.gui("mode.point"))

  // pieces of the UI
  private val intervalField = new org.nlogo.swing.TextField("", 8)
  private val penModes = new JComboBox(getPlotPenModeNames.asInstanceOf[Array[Object]])
  private val showPenInLegend = new JCheckBox(I18N.gui("showInLegend"), true)
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
          OptionDialog.show(org.nlogo.awt.Hierarchy.getWindow(this),
            "Invalid Entry", "Invalid value for the pen interval", Array(I18N.gui.get("common.buttons.ok")))
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
    setLayout(new BorderLayout())
    val title = createTitledBorder(createEtchedBorder(EtchedBorder.LOWERED), I18N.gui("advanced"))
    title.setTitleJustification(TitledBorder.LEFT)
    setBorder(title)
    val topPanel = new JPanel(){
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new JPanel(new BorderLayout){
        add(new JLabel(I18N.gui("mode")), BorderLayout.WEST)
        add(penModes, BorderLayout.CENTER)
      })
      add(new JPanel(new BorderLayout){
        add(new JLabel(I18N.gui("interval")), BorderLayout.WEST)
        add(intervalField, BorderLayout.CENTER)
      })
      add(new JPanel(new BorderLayout){ add(showPenInLegend, BorderLayout.WEST) })
    }
    val codePanel = new JPanel(){
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new JPanel) // for a little space
      add(setupCode)
      add(updateCode)
    }
    add(topPanel, BorderLayout.NORTH)
    runtimeErrorPanel.foreach(panel => add(panel, BorderLayout.CENTER))
    add(codePanel, BorderLayout.SOUTH)
  }
}
