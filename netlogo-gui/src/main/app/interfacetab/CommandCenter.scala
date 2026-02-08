// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Dimension, FileDialog, Font, GridBagConstraints, GridBagLayout,
                  Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import java.util.prefs.Preferences
import javax.swing.{ AbstractAction, Action, Box, JButton, JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.api.Exceptions
import org.nlogo.app.common.{ CommandLine, CommandServer, HistoryPrompt, LinePrompt }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ModalProgressTask, MenuItem, PopupMenu, RichAction,
                         RoundedBorderPanel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ CommandCenterInterface, Events => WindowEvents, OutputArea, TextMenuActions, Zoomable }
import org.nlogo.workspace.{ AbstractWorkspace, ExportOutput }

class CommandCenter(workspace: AbstractWorkspace, showToggle: Boolean) extends JPanel
  with Zoomable with CommandCenterInterface
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.ZoomedEvent.Handler
  with ThemeSync {

  // true = echo commands to output
  val commandLine = new CommandLine(this, true, 12, workspace)
  private val prompt = new LinePrompt(commandLine, true)
  private val northPanel = new JPanel(new GridBagLayout)
  private val southPanel = new JPanel
  val output = OutputArea.withNextFocus(commandLine)
  output.text.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
    override def mouseReleased(e: MouseEvent): Unit = { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
  })

  var commandServer: Option[CommandServer] = None

  if (Preferences.userRoot.node("/org/nlogo/NetLogo").get("enableRemoteCommands", "false").toBoolean) {
    commandServer = Some(new CommandServer(commandLine))
  }

  private val locationToggleButton = new JButton with RoundedBorderPanel with ThemeSync {
    setBorder(new EmptyBorder(3, 5, 3, 6))
    setFocusable(false)
    setDiameter(6)
    enableHover()
    enablePressed()

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())
    }
  }

  private val titleLabel = new JLabel(I18N.gui.get("tabs.run.commandcenter"))

  titleLabel.setFont(titleLabel.getFont.deriveFont(Font.BOLD))

  private val clearButton = new JButton(RichAction(I18N.gui.get("tabs.run.commandcenter.clearButton")) {
    _ => output.clear()
  }) with RoundedBorderPanel with ThemeSync {
    setBorder(new EmptyBorder(3, 12, 3, 12))
    setFocusable(false)
    setDiameter(6)
    enableHover()
    enablePressed()

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())
      setForeground(InterfaceColors.toolbarText())
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

    if (showToggle)
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

      c.insets = new Insets(3, 6, 3, 6)

      southPanel.add(prompt, c)

      c.weightx = 1
      c.fill = GridBagConstraints.HORIZONTAL
      c.insets = new Insets(3, 0, 3, 0)

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

  def repaintPrompt(): Unit = { prompt.repaint() }
  override def requestFocus(): Unit = { getDefaultComponentForFocus().requestFocus() }
  override def requestFocusInWindow(): Boolean = {
    getDefaultComponentForFocus().requestFocusInWindow()
  }
  def getDefaultComponentForFocus(): Component = commandLine.textField

  private def doPopup(e: MouseEvent): Unit = {
    new PopupMenu {
      add(new MenuItem(TextMenuActions.CopyAction))
      add(new MenuItem(new AbstractAction(I18N.gui.get("menu.file.export")) {
        def actionPerformed(e: ActionEvent): Unit = {
          try {
            val filename = SwingFileDialog.showFiles(
              output, I18N.gui.get("tabs.run.commandcenter.exporting"), FileDialog.SAVE,
              workspace.guessExportName("command center output.txt"))
            ModalProgressTask.runForResultOnBackgroundThread(
              Hierarchy.getFrame(output), I18N.gui.get("dialog.interface.export.task"),
              () => output.valueText, (text: String) => ExportOutput.silencingErrors(filename, text))
          } catch {
            case uce: UserCancelException => Exceptions.ignore(uce)
          }
        }
      }))
    }.show(this, e.getX, e.getY)
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.commandCenterBackground())

    titleLabel.setForeground(InterfaceColors.commandCenterText())

    locationToggleButton.syncTheme()
    clearButton.syncTheme()
    output.syncTheme()
    commandLine.syncTheme()
  }

  /// event handlers

  def handle(e: WindowEvents.LoadBeginEvent): Unit = {
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

  def cycleAgentType(forward: Boolean): Unit = {
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
