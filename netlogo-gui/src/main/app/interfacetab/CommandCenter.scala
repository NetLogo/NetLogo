// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Dimension, FileDialog, Font, Graphics, GridBagConstraints, GridBagLayout,
                  Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Action, Box, JButton, JLabel, JMenuItem, JPanel, JPopupMenu }
import javax.swing.border.EmptyBorder

import org.nlogo.api.Exceptions
import org.nlogo.app.common.{ CommandLine, HistoryPrompt, LinePrompt }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ModalProgressTask, RichAction, Utils }
import org.nlogo.window.{ CommandCenterInterface, Events => WindowEvents,
  InterfaceColors, OutputArea, TextMenuActions, ThemeSync, Zoomable }
import org.nlogo.workspace.{ AbstractWorkspace, ExportOutput }

class CommandCenter(workspace: AbstractWorkspace) extends JPanel
  with Zoomable with CommandCenterInterface
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.ZoomedEvent.Handler
  with ThemeSync {

  // true = echo commands to output
  val commandLine = new CommandLine(this, true, 12, workspace)
  private val prompt = new LinePrompt(commandLine)
  private val northPanel = new JPanel(new GridBagLayout)
  private val southPanel = new JPanel
  val output = OutputArea.withNextFocus(commandLine)
  output.text.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
    override def mouseReleased(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
  })

  private val locationToggleButton = new JButton {
    setFocusable(false)
  }

  private val titleLabel = new JLabel(I18N.gui.get("tabs.run.commandcenter"))

  titleLabel.setFont(titleLabel.getFont.deriveFont(Font.BOLD))

  private val clearButton = new JButton(RichAction(I18N.gui.get("tabs.run.commandcenter.clearButton")) {
    _ => output.clear()
  }) {
    setOpaque(false)
    setBackground(InterfaceColors.TRANSPARENT)
    setBorder(new EmptyBorder(3, 12, 3, 12))
    setFocusable(false)

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
      g2d.setColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)

      setForeground(InterfaceColors.TOOLBAR_TEXT)

      super.paintComponent(g)
    }
  }

  locally {
    setOpaque(true)  // so background color shows up - ST 10/4/05
    setLayout(new BorderLayout)

    //NORTH
    //-----------------------------------------

    add(northPanel, BorderLayout.NORTH)

    northPanel.setOpaque(false)

    val c = new GridBagConstraints

    c.anchor = GridBagConstraints.WEST
    c.weightx = 1
    c.fill = GridBagConstraints.VERTICAL
    c.insets = new Insets(6, 6, 6, 6)

    northPanel.add(titleLabel, c)

    c.anchor = GridBagConstraints.EAST
    c.weightx = 0
    c.insets = new Insets(6, 0, 6, 6)

    northPanel.add(locationToggleButton, c)
    northPanel.add(clearButton, c)

    resizeNorthPanel()

    //CENTER
    //-----------------------------------------
    add(output, BorderLayout.CENTER)

    //SOUTH
    //-----------------------------------------
    southPanel.setOpaque(false)
    southPanel.setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(0, 6, 0, 6)

      southPanel.add(prompt, c)

      c.weightx = 1
      c.fill = GridBagConstraints.HORIZONTAL
      c.insets = new Insets(0, 0, 0, 0)

      southPanel.add(commandLine, c)

      val historyPanel = new JPanel

      historyPanel.setOpaque(false)
      historyPanel.setLayout(new BorderLayout)

      historyPanel.add(new HistoryPrompt(commandLine), BorderLayout.CENTER)

      if (System.getProperty("os.name").startsWith("Mac"))
        historyPanel.add(Box.createHorizontalStrut(12), BorderLayout.EAST)
      
      c.weightx = 0
      c.fill = GridBagConstraints.VERTICAL

      southPanel.add(historyPanel, c)
    }
    
    add(southPanel, BorderLayout.SOUTH)
  }

  private[interfacetab] def locationToggleAction_=(a: Action) =
    locationToggleButton.setAction(a)

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
        addActionListener { _ =>
          try {
            val filename = SwingFileDialog.showFiles(
              output, I18N.gui.get("tabs.run.commandcenter.exporting"), FileDialog.SAVE,
              workspace.guessExportName("command center output.txt"))
            ModalProgressTask.onBackgroundThreadWithUIData(
              Hierarchy.getFrame(output), I18N.gui.get("dialog.interface.export.task"),
              () => output.valueText, (text: String) => ExportOutput.silencingErrors(filename, text))
          } catch {
            case uce: UserCancelException => Exceptions.ignore(uce)
          }
        }
      })
    }.show(this, e.getX, e.getY)
  }

  def syncTheme() {
    setBackground(InterfaceColors.COMMAND_CENTER_BACKGROUND)

    titleLabel.setForeground(InterfaceColors.COMMAND_CENTER_TEXT)

    locationToggleButton.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)

    commandLine.syncTheme()
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
