// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Dimension, Frame }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, Action, ButtonGroup, JLabel }

import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ AutomationUtils, BoxAlign, BoxColumn, BoxRow, DropdownArrow, MenuItem, MouseUtils, PopupMenu,
                         PreferredSize, RoundedBorderPanel, ToolBarToggleButton, Utils, Zoomable, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Editable, EditDialog, EditDialogFactory, Events => WindowEvents, GUIWorkspace, InterfaceMode,
                          JobWidget, Widget, WidgetInfo, WorldViewSettings }

import scala.collection.mutable.HashSet

class InterfaceWidgetControls(wPanel: WidgetPanel,
                              workspace: GUIWorkspace,
                              widgetInfos: Seq[WidgetInfo],
                              frame: Frame,
                              dialogFactory: EditDialogFactory)
  extends BoxRow(6)
  with AppEvents.WidgetSelectedEvent.Handler
  with WindowEvents.InterfaceModeChangedEvent.Handler
  with WindowEvents.WidgetForegroundedEvent.Handler
  with WindowEvents.WidgetRemovedEvent.Handler
  with WindowEvents.EditView3DEvent.Handler
  with WindowEvents.WidgetAddedEvent.Handler
  with ThemeSync {

  private val selectedObjects = new HashSet[Widget]

  val interactButton = new SquareButton(new InteractAction) {
    setIcon(Utils.iconScaledWithColor("/images/interact.png", 18, 18, () => {
      if (isSelected) {
        InterfaceColors.toolbarImageSelected()
      } else {
        InterfaceColors.toolbarImage()
      }
    }))
  }

  val selectButton = new SquareButton(new SelectAction) {
    setIcon(Utils.iconScaledWithColor("/images/select.png", 18, 18, () => {
      if (isSelected) {
        InterfaceColors.toolbarImageSelected()
      } else {
        InterfaceColors.toolbarImage()
      }
    }))
  }

  val editButton = new SquareButton(new EditAction) {
    setIcon(Utils.iconScaledWithColor("/images/edit.png", 18, 18, () => {
      if (isSelected) {
        InterfaceColors.toolbarImageSelected()
      } else {
        InterfaceColors.toolbarImage()
      }
    }))
  }

  val deleteButton = new SquareButton(new DeleteAction) {
    setIcon(Utils.iconScaledWithColor("/images/delete.png", 18, 18, () => {
      if (!isEnabled) {
        InterfaceColors.toolbarImageDisabled()
      } else if (isSelected) {
        InterfaceColors.toolbarImageSelected()
      } else {
        InterfaceColors.toolbarImage()
      }
    }))
  }

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

  setBorder(new ZoomableBorder(0, 6, 0, 0))

  add(widgetMenu)
  add(alignmentMenu)
  add(interactButton)
  add(selectButton)
  add(editButton)
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

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    widgetMenu.syncTheme()
    alignmentMenu.syncTheme()
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

  class WidgetMenu extends BoxRow(14) with RoundedBorderPanel with Zoomable with ThemeSync with MouseUtils {
    private val label = new JLabel(I18N.gui.get("tabs.run.addWidget"))
    private val arrow = new DropdownArrow

    setBorder(new ZoomableBorder(6, 8, 6, 6))
    setDiameter(Utils.zoom(6))

    add(label)
    add(new BoxColumn(arrow, BoxAlign.Center))

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
      setIcon(info.icon)

      override def addNotify(): Unit = {
        super.addNotify()

        setEnabled(wPanel.canAddWidget(info.displayName))
      }

      override def zoom(oldZoom: Float): Unit = {
        super.zoom(oldZoom)

        setIconTextGap(Utils.zoom(4))
      }
    }
  }

  class AlignmentMenu extends BoxRow(14) with RoundedBorderPanel with Zoomable with ThemeSync with MouseUtils {
    private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tabs.run.widget")

    private val label = new JLabel(I18N.gui.get("tabs.run.alignWidgets"))
    private val arrow = new DropdownArrow

    setBorder(new ZoomableBorder(6, 8, 6, 6))
    setDiameter(Utils.zoom(6))

    add(label)
    add(new BoxColumn(arrow, BoxAlign.Center))

    enableHover()

    private val leftAction = new AlignMenuItem("alignLeft", "align-left.png", _.alignLeft())
    private val centerHorizontalAction = new AlignMenuItem("alignCenterHorizontal", "align-horizontal-center.png",
                                                           _.alignCenterHorizontal())
    private val rightAction = new AlignMenuItem("alignRight", "align-right.png", _.alignRight())
    private val topAction = new AlignMenuItem("alignTop", "align-top.png", _.alignTop())
    private val centerVerticalAction = new AlignMenuItem("alignCenterVertical", "align-vertical-center.png",
                                                         _.alignCenterVertical())
    private val bottomAction = new AlignMenuItem("alignBottom", "align-bottom.png", _.alignBottom())
    private val distributeHorizontalAction = new AlignMenuItem("distributeHorizontal", "distribute-horizontal.png",
                                                               _.distributeHorizontal())
    private val distributeVerticalAction = new AlignMenuItem("distributeVertical", "distribute-vertical.png",
                                                             _.distributeVertical())
    private val stretchLeftAction = new AlignMenuItem("stretchLeft", "stretch-left.png", _.stretchLeft())
    private val stretchRightAction = new AlignMenuItem("stretchRight", "stretch-right.png", _.stretchRight())
    private val stretchTopAction = new AlignMenuItem("stretchTop", "stretch-top.png", _.stretchTop())
    private val stretchBottomAction = new AlignMenuItem("stretchBottom", "stretch-bottom.png", _.stretchBottom())

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

    override def zoom(oldZoom: Float): Unit = {
      setDiameter(Utils.zoom(6))

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
    }

    private class AlignMenuItem(name: String, image: String, action: WidgetPanel => Unit)
      extends MenuItem(I18N.gui(name), () => action(wPanel)) {

      setIcon(Utils.iconScaledWithColor(s"/images/$image", 16, 16, () => InterfaceColors.toolbarImage()))
    }
  }

  class SquareButton(action: Action) extends ToolBarToggleButton(action) with PreferredSize {
    setBorder(null)

    override def getPreferredSize: Dimension =
      new Dimension(widgetMenu.getPreferredSize.height, widgetMenu.getPreferredSize.height)
  }
}
