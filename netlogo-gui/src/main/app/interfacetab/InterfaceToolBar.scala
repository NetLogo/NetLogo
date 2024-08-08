// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Frame, Insets }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import java.util.{ HashSet => JHashSet }
import javax.swing.{ ButtonGroup, JMenuItem, JToggleButton,
  AbstractAction, Action }

import scala.collection.mutable

import org.nlogo.api.Editable
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ ToolBar, ToolBarActionButton, ToolBarToggleButton, Utils },
  Utils.icon
import org.nlogo.window.{ EditDialogFactoryInterface, Events => WindowEvents,
  GUIWorkspace, InterfaceColors, JobWidget, Widget, WidgetInfo }

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

  private val selectedObjects = new mutable.HashSet[Widget]
  private val editAction = new EditAction
  private val editButton = new ToolBarActionButton(editAction)
  private val addAction = new AddAction
  private val addButton = new AddButton
  private val group = new ButtonGroup
  private val noneButton = new JToggleButton
  private val deleteAction = new DeleteAction
  private val deleteButton = new ToolBarActionButton(deleteAction)
  private val widgetMenu = new WidgetMenu

  wPanel.setWidgetCreator(this)

  setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  addButton.setToolTipText(I18N.gui.get("tabs.run.addButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))
  widgetMenu.setToolTipText(I18N.gui.get("tabs.run.widgets.tooltip"))

  class EditAction extends AbstractAction(I18N.gui.get("tabs.run.editButton")) {
    putValue(Action.SMALL_ICON, icon("/images/edit.gif"))
    def actionPerformed(e: ActionEvent) {
      new WindowEvents.EditWidgetEvent(null).raise(InterfaceToolBar.this)
    }
  }

  class AddAction extends AbstractAction(I18N.gui.get("tabs.run.addButton")) {
    putValue(Action.SMALL_ICON, icon("/images/add.gif"))
    def actionPerformed(e: ActionEvent) { }
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

  class DeleteAction extends AbstractAction(I18N.gui.get("tabs.run.deleteButton")) {
    putValue(Action.SMALL_ICON, icon("/images/delete.gif"))
    def actionPerformed(e: ActionEvent) {
      wPanel.deleteSelectedWidgets()
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
    Seq(widgetMenu, editButton, deleteButton).foreach(add)
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
  class WidgetMenu extends org.nlogo.swing.ToolBarComboBox(getItems) {
    def getSelectedWidget =
      WidgetInfos.find(_.displayName == chosenItem.getText).get.coreWidget
    addActionListener(InterfaceToolBar.this)
    addPopupMenuListener(new javax.swing.event.PopupMenuListener {
      def popupMenuCanceled(e: javax.swing.event.PopupMenuEvent) {}
      def popupMenuWillBecomeInvisible(e: javax.swing.event.PopupMenuEvent) {}
      def popupMenuWillBecomeVisible(e: javax.swing.event.PopupMenuEvent) {
        widgetMenu.updateList(wPanel.canAddWidget)
      }
    })
  }
}
