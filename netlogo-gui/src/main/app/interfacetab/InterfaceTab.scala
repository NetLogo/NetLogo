// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ BorderLayout, Color, Component, Container, ContainerOrderFocusTraversalPolicy, Cursor, Dimension,
                  Graphics, Graphics2D, GridBagConstraints, Point }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, MouseAdapter, MouseEvent, MouseMotionAdapter }
import java.awt.print.{ PageFormat, Printable }
import javax.swing.{ AbstractAction, Action, JButton, JComponent, JLayeredPane, JPanel, JScrollPane, JSplitPane,
                     ScrollPaneConstants }

import org.nlogo.app.common.{Events => AppEvents, MenuTab}, AppEvents.SwitchedTabsEvent
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{ Implicits, PrinterManager, Printable => NlogoPrintable, UserAction, Utils },
                       Implicits.thunk2action, UserAction.{ MenuAction, ToolsCategory }
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace, InterfaceColors, ViewUpdatePanel, WidgetInfo,
                          Events => WindowEvents, WorkspaceActions },
                        WindowEvents.{ Enable2DEvent, LoadBeginEvent, OutputEvent }

object InterfaceTab {
  val MenuGroup = "org.nlogo.app.InterfaceTab"
}

import InterfaceTab._

class InterfaceTab(workspace: GUIWorkspace,
                   monitorManager: AgentMonitorManager,
                   dialogFactory: EditDialogFactoryInterface,
                   val commandCenter: CommandCenter) extends JPanel
  with LoadBeginEvent.Handler
  with OutputEvent.Handler
  with Enable2DEvent.Handler
  with SwitchedTabsEvent.Handler
  with NlogoPrintable
  with MenuTab {

  private class SizeButton(expand: Boolean, splitPane: SplitPane) extends JButton {
    setBorder(null)
    setBackground(InterfaceColors.TRANSPARENT)

    if (expand) {
      setAction(new AbstractAction {
        def actionPerformed(e: ActionEvent) {
          if (splitPane.getDividerLocation >= maxDividerLocation) {
            splitPane.resetToPreferredSizes()
          }

          else if (splitPane.getDividerLocation > 0) {
            splitPane.setDividerLocation(0)
          }
        }
      })
    }

    else {
      setAction(new AbstractAction {
        def actionPerformed(e: ActionEvent) {
          if (splitPane.getDividerLocation <= 0) {
            splitPane.resetToPreferredSizes()
          }

          else if (splitPane.getDividerLocation < maxDividerLocation) {
            splitPane.setDividerLocation(maxDividerLocation)
          }
        }
      })
    }

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(Color.BLACK)

      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          if (expand)
            g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                            Array(getHeight / 2 - 2, getHeight / 2 + 2, getHeight / 2 + 2), 3)
          else
            g2d.fillPolygon(Array(getWidth / 2, getWidth / 2 + 5, getWidth / 2 - 5),
                            Array(getHeight / 2 + 2, getHeight / 2 - 2, getHeight / 2 - 2), 3)
        case JSplitPane.VERTICAL_SPLIT =>
          if (expand)
            g2d.fillPolygon(Array(getWidth / 2 - 2, getWidth / 2 + 2, getWidth / 2 + 2),
                            Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
          else
            g2d.fillPolygon(Array(getWidth / 2 + 2, getWidth / 2 - 2, getWidth / 2 - 2),
                            Array(getHeight / 2, getHeight / 2 - 5, getHeight / 2 + 5), 3)
      }
    }
  }

  private class SplitPaneDivider(splitPane: SplitPane) extends JPanel(null) {
    private val expandButton = new SizeButton(true, splitPane)
    private val contractButton = new SizeButton(false, splitPane)

    add(expandButton)
    add(contractButton)
    
    setBackground(InterfaceColors.DARK_GRAY)

    private val dragRadius = 3

    private var offset = new Point(0, 0)

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent) {
        splitPane.getOrientation match {
          case JSplitPane.HORIZONTAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR))
          case JSplitPane.VERTICAL_SPLIT => setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR))
        }
      }

      override def mouseExited(e: MouseEvent) {
        setCursor(Cursor.getDefaultCursor)
      }

      override def mousePressed(e: MouseEvent) {
        offset = e.getPoint
      }
    })

    addMouseMotionListener(new MouseMotionAdapter {
      override def mouseDragged(e: MouseEvent) {
        e.translatePoint(getX, getY)

        splitPane.getOrientation match {
          case JSplitPane.HORIZONTAL_SPLIT => splitPane.setDividerLocation(e.getY - offset.y)
          case JSplitPane.VERTICAL_SPLIT => splitPane.setDividerLocation(e.getX - offset.x)
        }
      }
    })

    override def doLayout() {
      val size = splitPane.getDividerSize

      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          expandButton.setBounds(0, 0, size, size)
          contractButton.setBounds(size, 0, size, size)
        case JSplitPane.VERTICAL_SPLIT =>
          expandButton.setBounds(0, 0, size, size)
          contractButton.setBounds(0, size, size, size)
      }
    }

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(Color.WHITE)
      g2d.fillOval(getWidth / 2 - dragRadius, getHeight / 2 - dragRadius, dragRadius * 2, dragRadius * 2)
    }
  }
  
  private class SplitPane(mainComponent: Component, topComponent: Component) extends JLayeredPane {
    private val divider = new SplitPaneDivider(this)

    add(mainComponent, JLayeredPane.DEFAULT_LAYER)
    add(topComponent, JLayeredPane.PALETTE_LAYER)
    add(divider, JLayeredPane.PALETTE_LAYER)

    private var orientation = JSplitPane.HORIZONTAL_SPLIT
    private var dividerLocation = 0
    private val dividerSize = 18

    def getOrientation: Int = orientation

    def setOrientation(orientation: Int) {
      this.orientation = orientation

      revalidate()
      dividerChanged()
    }

    def getDividerLocation: Int = dividerLocation

    def setDividerLocation(location: Int) {
      dividerLocation = location.max(0).min(maxDividerLocation)

      revalidate()
      dividerChanged()
    }

    private def dividerChanged() {
      commandCenterToggleAction.putValue(Action.NAME,
        if (dividerLocation < maxDividerLocation) I18N.gui.get("menu.tools.hideCommandCenter")
        else I18N.gui.get("menu.tools.showCommandCenter"))
    }

    def getDividerSize: Int = dividerSize

    def resetToPreferredSizes() {
      orientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          setDividerLocation(getHeight - topComponent.getPreferredSize.height - dividerSize)
        case JSplitPane.VERTICAL_SPLIT =>
          setDividerLocation(getWidth - topComponent.getPreferredSize.width - dividerSize)
      }
    }

    override def doLayout() {
      orientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          mainComponent.setBounds(0, 0, getWidth, dividerLocation)
        case JSplitPane.VERTICAL_SPLIT =>
          mainComponent.setBounds(0, 0, dividerLocation, getHeight)
      }

      if (dividerLocation > maxDividerLocation)
        dividerLocation = maxDividerLocation
      
      orientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          topComponent.setBounds(0, dividerLocation + dividerSize, getWidth, getHeight - dividerLocation - dividerSize)
          divider.setBounds(0, dividerLocation, getWidth, dividerSize)
        case JSplitPane.VERTICAL_SPLIT =>
          topComponent.setBounds(dividerLocation + dividerSize, 0, getWidth - dividerLocation - dividerSize, getHeight)
          divider.setBounds(dividerLocation, 0, dividerSize, getHeight)
      }

      dividerChanged()
    }
  }

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(new InterfaceTabFocusTraversalPolicy)
  commandCenter.locationToggleAction = new CommandCenterLocationToggleAction
  val iP = new InterfacePanel(workspace.viewWidget, workspace)

  val commandCenterToggleAction = new CommandCenterToggleAction()

  override val activeMenuActions =
    WorkspaceActions.interfaceActions(workspace) ++
    Seq(iP.undoAction, iP.redoAction, commandCenterToggleAction, new JumpToCommandCenterAction())

  var lastFocusedComponent: JComponent = commandCenter
  setLayout(new BorderLayout)
  private val scrollPane = new JScrollPane(
    iP,
    // always reserve space for the vertical scrollbar, otherwise when it appears it causes a
    // horizontal scrollbar to appear too!  this problem is especially noticeable now that we have
    // the JSplitPane, which means the user is very likely to resize the InterfacePanel in such a
    // way so that only a vertical scrollbar is really needed - ST 7/13/04
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  commandCenter.setMinimumSize(new Dimension(0, 0))

  private var viewUpdatePanel: ViewUpdatePanel = null

  private val splitPane = new SplitPane(scrollPane, commandCenter)

  add(splitPane, BorderLayout.CENTER)

  object TrackingFocusListener extends FocusListener {
    var lastFocused = Option.empty[Component]
    override def focusGained(e: FocusEvent): Unit = {
      lastFocused = Some(e.getSource.asInstanceOf[Component])
    }
    override def focusLost(e: FocusEvent): Unit = { }
  }

  locally {
    import WidgetInfo._
    val buttons = List(button, slider, switch, chooser, input, monitor, plot, output, note)
    add(new InterfaceToolBar(iP, workspace, buttons, workspace.getFrame, dialogFactory) {
      override def addControls() {
        super.addControls()
        viewUpdatePanel = new ViewUpdatePanel(workspace, workspace.viewWidget.displaySwitch, workspace.viewWidget.tickCounter)
        val c = new GridBagConstraints
        c.gridy = 0
        c.gridheight = 2
        add(viewUpdatePanel, c)
      }
    }, BorderLayout.NORTH)
    iP.addFocusListener(TrackingFocusListener)
    commandCenter.getDefaultComponentForFocus.addFocusListener(TrackingFocusListener)
  }

  SwingUtils.addEscKeyAction(this, () => InterfaceTab.this.monitorManager.closeTopMonitor())

  private class InterfaceTabFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
    override def getComponentAfter(focusCycleRoot: Container, aComponent: Component) =
      if(aComponent == iP) commandCenter.getDefaultComponentForFocus
      else super.getComponentAfter(focusCycleRoot, aComponent)
    override def getComponentBefore(focusCycleRoot: Container, aComponent: Component) =
      if(aComponent == iP) commandCenter.getDefaultComponentForFocus
      else super.getComponentBefore(focusCycleRoot, aComponent)
  }

  def getInterfacePanel = iP

  // When we get focus, we want to focus the command center first
  // to prevent keyboard shortcuts (copy, cut, paste) from affecting
  // the code tab or wherever the cursor was before the user switched - RG 2/16/18
  override def requestFocus() {
    commandCenter.requestFocusInWindow()
    TrackingFocusListener.lastFocused.getOrElse(commandCenter).requestFocusInWindow()
  }

  final def handle(e: SwitchedTabsEvent) {
    commandCenter.requestFocusInWindow()
    TrackingFocusListener.lastFocused.getOrElse(commandCenter).requestFocusInWindow()
    if (e.newTab != this) {
      monitorManager.refresh()
    }
  }

  def handle(e: LoadBeginEvent) {
    scrollPane.getHorizontalScrollBar.setValue(0)
    scrollPane.getVerticalScrollBar.setValue(0)
  }

  /// output

  def getOutputArea = Option(iP.getOutputWidget).map(_.outputArea).getOrElse(commandCenter.output)

  def handle(e: OutputEvent) {
    val outputArea = if(e.toCommandCenter) commandCenter.output else getOutputArea
    if(e.clear && iP.getOutputWidget != null) outputArea.clear()
    if(e.outputObject != null) outputArea.append(e.outputObject, e.wrapLines)
  }

  def handle(e: Enable2DEvent) {
    viewUpdatePanel.setVisible(e.enabled)
    viewUpdatePanel.handle(null)
  }

  /// printing

  // satisfy org.nlogo.swing.Printable
  override def print(g: Graphics, pageFormat: PageFormat,
                     pageIndex: Int, printer: PrinterManager) =
    // only allow printing on 1 page since printing graphics over multiple pages would require a lot
    // more changes to the NetLogo source code --mag 10/23/02
    if(pageIndex > 0) Printable.NO_SUCH_PAGE
    else {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.translate(pageFormat.getImageableX, pageFormat.getImageableY)
      iP.printAll(g2d)
      Printable.PAGE_EXISTS
    }

  /// command center stuff

  private class CommandCenterLocationToggleAction extends AbstractAction {
    putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-right.png", 10, 10))

    override def actionPerformed(e: ActionEvent) {
      splitPane.getOrientation match {
        case JSplitPane.VERTICAL_SPLIT =>
          splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
          putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-right.png", 10, 10))
        case JSplitPane.HORIZONTAL_SPLIT =>
          splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT)
          putValue(Action.SMALL_ICON, Utils.iconScaled("/images/shift-bottom.png", 10, 10))
      }

      splitPane.resetToPreferredSizes()
    }
  }

  private def showCommandCenter(): Unit = {
    if (splitPane.getDividerLocation >= maxDividerLocation)
      splitPane.resetToPreferredSizes()
  }

  class CommandCenterToggleAction extends AbstractAction(I18N.gui.get("menu.tools.hideCommandCenter"))
  with MenuAction {
    category    = ToolsCategory
    group       = MenuGroup
    accelerator = UserAction.KeyBindings.keystroke('/', withMenu = true)

    override def actionPerformed(e: ActionEvent) {
      if (splitPane.getDividerLocation < maxDividerLocation) {
        splitPane.setDividerLocation(maxDividerLocation)
        if (iP.isFocusable) iP.requestFocus()
      } else {
        showCommandCenter()
        commandCenter.requestFocus()
      }
    }
  }

  class JumpToCommandCenterAction extends AbstractAction(I18N.gui.get("menu.tools.jumpToCommandCenter"))
  with MenuAction {
    category    = ToolsCategory
    group       = MenuGroup
    accelerator = UserAction.KeyBindings.keystroke('C', withMenu = true, withShift = true)

    override def actionPerformed(e: ActionEvent) {
      if (! commandCenter.getDefaultComponentForFocus.isFocusOwner) {
        showCommandCenter()
        commandCenter.requestFocusInWindow()
      }
    }
  }

  private def maxDividerLocation: Int = {
    splitPane.getOrientation match {
      case JSplitPane.HORIZONTAL_SPLIT => splitPane.getHeight - splitPane.getDividerSize
      case JSplitPane.VERTICAL_SPLIT => splitPane.getWidth - splitPane.getDividerSize
    }
  }

  def packSplitPane() {
    splitPane.setPreferredSize(
      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width, scrollPane.getPreferredSize.height + splitPane.getDividerSize
                        + commandCenter.getPreferredSize.height)
        case JSplitPane.VERTICAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width + splitPane.getDividerSize +
                        commandCenter.getPreferredSize.width, scrollPane.getPreferredSize.height)
      })

    splitPane.revalidate()
    splitPane.resetToPreferredSizes()
  }
}
