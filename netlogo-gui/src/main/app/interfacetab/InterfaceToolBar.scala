// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, ButtonGroup, JLabel, JMenuItem, JPanel, JPopupMenu }

import org.nlogo.api.Editable
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ DropdownArrow, ToolBar, ToolBarToggleButton, Utils }
import org.nlogo.window.{ EditDialogFactoryInterface, Events => WindowEvents, GUIWorkspace, InterfaceColors, JobWidget,
                          RoundedBorderPanel, ThemeSync, Widget, WidgetInfo }

import scala.collection.mutable.HashSet

class InterfaceToolBar(wPanel: WidgetPanel,
                       workspace: GUIWorkspace,
                       WidgetInfos: List[WidgetInfo],
                       frame: Frame,
                       dialogFactory: EditDialogFactoryInterface) extends ToolBar
  with WindowEvents.WidgetForegroundedEvent.Handler
  with WindowEvents.WidgetRemovedEvent.Handler
  with AppEvents.WidgetSelectedEvent.Handler
  with WindowEvents.EditWidgetEvent.Handler
  with WindowEvents.WidgetAddedEvent.Handler
  with WindowEvents.SelectModeEvent.Handler
  with ThemeSync {

  private val selectedObjects = new HashSet[Widget]

  private val selectButton = new ToolBarToggleButton(new SelectAction)
  private val editButton = new ToolBarToggleButton(new EditAction)
  private val deleteButton = new ToolBarToggleButton(new DeleteAction)

  private val buttonGroup = new ButtonGroup

  private val widgetMenu = new WidgetMenu
  private val alignmentMenu = new AlignmentMenu

  selectButton.setToolTipText(I18N.gui.get("tabs.run.selectButton.tooltip"))
  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))

  class SelectAction extends AbstractAction(null, Utils.iconScaled("/images/select.png", 15, 15)) {
    def actionPerformed(e: ActionEvent) {
      wPanel.beginSelect()
    }
  }

  class EditAction extends AbstractAction(null, Utils.icon("/images/edit.png")) {
    def actionPerformed(e: ActionEvent) {
      new WindowEvents.EditWidgetEvent(null).raise(InterfaceToolBar.this)

      wPanel.beginEdit()
    }
  }

  class DeleteAction extends AbstractAction(null, Utils.icon("/images/delete.png")) {
    def actionPerformed(e: ActionEvent) {
      wPanel.deleteSelectedWidgets()
      wPanel.beginDelete()
    }
  }

  private var editTarget: Option[Editable] = None

  def handle(e: WindowEvents.EditWidgetEvent) {
    // this is to support the "Edit..." button in the view control strip - ST 7/18/03
    val targetOption = Option(e.widget).orElse {
      if (!editButton.isEnabled) return
      editTarget
    }.filter(wPanel.contains)
    for(target <- targetOption) {
      def suppress(b: Boolean) {
        target match {
          case w: JobWidget => w.suppressRecompiles(b)
          case _ =>
        }
      }
      suppress(true)
      wPanel.editWidgetFinished(target, dialogFactory.canceled(frame, target, false))
      suppress(false)
    }
  }

  override def addControls() {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.anchor = GridBagConstraints.CENTER
    c.weighty = 1
    c.insets = new Insets(0, 6, 0, 6)

    add(widgetMenu, c)

    c.insets = new Insets(0, 0, 0, 6)

    add(alignmentMenu, c)

    add(selectButton, c)
    add(editButton, c)

    c.insets = new Insets(0, 0, 0, 0)

    add(deleteButton, c)

    buttonGroup.add(selectButton)
    buttonGroup.add(editButton)
    buttonGroup.add(deleteButton)

    selectButton.setSelected(true)
  }

  def syncTheme() {
    setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

    widgetMenu.syncTheme()
    alignmentMenu.syncTheme()

    selectButton.setColor(InterfaceColors.TOOLBAR_BUTTON_PRESSED)
    editButton.setColor(InterfaceColors.TOOLBAR_BUTTON_PRESSED)
    deleteButton.setColor(InterfaceColors.TOOLBAR_BUTTON_PRESSED)

    // set action icons here
  }

  def handle(e: WindowEvents.WidgetRemovedEvent) {
    val r = e.widget
    if(selectedObjects.contains(r)) {
      if(r.isInstanceOf[Editable] && editTarget.exists(_ == r.asInstanceOf[Editable]))
        editTarget = None
      selectedObjects.remove(r)
    }
  }

  private def updateActions(widget: Widget) {
    if (wPanel.getWrapper(widget).selected)
      selectedObjects += widget
    else
      selectedObjects -= widget

    updateTarget(widget)
  }

  private def updateTarget(widget: Widget) {
    if (selectedObjects.size == 1)
      editTarget = Some(widget.getEditable).collect { case editable: Editable => editable }
    else
      editTarget = None
  }

  def handle(e: WindowEvents.WidgetAddedEvent) {
    updateActions(e.widget.asInstanceOf[Widget])
  }

  final def handle(e: AppEvents.WidgetSelectedEvent) {
    updateActions(e.widget)
  }

  def handle(e: WindowEvents.WidgetForegroundedEvent) {
    updateTarget(e.widget)
  }

  def handle(e: WindowEvents.SelectModeEvent) {
    selectButton.setSelected(true)
  }

  def getItems: Array[JMenuItem] = WidgetInfos.map(spec => new JMenuItem(spec.displayName, spec.icon)).toArray

  class WidgetMenu extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync {
    private class WidgetAction(item: JMenuItem) extends AbstractAction(item.getText, item.getIcon) {
      def actionPerformed(e: ActionEvent) {
        chosenItem = item

        wPanel.createShadowWidget(widgetMenu.getSelectedWidget)
      }

      def getText = item.getText
    }

    setDiameter(6)

    private val label = new JLabel(I18N.gui.get("tabs.run.addWidget"))
    private val arrow = new DropdownArrow

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(6, 6, 6, 6)

      add(label, c)
      add(arrow, c)
    }

    private val actions = getItems.map(new WidgetAction(_))

    private var chosenItem: JMenuItem = null

    val popup = new JPopupMenu

    popup.add(actions(0))
    popup.addSeparator()
    popup.add(actions(1))
    popup.add(actions(2))
    popup.add(actions(3))
    popup.add(actions(4))
    popup.addSeparator()
    popup.add(actions(5))
    popup.add(actions(6))
    popup.add(actions(7))
    popup.addSeparator()
    popup.add(actions(8))

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        if (!wPanel.addingWidget) {
          actions.foreach(action => action.setEnabled(wPanel.canAddWidget(action.getText)))

          popup.show(WidgetMenu.this, 0, getHeight)
        }
      }
    })

    def getSelectedWidget =
      WidgetInfos.find(_.displayName == chosenItem.getText).get.coreWidget
    
    def syncTheme() {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      label.setForeground(InterfaceColors.TOOLBAR_TEXT)

      arrow.setColor(InterfaceColors.TOOLBAR_TEXT)
    }
  }

  class AlignmentMenu extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync {
    private val label = new JLabel(I18N.gui.get("tabs.run.alignWidgets"))
    private val arrow = new DropdownArrow

    setDiameter(6)

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(6, 6, 6, 6)

      add(label, c)
      add(arrow, c)
    }

    private val leftAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignLeft"),
                                                Utils.iconScaled("/images/align-left.png", 16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignLeft(wPanel.getWrapper(selectedObjects.minBy(_.getParent.getX)))
      }
    }

    private val centerHorizontalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterHorizontal"),
                                                            Utils.iconScaled("/images/align-horizontal-center.png",
                                                                             16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignCenterHorizontal(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val rightAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignRight"),
                                                 Utils.iconScaled("/images/align-right.png", 16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignRight(wPanel.getWrapper(selectedObjects.maxBy(_.getParent.getX)))
      }
    }

    private val topAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignTop"),
                                               Utils.iconScaled("/images/align-top.png", 16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignTop(wPanel.getWrapper(selectedObjects.minBy(_.getParent.getY)))
      }
    }

    private val centerVerticalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterVertical"),
                                                          Utils.iconScaled("/images/align-vertical-center.png",
                                                                           16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignCenterVertical(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val bottomAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignBottom"),
                                                  Utils.iconScaled("/images/align-bottom.png", 16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignBottom(wPanel.getWrapper(selectedObjects.maxBy(_.getParent.getY)))
      }
    }

    private val distributeHorizontalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.distributeHorizontal"),
                                                                Utils.iconScaled("/images/distribute-horizontal.png",
                                                                                 16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.distributeHorizontal()
      }
    }

    private val distributeVerticalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.distributeVertical"),
                                                              Utils.iconScaled("/images/distribute-vertical.png",
                                                                               16, 16)) {
      def actionPerformed(e: ActionEvent) {
        wPanel.distributeVertical()
      }
    }

    private val popup = new JPopupMenu

    popup.add(new JLabel("Arrange selected widgets") {
      setBorder(new javax.swing.border.EmptyBorder(0, 6, 0, 0))
    }).setEnabled(false)
    popup.addSeparator()
    popup.add(leftAction)
    popup.add(centerHorizontalAction)
    popup.add(rightAction)
    popup.add(topAction)
    popup.add(centerVerticalAction)
    popup.add(bottomAction)
    popup.addSeparator()
    popup.add(distributeHorizontalAction)
    popup.add(distributeVerticalAction)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        leftAction.setEnabled(selectedObjects.size > 1)
        centerHorizontalAction.setEnabled(selectedObjects.size > 1)
        rightAction.setEnabled(selectedObjects.size > 1)
        topAction.setEnabled(selectedObjects.size > 1)
        centerVerticalAction.setEnabled(selectedObjects.size > 1)
        bottomAction.setEnabled(selectedObjects.size > 1)
        distributeHorizontalAction.setEnabled(selectedObjects.size > 1)
        distributeVerticalAction.setEnabled(selectedObjects.size > 1)

        popup.show(AlignmentMenu.this, 0, getHeight)
      }
    })

    def syncTheme() {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      label.setForeground(InterfaceColors.TOOLBAR_TEXT)

      arrow.setColor(InterfaceColors.TOOLBAR_TEXT)
    }
  }
}
