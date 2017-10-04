// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Dimension, FileDialog, Font, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Action, Box, BoxLayout, JButton, JLabel, JMenuItem, JPanel,
  JPopupMenu }

import org.nlogo.api.Exceptions
import org.nlogo.app.common.{ CommandLine, HistoryPrompt, LinePrompt }
import org.nlogo.awt.{ Fonts, Hierarchy, UserCancelException }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ModalProgressTask, RichAction }
import org.nlogo.swing.Implicits._
import org.nlogo.window.{ CommandCenterInterface, Events => WindowEvents,
  InterfaceColors, OutputArea, TextMenuActions, Zoomable }
import org.nlogo.workspace.{ AbstractWorkspace, ExportOutput }

class CommandCenter(workspace: AbstractWorkspace) extends JPanel
  with Zoomable with CommandCenterInterface
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.ZoomedEvent.Handler {

  // true = echo commands to output
  val commandLine = new CommandLine(this, true, 12, workspace)
  private val prompt = new LinePrompt(commandLine)
  private val northPanel = new JPanel
  private val southPanel = new JPanel
  val output = OutputArea.withNextFocus(commandLine)
  val outputArea = Some(output)
  output.text.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
    override def mouseReleased(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
  })

  private val locationToggleButton =
    new JButton() {
      setText("")
      setFocusable(false)
      setVisible(false)
      // this is very ad hoc. we want to save vertical screen real estate and also keep the
      // button from being too wide on Windows and Linux - ST 7/13/04, 11/24/04
      override def getInsets = new Insets(2, 4, 3, 4)
    }

  locally {
    setOpaque(true)  // so background color shows up - ST 10/4/05
    setBackground(InterfaceColors.COMMAND_CENTER_BACKGROUND)
    setLayout(new BorderLayout)

    //NORTH
    //-----------------------------------------
    val titleLabel = new JLabel(I18N.gui.get("tabs.run.commandcenter"))

    val clearButton = new JButton(RichAction(I18N.gui.get("tabs.run.commandcenter.clearButton")) { _ => output.clear() }) {
      setFocusable(false)
      setFont(new Font(Fonts.platformFont, Font.PLAIN, 9))

      override def getPreferredSize: Dimension = {
        val ps = super.getPreferredSize
        val ms = super.getMinimumSize
        new Dimension(ps.getWidth.toInt, (ms.getHeight * 0.8).toInt)
      }

      override def getInsets = {
        val insets = super.getInsets()
        // this is very ad hoc. we want to save vertical screen real estate - ST 7/13/04
        new Insets(0, insets.left, 2, insets.right)
      }
    }

    add(northPanel, BorderLayout.NORTH)
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS))
    northPanel.setOpaque(false)
    northPanel.add(titleLabel)
    northPanel.add(Box.createGlue)
    Fonts.adjustDefaultFont(titleLabel)
    titleLabel.setFont(titleLabel.getFont.deriveFont(Font.BOLD))
    northPanel.add(locationToggleButton)
    northPanel.add(clearButton)
    resizeNorthPanel()

    //CENTER
    //-----------------------------------------
    add(output, BorderLayout.CENTER)

    //SOUTH
    //-----------------------------------------
    southPanel.setOpaque(false)
    southPanel.setLayout(new BorderLayout)
    southPanel.add(prompt, BorderLayout.WEST)
    southPanel.add(commandLine, BorderLayout.CENTER)
    val historyPanel = new JPanel()
    historyPanel.setOpaque(false)
    historyPanel.setLayout(new BorderLayout)
    historyPanel.add(new HistoryPrompt(commandLine), BorderLayout.CENTER)
    if(System.getProperty("os.name").startsWith("Mac"))
      historyPanel.add(Box.createHorizontalStrut(12), BorderLayout.EAST)
    southPanel.add(historyPanel, BorderLayout.EAST)
    add(southPanel, BorderLayout.SOUTH)
  }

  private[interfacetab] def locationToggleAction_=(a: Action): Unit = {
    locationToggleButton.setAction(a)
    locationToggleButton.setText("")
    locationToggleButton.setVisible(a != null)
  }

  private[interfacetab] def locationToggleAction: Action =
    locationToggleButton.getAction

  override def getMinimumSize =
    new Dimension(0, 2 + northPanel.getMinimumSize.height +
      output.getMinimumSize.height +
      southPanel.getMinimumSize.height)

  def repaintPrompt() { prompt.repaint() }
  override def requestFocus() { getDefaultComponentForFocus().requestFocus() }
  override def requestFocusInWindow(): Boolean = {
    getDefaultComponentForFocus().requestFocusInWindow()
  }
  def getDefaultComponentForFocus(): Component = commandLine.textField

  private def doPopup(e: MouseEvent) {
    new JPopupMenu{
      add(new JMenuItem(TextMenuActions.CopyAction))
      add(new JMenuItem(I18N.gui.get("menu.file.export")){
        addActionListener(() =>
          try {
            val filename = SwingFileDialog.showFiles(
              output, I18N.gui.get("tabs.run.commandcenter.exporting"), FileDialog.SAVE,
              workspace.modelTracker.guessExportName("command center output.txt"))
            ModalProgressTask.onBackgroundThreadWithUIData(
              Hierarchy.getFrame(output), I18N.gui.get("dialog.interface.export.task"),
              () => output.valueText, (text: String) => ExportOutput.silencingErrors(filename, text))
          } catch {
            case uce: UserCancelException => Exceptions.ignore(uce)
          }
        )
      })
    }.show(this, e.getX, e.getY)
  }

  /// event handlers

  def handle(e: WindowEvents.LoadBeginEvent) {
    commandLine.reset()
    repaintPrompt()
    output.clear()
    resizeNorthPanel()
  }

  def resizeNorthPanel(): Unit = {
    val preferredSize = northPanel.getPreferredSize
    northPanel.setMaximumSize(new Dimension(
      preferredSize.getWidth.toInt,
      (northPanel.getMinimumSize.getHeight * zoomFactor).toInt))
  }

  def cycleAgentType(forward: Boolean) {
    import AgentKind.{ Observer => O, Turtle => T, Patch => P, Link => L}
    commandLine.kind match {
      case O => commandLine.agentKind(if (forward) T else L)
      case T => commandLine.agentKind(if (forward) P else O)
      case P => commandLine.agentKind(if (forward) L else T)
      case L => commandLine.agentKind(if (forward) O else P)
    }
    repaintPrompt()
    commandLine.requestFocus()
  }
}
