// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Component, Dimension, FileDialog, Font }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, Action, Box, JButton, JLabel, JPanel }

import org.nlogo.api.Exceptions
import org.nlogo.app.common.{ CommandLine, CommandServer, HistoryPrompt, LinePrompt }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, FileDialog => SwingFileDialog, ModalProgressTask,
                         MenuItem, PopupMenu, PreferredSize, RichAction, RoundedBorderPanel, Utils, Zoomable,
                         ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ CommandCenterInterface, Events => WindowEvents, OutputArea, TextMenuActions }
import org.nlogo.workspace.{ AbstractWorkspace, ExportOutput }

class CommandCenter(workspace: AbstractWorkspace, showToggle: Boolean, packSplitPane: () => Unit = () => {})
  extends JPanel
  with CommandCenterInterface
  with WindowEvents.LoadBeginEvent.Handler
  with ThemeSync {

  // true = echo commands to output
  val commandLine = new CommandLine(this, true, 12, workspace)
  val commandServer = new CommandServer(commandLine)

  private val prompt = new LinePrompt(commandLine, true)

  val output = OutputArea.withNextFocus(commandLine)
  output.text.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
    override def mouseReleased(e: MouseEvent): Unit = { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
  })

  private val locationToggleButton = new Button(null) with PreferredSize {
    setBorder(new ZoomableBorder(3, 5, 3, 6))
    setVisible(showToggle)

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, clearButton.getPreferredSize.height)

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())
    }
  }

  private val titleLabel = new JLabel(I18N.gui.get("tabs.run.commandcenter")) with Zoomable {
    setBaseFont(getFont.deriveFont(Font.BOLD))
  }

  private val clearButton = new JButton(RichAction(I18N.gui.get("tabs.run.commandcenter.clearButton")) {
    _ => output.clear()
  }) with RoundedBorderPanel with Zoomable with ThemeSync {
    setBorder(new ZoomableBorder(3, 12, 3, 12))
    setFocusable(false)
    enableHover()
    enablePressed()

    override def zoomComponent(): Unit = {
      setDiameter(Utils.zoom(6))
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())
      setForeground(InterfaceColors.toolbarText())
    }
  }

  private val historyPrompt = new HistoryPrompt(commandLine)

  private val northPanel = new BoxRow(Seq(
    titleLabel,
    Box.createHorizontalGlue,
    locationToggleButton,
    clearButton
  ), 6) {
    setBorder(new ZoomableBorder(6, 0, 6, 0))
  }

  private val southPanel = new BoxRow(Seq(
    prompt,
    commandLine,
    new BoxColumn(historyPrompt, BoxAlign.End)
  ), 6) {
    setBorder(new ZoomableBorder(3, 0, 3, 0))
  }

  setLayout(new BorderLayout)
  setBorder(new ZoomableBorder(0, 6, 0, 6))

  add(northPanel, BorderLayout.NORTH)
  add(output, BorderLayout.CENTER)
  add(southPanel, BorderLayout.SOUTH)

  private[interfacetab] def locationToggleAction_=(a: Action) =
    locationToggleButton.setAction(a)

  private[interfacetab] def locationToggleAction: Action =
    locationToggleButton.getAction

  override def getMinimumSize =
    new Dimension(0, 2 + northPanel.getMinimumSize.height +
      output.getMinimumSize.height +
      southPanel.getMinimumSize.height)

  override def repaintPrompt(): Unit = {
    prompt.repaint()
  }

  override def fitPrompt(): Unit = {
    revalidate()
    repaint()

    packSplitPane()
  }

  override def requestFocus(): Unit = { getDefaultComponentForFocus().requestFocus() }
  override def requestFocusInWindow(): Boolean = {
    getDefaultComponentForFocus().requestFocusInWindow()
  }
  def getDefaultComponentForFocus(): Component = commandLine.textField

  def setCodeFont(font: Font): Unit = {
    output.setBaseFont(font)
    commandLine.setBaseFont(font)
  }

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
    historyPrompt.syncTheme()
  }

  /// event handlers

  def handle(e: WindowEvents.LoadBeginEvent): Unit = {
    commandLine.reset()
    repaintPrompt()
    output.clear()
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
