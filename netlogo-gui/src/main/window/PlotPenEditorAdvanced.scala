// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.api.CompilerServices
import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.plot.PlotManagerInterface
import org.nlogo.swing.{ BoxRow, CheckBox, ComboBox, HorizontalStrut, OptionPane, SyncZoom, TextField, Transparent,
                         VerticalStrut, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class PlotPenEditorAdvanced(inputPen: PlotPensEditor.Pen, compiler: CompilerServices, colorizer: Colorizer,
                            plotManager: PlotManagerInterface) extends JPanel with Transparent with SyncZoom {

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.plot.pen")

  // pieces of the UI
  private val intervalField = new TextField(8) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  }

  private val penModes = new ComboBox(List(I18N.gui("mode.line"), I18N.gui("mode.bar"), I18N.gui("mode.point")))

  private val showPenInLegend = new CheckBox(I18N.gui("showInLegend")) {
    setForeground(InterfaceColors.dialogText())
  }

  val setupCode = CodeEditor(I18N.gui("setupCommands"), compiler, colorizer, columns = 65,
                             err = () => inputPen.setupError)

  val updateCode = CodeEditor(I18N.gui("updateCommands"), compiler, colorizer, columns = 65,
                              err = () => inputPen.updateError)

  val runtimeErrorPanel =
    inputPen.runtimeError.map(
      new RuntimeErrorPanel(_, { panel =>
        inputPen.originalPen.runtimeError = None
        remove(panel)
        revalidate()
        repaint()
      }))

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(new BoxRow(Seq(
    new JLabel(I18N.gui("mode")) {
      setForeground(InterfaceColors.dialogText())
    },
    new HorizontalStrut(6),
    penModes
  )) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })

  add(new VerticalStrut(6))

  add(new BoxRow(Seq(
    new JLabel(I18N.gui("interval")) {
      setForeground(InterfaceColors.dialogText())
    },
    new HorizontalStrut(6),
    intervalField
  )) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })

  add(new VerticalStrut(6))
  add(new BoxRow(Seq(showPenInLegend, Box.createHorizontalGlue)))
  add(new VerticalStrut(6))

  runtimeErrorPanel.foreach(panel => {
    add(panel)

    panel.syncTheme()
  })

  add(setupCode)
  add(new VerticalStrut(6))
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
}
