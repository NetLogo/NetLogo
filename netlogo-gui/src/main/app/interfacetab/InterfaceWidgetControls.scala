// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Dimension, Frame }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, Action, Box, BoxLayout, ButtonGroup, JLabel, JPanel }

import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ AutomationUtils, BoxColumn, DropdownArrow, HorizontalStrut, MenuItem, MouseUtils, PopupMenu,
                         PreferredSize, RoundedBorderPanel, ToolBarToggleButton, Transparent, Utils, Zoomable,
                         ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Editable, EditDialog, EditDialogFactory, Events => WindowEvents, GUIWorkspace, InterfaceMode,
                          JobWidget, Widget, WidgetInfo, WorldViewSettings }

import scala.collection.mutable.HashSet

class InterfaceWidgetControls(wPanel: WidgetPanel,
                              workspace: GUIWorkspace,
                              widgetInfos: Seq[WidgetInfo],
                              frame: Frame,
                              dialogFactory: EditDialogFactory)
  extends JPanel
  with Transparent
  with AppEvents.WidgetSelectedEvent.Handler
  with WindowEvents.InterfaceModeChangedEvent.Handler
  with WindowEvents.WidgetForegroundedEvent.Handler
  with WindowEvents.WidgetRemovedEvent.Handler
  with WindowEvents.EditView3DEvent.Handler
  with WindowEvents.WidgetAddedEvent.Handler
  with Zoomable
  with ThemeSync {

  private val selectedObjects = new HashSet[Widget]

  val interactButton = new SquareButton(new InteractAction)
  val selectButton = new SquareButton(new SelectAction)
  val editButton = new SquareButton(new EditAction)
  val deleteButton = new SquareButton(new DeleteAction)

  private val buttonGroup = new ButtonGroup

  private val widgetMenu = new WidgetMenu
  private val alignmentMenu = new AlignmentMenu

  locally {
    val altText = if (System.getProperty("os.name").toLowerCase.contains("mac")) {
      "\u2325"
    } else {
      "Alt"
    }

    interactButton.setToolTipText(I18N.gui.getN("tabs.run.interactButton.tooltip", altText))
    selectButton.setToolTipText(I18N.gui.getN("tabs.run.selectButton.tooltip", altText))
    editButton.setToolTipText(I18N.gui.getN("tabs.run.editButton.tooltip", altText))
    deleteButton.setToolTipText(I18N.gui.getN("tabs.run.deleteButton.tooltip", altText))
  }

  buttonGroup.add(interactButton)
  buttonGroup.add(selectButton)
  buttonGroup.add(editButton)
  buttonGroup.add(deleteButton)

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(new HorizontalStrut(6))
  add(widgetMenu)
  add(new HorizontalStrut(6))
  add(alignmentMenu)
  add(new HorizontalStrut(6))
  add(interactButton)
  add(new HorizontalStrut(6))
  add(selectButton)
  add(new HorizontalStrut(6))
  add(editButton)
  add(new HorizontalStrut(6))
  add(deleteButton)

  interactButton.setSelected(true)

  class InteractAction extends AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      wPanel.setInterfaceMode(InterfaceMode.Interact, true)
    }
  }

  class SelectAction extends AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      wPanel.setInterfaceMode(InterfaceMode.Select, true)
    }
  }

  class EditAction extends AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      if (editButton.isSelected) {
        editWidget(null)
        wPanel.setInterfaceMode(InterfaceMode.Edit, true)
      } else {
        editButton.doClick()
      }
    }
  }

  class DeleteAction extends AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      if (deleteButton.isSelected) {
        wPanel.deleteSelectedWidgets()
        wPanel.setInterfaceMode(InterfaceMode.Delete, true)
      }

      else
        deleteButton.doClick()
    }
  }

  private var editTarget: Option[Editable] = None

  private [interfacetab] def editWidget(widget: Editable): Unit = {
    // this is to support the "Edit..." button in the view control strip - ST 7/18/03
    val targetOption = Option(widget).orElse {
      if (!editButton.isEnabled) None
      editTarget
    }.filter(wPanel.contains)
    for (target <- targetOption) {
      def suppress(b: Boolean): Unit = {
        target match {
          case w: JobWidget => w.suppressRecompiles(b)
          case _ =>
        }
      }
      wPanel.haltIfRunning()
      suppress(true)
      wPanel.editWidgetFinished(target, dialogFactory.canceled(frame, target))
      suppress(false)
    }
  }

  def handle(e: WindowEvents.EditView3DEvent): Unit = {
    e.settings match {
      case wvs: WorldViewSettings =>
        wPanel.haltIfRunning()
        wPanel.editWidgetFinished(wvs, new EditDialog(frame, wvs, wvs.editPanel3D, true).canceled)

      case _ =>
    }
  }

  private def setIcons(): Unit = {
    val size: Int = Utils.zoom(18)

    interactButton.setIcon(Utils.iconScaledWithColor("/images/interact.png", size, size,
                           if (interactButton.isSelected) {
                             InterfaceColors.toolbarImageSelected()
                           } else {
                             InterfaceColors.toolbarImage()
                           }))

    selectButton.setIcon(Utils.iconScaledWithColor("/images/select.png", size, size,
                         if (selectButton.isSelected) {
                           InterfaceColors.toolbarImageSelected()
                         } else {
                           InterfaceColors.toolbarImage()
                         }))

    editButton.setIcon(Utils.iconScaledWithColor("/images/edit.png", size, size,
                       if (editButton.isSelected) {
                         InterfaceColors.toolbarImageSelected()
                       } else {
                         InterfaceColors.toolbarImage()
                       }))

    deleteButton.setIcon(Utils.iconScaledWithColor("/images/delete.png", size, size,
                         if (!deleteButton.isEnabled) {
                           InterfaceColors.toolbarImageDisabled()
                         } else if (deleteButton.isSelected) {
                           InterfaceColors.toolbarImageSelected()
                         } else {
                           InterfaceColors.toolbarImage()
                         }))
  }

  override def zoom(oldZoom: Float): Unit = {
    setIcons()
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    widgetMenu.syncTheme()
    alignmentMenu.syncTheme()

    setIcons()
  }

  def handle(e: WindowEvents.WidgetRemovedEvent): Unit = {
    val r = e.widget
    if(selectedObjects.contains(r)) {
      if(r.isInstanceOf[Editable] && editTarget.exists(_ == r.asInstanceOf[Editable]))
        editTarget = None
      selectedObjects.remove(r)
    }
  }

  private def updateActions(widget: Widget): Unit = {
    if (wPanel.getWrapper(widget).selected) {
      selectedObjects += widget
    } else {
      selectedObjects -= widget
    }

    updateTargets(widget)
  }

  private def updateTargets(widget: Widget): Unit = {
    if (selectedObjects.size == 1) {
      editTarget = widget.getEditable
    } else {
      editTarget = None
    }

    deleteButton.setEnabled(selectedObjects.forall(_.deleteable))

    syncTheme()
  }

  def handle(e: WindowEvents.WidgetAddedEvent): Unit = {
    updateActions(e.widget.asInstanceOf[Widget])
  }

  final def handle(e: AppEvents.WidgetSelectedEvent): Unit = {
    updateActions(e.widget)
  }

  def handle(e: WindowEvents.WidgetForegroundedEvent): Unit = {
    updateTargets(e.widget)
  }

  def handle(e: WindowEvents.InterfaceModeChangedEvent): Unit = {
    if (e.source == wPanel) {
      e.mode match {
        case InterfaceMode.Interact =>
          interactButton.setSelected(true)

        case InterfaceMode.Select =>
          selectButton.setSelected(true)

        case InterfaceMode.Edit =>
          editButton.setSelected(true)

        case InterfaceMode.Delete =>
          deleteButton.setSelected(true)

        case InterfaceMode.Add =>
          buttonGroup.clearSelection()

      }

      syncTheme()
    }
  }

  private [app] def openWidgetMenu(): Option[PopupMenu] = {
    AutomationUtils.sendClick(widgetMenu, widgetMenu.getWidth / 2, widgetMenu.getHeight / 2)

    if (AutomationUtils.waitUntil(widgetMenu.popup.isVisible)) {
      Option(widgetMenu.popup)
    } else {
      None
    }
  }

  private [app] def openAlignmentMenu(): Option[PopupMenu] = {
    AutomationUtils.sendClick(alignmentMenu, alignmentMenu.getWidth / 2, alignmentMenu.getHeight / 2)

    if (AutomationUtils.waitUntil(alignmentMenu.popup.isVisible)) {
      Option(alignmentMenu.popup)
    } else {
      None
    }
  }

  class WidgetMenu extends JPanel with RoundedBorderPanel with Zoomable with ThemeSync with MouseUtils {
    private val label = new JLabel(I18N.gui.get("tabs.run.addWidget"))
    private val arrow = new DropdownArrow

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
    setBorder(new ZoomableBorder(6, 8, 6, 6))
    setDiameter(Utils.zoom(6))

    add(label)
    add(new HorizontalStrut(14))
    add(new BoxColumn(Seq(Box.createVerticalGlue, arrow, Box.createVerticalGlue)))

    enableHover()

    private val actions: Seq[WidgetMenuItem] = widgetInfos.map(new WidgetMenuItem(_))

    private var chosenItem = ""

    var popup: PopupMenu = getPopup

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        popup = getPopup

        popup.show(WidgetMenu.this, 0, getHeight)
      }
    })

    def getSelectedWidget =
      widgetInfos.find(_.displayName == chosenItem).get.coreWidget

    private def getPopup: PopupMenu = {
      new PopupMenu {
        add(actions(0))
        addSeparator()
        add(actions(1))
        add(actions(2))
        add(actions(3))
        add(actions(4))
        addSeparator()
        add(actions(5))
        add(actions(6))
        add(actions(7))
        addSeparator()
        add(actions(8))
      }
    }

    override def zoom(oldZoom: Float): Unit = {
      setDiameter(Utils.zoom(6))

      actions.foreach(Utils.zoomComponents(_, oldZoom))
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())

      label.setForeground(InterfaceColors.toolbarText())
    }

    private def createWidget(info: WidgetInfo): Unit = {
      chosenItem = info.displayName

      wPanel.createShadowWidget(widgetMenu.getSelectedWidget)
    }

    private class WidgetMenuItem(info: WidgetInfo) extends MenuItem(info.displayName, () => createWidget(info)) {
      override def addNotify(): Unit = {
        super.addNotify()

        setEnabled(wPanel.canAddWidget(info.displayName))
      }

      override def zoom(oldZoom: Float): Unit = {
        setIcon(info.icon)
        setIconTextGap(Utils.zoom(4))
      }
    }
  }

  class AlignmentMenu extends JPanel with RoundedBorderPanel with Zoomable with ThemeSync with MouseUtils {
    private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tabs.run.widget")

    private val label = new JLabel(I18N.gui.get("tabs.run.alignWidgets"))
    private val arrow = new DropdownArrow

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
    setBorder(new ZoomableBorder(6, 8, 6, 6))
    setDiameter(Utils.zoom(6))

    add(label)
    add(new HorizontalStrut(14))
    add(new BoxColumn(Seq(Box.createVerticalGlue, arrow, Box.createVerticalGlue)))

    enableHover()

    private val leftAction = new MenuItem(I18N.gui("alignLeft"), () => wPanel.alignLeft())
    private val centerHorizontalAction = new MenuItem(I18N.gui("alignCenterHorizontal"),
                                                      () => wPanel.alignCenterHorizontal())
    private val rightAction = new MenuItem(I18N.gui("alignRight"), () => wPanel.alignRight())
    private val topAction = new MenuItem(I18N.gui("alignTop"), () => wPanel.alignTop())
    private val centerVerticalAction = new MenuItem(I18N.gui("alignCenterVertical"), () => wPanel.alignCenterVertical())
    private val bottomAction = new MenuItem(I18N.gui("alignBottom"), () => wPanel.alignBottom())
    private val distributeHorizontalAction = new MenuItem(I18N.gui("distributeHorizontal"),
                                                          () => wPanel.distributeHorizontal())
    private val distributeVerticalAction = new MenuItem(I18N.gui("distributeVertical"),
                                                        () => wPanel.distributeVertical())
    private val stretchLeftAction = new MenuItem(I18N.gui("stretchLeft"), () => wPanel.stretchLeft())
    private val stretchRightAction = new MenuItem(I18N.gui("stretchRight"), () => wPanel.stretchRight())
    private val stretchTopAction = new MenuItem(I18N.gui("stretchTop"), () => wPanel.stretchTop())
    private val stretchBottomAction = new MenuItem(I18N.gui("stretchBottom"), () => wPanel.stretchBottom())

    var popup: PopupMenu = getPopup

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        leftAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignLeft)
        centerHorizontalAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignCenterHorizontal)
        rightAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignRight)
        topAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignTop)
        centerVerticalAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignCenterVertical)
        bottomAction.setEnabled(selectedObjects.size > 1 && wPanel.canAlignBottom)
        distributeHorizontalAction.setEnabled(selectedObjects.size > 1)
        distributeVerticalAction.setEnabled(selectedObjects.size > 1)
        stretchLeftAction.setEnabled(selectedObjects.size > 1)
        stretchRightAction.setEnabled(selectedObjects.size > 1)
        stretchTopAction.setEnabled(selectedObjects.size > 1)
        stretchBottomAction.setEnabled(selectedObjects.size > 1)

        popup = getPopup

        popup.show(AlignmentMenu.this, 0, getHeight)
      }
    })

    private def getPopup: PopupMenu = {
      new PopupMenu {
        add(new JLabel("Arrange selected widgets") {
          setBorder(new ZoomableBorder(0, 6, 0, 0))
          setFont(Utils.zoomFont(getFont, 1))
        }).setEnabled(false)
        addSeparator()
        add(leftAction)
        add(centerHorizontalAction)
        add(rightAction)
        add(topAction)
        add(centerVerticalAction)
        add(bottomAction)
        addSeparator()
        add(distributeHorizontalAction)
        add(distributeVerticalAction)
        addSeparator()
        add(stretchLeftAction)
        add(stretchRightAction)
        add(stretchTopAction)
        add(stretchBottomAction)
      }
    }

    private def setIcons(): Unit = {
      val size: Int = Utils.zoom(16)
      val color: Color = InterfaceColors.toolbarImage()

      leftAction.setIcon(Utils.iconScaledWithColor("/images/align-left.png", size, size, color))
      centerHorizontalAction.setIcon(Utils.iconScaledWithColor("/images/align-horizontal-center.png", size, size,
                                                               color))
      rightAction.setIcon(Utils.iconScaledWithColor("/images/align-right.png", size, size, color))
      topAction.setIcon(Utils.iconScaledWithColor("/images/align-top.png", size, size, color))
      centerVerticalAction.setIcon(Utils.iconScaledWithColor("/images/align-vertical-center.png", size, size, color))
      bottomAction.setIcon(Utils.iconScaledWithColor("/images/align-bottom.png", size, size, color))
      distributeHorizontalAction.setIcon(Utils.iconScaledWithColor("/images/distribute-horizontal.png", size, size,
                                                                   color))
      distributeVerticalAction.setIcon(Utils.iconScaledWithColor("/images/distribute-vertical.png", size, size, color))
      stretchLeftAction.setIcon(Utils.iconScaledWithColor("/images/stretch-left.png", size, size, color))
      stretchRightAction.setIcon(Utils.iconScaledWithColor("/images/stretch-right.png", size, size, color))
      stretchTopAction.setIcon(Utils.iconScaledWithColor("/images/stretch-top.png", size, size, color))
      stretchBottomAction.setIcon(Utils.iconScaledWithColor("/images/stretch-bottom.png", size, size, color))
    }

    override def zoom(oldZoom: Float): Unit = {
      setDiameter(Utils.zoom(6))
      setIcons()

      Seq(
        leftAction,
        centerHorizontalAction,
        rightAction,
        topAction,
        centerVerticalAction,
        bottomAction,
        distributeHorizontalAction,
        distributeVerticalAction,
        stretchLeftAction,
        stretchRightAction,
        stretchTopAction,
        stretchBottomAction
      ).foreach(Utils.zoomComponents(_, oldZoom))
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.toolbarControlBackground())
      setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
      setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
      setBorderColor(InterfaceColors.toolbarControlBorder())

      label.setForeground(InterfaceColors.toolbarText())

      setIcons()
    }
  }

  class SquareButton(action: Action) extends ToolBarToggleButton(action) with PreferredSize {
    setBorder(null)

    override def getPreferredSize: Dimension =
      new Dimension(widgetMenu.getPreferredSize.height, widgetMenu.getPreferredSize.height)
  }
}
