// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.swing.Implicits._
import org.nlogo.swing.RichAction
import java.awt._
import event.{MouseAdapter, MouseEvent}
import javax.swing._
import org.nlogo.api.I18N

class CommandCenter(workspace: org.nlogo.workspace.AbstractWorkspace,
                    locationToggleAction: Action) extends JPanel
  with org.nlogo.window.Zoomable
  with org.nlogo.window.CommandCenterInterface
  with org.nlogo.window.Events.LoadBeginEvent.Handler
  with org.nlogo.window.Events.ZoomedEvent.Handler {

  // true = echo commands to output
  private val commandLine = new CommandLine(this, true, 12, workspace)
  private val prompt = new LinePrompt(commandLine)
  private val northPanel = new JPanel
  private val southPanel = new JPanel
  val output = new org.nlogo.window.OutputArea(commandLine){
    text.addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
      override def mouseReleased(e: MouseEvent) { if(e.isPopupTrigger) { e.consume(); doPopup(e) }}
    })
  }

  locally {
    setOpaque(true)  // so background color shows up - ST 10/4/05
    setBackground(org.nlogo.window.InterfaceColors.COMMAND_CENTER_BACKGROUND)
    setLayout(new BorderLayout)

    //NORTH
    //-----------------------------------------
    val titleLabel = new JLabel(I18N.gui.get("tabs.run.commandcenter")){
      // tweak spacing on Mac
      putClientProperty("Quaqua.Component.visualMargin", new Insets(0, 0, 0, 0))
    }

    val locationToggleButton =
      if(locationToggleAction == null) null
      else new JButton(locationToggleAction) {
        setText("")
        setFocusable(false)
        // get right appearance on Mac - ST 10/4/05
        putClientProperty("Quaqua.Button.style", "square")
        putClientProperty("Quaqua.Component.visualMargin", new Insets(0, 0, 0, 0))
        // this is very ad hoc. we want to save vertical screen real estate and also keep the
        // button from being too wide on Windows and Linux - ST 7/13/04, 11/24/04
        override def getInsets = new Insets(2, 4, 3, 4)
      }
    val clearButton = new JButton(RichAction(I18N.gui.get("tabs.run.commandcenter.clearButton")) { _ => output.clear() }) {
      setFocusable(false)
      setFont(new Font(org.nlogo.awt.Fonts.platformFont, Font.PLAIN, 9))
      // get right appearance on Mac - ST 10/4/05
      putClientProperty("Quaqua.Button.style", "square")
      putClientProperty("Quaqua.Component.visualMargin", new Insets(0, 0, 0, 0))
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
    org.nlogo.awt.Fonts.adjustDefaultFont(titleLabel)
    titleLabel.setFont(titleLabel.getFont.deriveFont(Font.BOLD))
    if(locationToggleButton != null) northPanel.add(locationToggleButton)
    northPanel.add(clearButton)

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

  override def getMinimumSize =
    new Dimension(0, 2 + northPanel.getMinimumSize.height +
      output.getMinimumSize.height +
      southPanel.getMinimumSize.height)

  def repaintPrompt() { prompt.repaint() }
  override def requestFocus() { getDefaultComponentForFocus().requestFocus() }
  def getDefaultComponentForFocus(): Component = commandLine.textField

  private def doPopup(e: MouseEvent) {
    import org.nlogo.editor.Actions
    new JPopupMenu{
      add(new JMenuItem(Actions.COPY_ACTION))
      Actions.COPY_ACTION.putValue(Action.NAME, I18N.gui.get("menu.edit.copy"))
      add(new JMenuItem(I18N.gui.get("menu.file.export")){
        addActionListener(() =>
          try output.export(
            org.nlogo.swing.FileDialog.show(
              output, I18N.gui.get("tabs.run.commandcenter.exporting"), FileDialog.SAVE,
              workspace.guessExportName("command center output.txt")))
          catch {
            case uce: org.nlogo.awt.UserCancelException => org.nlogo.util.Exceptions.ignore(uce)
          }
        )
      })
    }.show(this, e.getX, e.getY)
  }

  /// event handlers

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) {
    commandLine.reset()
    repaintPrompt()
    output.clear()
  }

  def cycleAgentType(forward: Boolean) {
    import org.nlogo.agent._
    val O = classOf[Observer]; val T = classOf[Turtle]
    val P = classOf[Patch];    val L = classOf[Link]
    commandLine.agentClass match {
      case O => commandLine.agentClass(if (forward) T else L)
      case T => commandLine.agentClass(if (forward) P else O)
      case P => commandLine.agentClass(if (forward) L else T)
      case L => commandLine.agentClass(if (forward) O else P)
    }
    repaintPrompt()
    commandLine.requestFocus()
  }
}
