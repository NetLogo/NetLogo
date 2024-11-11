// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints, GridBagLayout }
import javax.swing._
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.plot.PlotManagerInterface
import org.nlogo.swing.{ CheckBox, OptionDialog, TextField }
import org.nlogo.theme.InterfaceColors

class PlotPenEditorAdvanced(inputPen: PlotPensEditor.Pen, colorizer: Colorizer, plotManager: PlotManagerInterface) extends JPanel {
  private implicit val i18nPrefix = I18N.Prefix("edit.plot.pen")

  def getPlotPenModeNames = PlotPenModes.toArray

  val PlotPenModes =
    scala.List(I18N.gui("mode.line"),
               I18N.gui("mode.bar"),
               I18N.gui("mode.point"))

  // pieces of the UI
  private val intervalField = new TextField("", 8)
  private val penModes = new JComboBox(getPlotPenModeNames.asInstanceOf[Array[Object]])
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
          OptionDialog.showMessage(org.nlogo.awt.Hierarchy.getWindow(this),
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
    setBorder(new EmptyBorder(6, 6, 6, 6))
    setOpaque(false)
    setBackground(InterfaceColors.TRANSPARENT)

    intervalField.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    intervalField.setForeground(InterfaceColors.TOOLBAR_TEXT)
    intervalField.setCaretColor(InterfaceColors.TOOLBAR_TEXT)

    penModes.setForeground(InterfaceColors.DIALOG_TEXT)
    showPenInLegend.setForeground(InterfaceColors.DIALOG_TEXT)

    setupCode.syncTheme()
    updateCode.syncTheme()

    val topPanel = new JPanel {
      setOpaque(false)
      setBackground(InterfaceColors.TRANSPARENT)

      val modePanel = new JPanel(new BorderLayout) {
        setOpaque(false)
        setBackground(InterfaceColors.TRANSPARENT)

        add(new JLabel(I18N.gui("mode")) {
          setForeground(InterfaceColors.DIALOG_TEXT)
        }, BorderLayout.WEST)
        add(penModes, BorderLayout.CENTER)
      }
      val intervalPanel = new JPanel(new BorderLayout) {
        setOpaque(false)
        setBackground(InterfaceColors.TRANSPARENT)

        add(new JLabel(I18N.gui("interval")) {
          setForeground(InterfaceColors.DIALOG_TEXT)
        }, BorderLayout.WEST)
        add(intervalField, BorderLayout.CENTER)
      }
      val showPanel = new JPanel(new BorderLayout) {
        setOpaque(false)
        setBackground(InterfaceColors.TRANSPARENT)
        
        add(showPenInLegend, BorderLayout.WEST)
      }
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      Seq(modePanel, intervalPanel, showPanel).foreach(add)
    }
    val codePanel = new JPanel {
      setOpaque(false)
      setBackground(InterfaceColors.TRANSPARENT)
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new JPanel {
        setOpaque(false)
        setBackground(InterfaceColors.TRANSPARENT)
      }) // for a little space
      add(setupCode)
      add(updateCode)
    }
    setLayout(new GridBagLayout)
    val c = new GridBagConstraints()
    c.anchor = GridBagConstraints.NORTH
    c.fill = GridBagConstraints.HORIZONTAL
    c.gridheight = 3
    c.gridx = 0
    c.weightx = 1.0
    c.weighty = 0
    add(topPanel, c)
    runtimeErrorPanel.foreach { panel =>
      c.weighty = 0
      add(panel, c)
    }
    c.weighty = 1.0
    c.fill = GridBagConstraints.BOTH
    add(codePanel, c)
  }
}
