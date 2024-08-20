// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Dimension, Frame, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import java.util.{ HashSet => JHashSet }
import javax.swing.{ Action, AbstractAction, ButtonGroup, JLabel, JMenuItem, JPanel, JPopupMenu, JToggleButton }
import javax.swing.event.{ PopupMenuEvent, PopupMenuListener }

import org.nlogo.api.Editable
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ DropdownArrow, ToolBar, ToolBarActionButton, ToolBarComboBox, ToolBarToggleButton, Utils }
import org.nlogo.window.{ EditDialogFactoryInterface, Events => WindowEvents, GUIWorkspace, InterfaceColors, JobWidget,
                          Widget, WidgetInfo }

import scala.collection.mutable.HashSet

class InterfaceToolBar(wPanel: WidgetPanel,
                       workspace: GUIWorkspace,
                       WidgetInfos: List[WidgetInfo],
                       frame: Frame,
                       dialogFactory: EditDialogFactoryInterface) extends ToolBar
  with WidgetCreator
  with WindowEvents.WidgetForegroundedEvent.Handler
  with WindowEvents.WidgetRemovedEvent.Handler
  with WindowEvents.WidgetAddedEvent.Handler
  with AppEvents.WidgetSelectedEvent.Handler
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.EditWidgetEvent.Handler
  with ActionListener {

  private val selectedObjects = new HashSet[Widget]
  private val editAction = new EditAction
  private val editButton = new ToolBarActionButton(editAction)
  private val addAction = new AddAction
  private val addButton = new AddButton
  private val group = new ButtonGroup
  private val noneButton = new JToggleButton
  private val deleteAction = new DeleteAction
  private val deleteButton = new ToolBarActionButton(deleteAction)
  private val widgetMenu = new WidgetMenu
  private val alignmentMenu = new AlignmentMenu

  wPanel.setWidgetCreator(this)

  setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  addButton.setToolTipText(I18N.gui.get("tabs.run.addButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))
  widgetMenu.setToolTipText(I18N.gui.get("tabs.run.widgets.tooltip"))

  class AddAction extends AbstractAction {
    putValue(Action.SMALL_ICON, Utils.icon("/images/add.gif"))
    def actionPerformed(e: ActionEvent) { }
  }

  class EditAction extends AbstractAction {
    putValue(Action.SMALL_ICON, Utils.icon("/images/edit.png"))
    def actionPerformed(e: ActionEvent) {
      new WindowEvents.EditWidgetEvent(null).raise(InterfaceToolBar.this)
    }
  }

  class DeleteAction extends AbstractAction {
    putValue(Action.SMALL_ICON, Utils.icon("/images/delete.png"))
    def actionPerformed(e: ActionEvent) {
      wPanel.deleteSelectedWidgets()
    }
  }

  def getWidget =
    if(noneButton.isSelected) null
    else {
      noneButton.setSelected(true)
      wPanel.makeWidget(widgetMenu.getSelectedWidget)
    }

  var editTarget: Option[Editable] = None

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
      editButton.setSelected(true)
      wPanel.editWidgetFinished(target, dialogFactory.canceled(frame, target, false))
      editButton.setSelected(false)
      suppress(false)
    }
  }

  private class AddButton extends ToolBarToggleButton(addAction) {
    // normally ToggleButtons when pressed again stay pressed, but we want it to pop back up if
    // pressed again; this variable is used to produce that behavior - ST 7/30/03, 2/22/07
    private var wasSelectedWhenMousePressed = false
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) { wasSelectedWhenMousePressed = isSelected }
    })
    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) { if (wasSelectedWhenMousePressed) noneButton.setSelected(true) }
    })
  }

  override def addControls() {
    setMargin(new Insets(0, 6, 0, 0))
    Seq(widgetMenu, alignmentMenu, editButton, deleteButton).foreach(add)
    group.add(noneButton)
    group.add(addButton)
    noneButton.setSelected(true)
  }

  def handle(e: WindowEvents.LoadBeginEvent) {
    editAction.setEnabled(false)
    deleteAction.setEnabled(false)
    noneButton.setSelected(true)
  }

  def handle(e: WindowEvents.WidgetRemovedEvent) {
    val r = e.widget
    if(selectedObjects.contains(r)) {
      if(r.isInstanceOf[Editable] && editTarget.exists(_ == r.asInstanceOf[Editable])) {
        editTarget = None
        editAction.setEnabled(false)
      }
      selectedObjects.remove(r)
      deleteableObjects.remove(r)
      deleteAction.setEnabled(!deleteableObjects.isEmpty)
    }
  }

  def handle(e: WindowEvents.WidgetAddedEvent) {
    widgetMenu.updateList(wPanel.canAddWidget)
  }

  private val deleteableObjects = new JHashSet[Widget]

  final def handle(e: AppEvents.WidgetSelectedEvent) {
    val w = e.widget
    if(wPanel.getWrapper(w).selected) {
      if(!selectedObjects.contains(w)) selectedObjects.add(w)
      if(w.deleteable && !deleteableObjects.contains(w)) deleteableObjects.add(w)
    }
    else {
      selectedObjects.remove(w)
      deleteableObjects.remove(w)
    }
    if(selectedObjects.isEmpty) {
      editTarget = None
      editAction.setEnabled(false)
    }
    deleteAction.setEnabled(!deleteableObjects.isEmpty)
  }

  def handle(e: WindowEvents.WidgetForegroundedEvent) {
    editTarget = Some(e.widget.getEditable).collect{case editable: Editable => editable}
    editAction.setEnabled(editTarget.isDefined)
  }

  def actionPerformed(e: ActionEvent) { addButton.setSelected(true) }

  def getItems: Array[JMenuItem] = WidgetInfos.map(spec => new JMenuItem(spec.displayName, spec.icon)).toArray

  class WidgetMenu extends ToolBarComboBox(getItems) {
    def getSelectedWidget =
      WidgetInfos.find(_.displayName == chosenItem.getText).get.coreWidget
    addActionListener(InterfaceToolBar.this)
    addPopupMenuListener(new PopupMenuListener {
      def popupMenuCanceled(e: PopupMenuEvent) {}
      def popupMenuWillBecomeInvisible(e: PopupMenuEvent) {}
      def popupMenuWillBecomeVisible(e: PopupMenuEvent) {
        widgetMenu.updateList(wPanel.canAddWidget)
      }
    })
  }

  class AlignmentMenu extends JPanel {
    setOpaque(false)
    setBackground(InterfaceColors.TRANSPARENT)

    setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(0, 6, 0, 6)

      add(new JLabel(Utils.icon("/images/align.png")), c)

      c.insets = new Insets(0, 0, 0, 6)

      add(new DropdownArrow, c)
    }

    setPreferredSize(new Dimension(getPreferredSize.width, widgetMenu.getPreferredSize.height))

    private val leftAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignLeft")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignLeft(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val centerHorizontalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterHorizontal")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignCenterHorizontal(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val rightAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignRight")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignRight(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val topAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignTop")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignTop(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val centerVerticalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterVertical")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignCenterVertical(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val bottomAction = new AbstractAction(I18N.gui.get("tabs.run.widget.alignBottom")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignBottom(wPanel.getWrapper(selectedObjects.head))
      }
    }

    private val distributeHorizontalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.distributeHorizontal")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.distributeHorizontal()
      }
    }

    private val distributeVerticalAction = new AbstractAction(I18N.gui.get("tabs.run.widget.distributeVertical")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.distributeVertical()
      }
    }

    private val popup = new JPopupMenu

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

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
      g2d.setColor(InterfaceColors.DARK_GRAY)
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)

      super.paintComponent(g)
    }
  }
}
