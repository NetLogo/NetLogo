// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Frame, SystemColor }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import java.util.HashSet
import javax.swing.{ AbstractAction, Action, ButtonGroup, ImageIcon,
  JButton, JMenuItem, JPopupMenu, JToggleButton }
import org.nlogo.api.{ Editable, I18N }
import org.nlogo.swing.{ ToolBar, ToolBarComboBox }
import org.nlogo.window.{ WidgetInfo, EditDialogFactoryInterface, GUIWorkspace, JobWidget, Widget }
import scala.collection.mutable

class InterfaceToolBar(wPanel: WidgetPanel,
  workspace: GUIWorkspace,
  WidgetInfos: List[WidgetInfo],
  frame: Frame,
  dialogFactory: EditDialogFactoryInterface) extends ToolBar
    with WidgetCreator
    with org.nlogo.window.Events.WidgetForegroundedEventHandler
    with org.nlogo.window.Events.WidgetRemovedEventHandler
    with org.nlogo.window.Events.WidgetAddedEventHandler
    with Events.WidgetSelectedEventHandler
    with org.nlogo.window.Events.LoadBeginEventHandler
    with org.nlogo.window.Events.EditWidgetEventHandler
    with ActionListener {

  private val selectedObjects = new mutable.HashSet[Widget]
  private val editAction = new EditAction
  private val editButton = new JButton(editAction)
  private val addAction = new AddAction
  private val addButton = new JToggleButton(addAction)
  private val deleteAction = new DeleteAction
  private val deleteButton = new JButton(deleteAction)
  private val widgetMenu = new WidgetMenu

  wPanel.setWidgetCreator(this)
  // on Macs we want the window background but not on other systems
  if(System.getProperty("os.name").startsWith("Mac")) {
    setOpaque(true)
    setBackground(SystemColor.window)
  }
  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  addButton.setToolTipText(I18N.gui.get("tabs.run.addButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))
  widgetMenu.setToolTipText(I18N.gui.get("tabs.run.widgets.tooltip"))

  class EditAction extends AbstractAction(I18N.gui.get("tabs.run.editButton")) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceToolBar].getResource("/images/edit.gif")))
    def actionPerformed(e: ActionEvent) =
      new org.nlogo.window.Events.EditWidgetEvent(null).raise(InterfaceToolBar.this)
  }

  class AddAction extends AbstractAction(I18N.gui.get("tabs.run.addButton")) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceToolBar].getResource("/images/add.gif")))
    def actionPerformed(e: ActionEvent) = {}
  }

  def getWidget =
    if(addButton.isSelected) {
      addButton.setSelected(false)
      wPanel.makeWidget(widgetMenu.getSelectedWidgetType, false)
    } else
      null

  var editTarget: Option[Editable] = None

  def handle(e: org.nlogo.window.Events.EditWidgetEvent): Unit = {
    // this is to support the "Edit..." button in the view control strip - ST 7/18/03
    val targetOption = Option(e.widget).orElse{
      if(!editButton.isEnabled) return
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
      wPanel.editWidgetFinished(target, dialogFactory.canceled(frame, target))
      editButton.setSelected(false)
      suppress(false)
    }
  }

  class DeleteAction extends AbstractAction(I18N.gui.get("tabs.run.deleteButton")) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceToolBar].getResource("/images/delete.gif")))
    def actionPerformed(e: ActionEvent) =
      wPanel.deleteSelectedWidgets()
  }

  override def addControls() =
    Seq(editButton, deleteButton, addButton, widgetMenu).foreach(add)

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) = {
    editAction.setEnabled(false)
    deleteAction.setEnabled(false)
    addButton.setSelected(false)
    widgetMenu.setSelectedString(I18N.gui.get("tabs.run.widgets.button"))
  }

  def handle(e: org.nlogo.window.Events.WidgetRemovedEvent) = {
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

  def handle(e: org.nlogo.window.Events.WidgetAddedEvent) = {
    for(i <- widgetMenu.items) i.setEnabled(wPanel.canAddWidget(i.getText))
    widgetMenu.updateSelected()
  }

  private val deleteableObjects = new HashSet[Widget]

  def handle(e: Events.WidgetSelectedEvent) = {
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

  def handle(e: org.nlogo.window.Events.WidgetForegroundedEvent) = {
    editTarget = Some(e.widget.getEditable).collect{case editable: Editable => editable}
    editAction.setEnabled(editTarget.isDefined)
  }

  def actionPerformed(e: ActionEvent) = addButton.setSelected(true)

  def getItems: Array[JMenuItem] = WidgetInfos.map( spec => new JMenuItem(spec.displayName, spec.icon)).toArray
  class WidgetMenu extends ToolBarComboBox(getItems) {
    def getSelectedWidgetType = WidgetInfos.find(_.displayName == getSelectedItem.getText).get.widgetType
    override def populate(menu: JPopupMenu) = {
      super.populate(menu)
      for(i <- items) {
        i.setEnabled(wPanel.canAddWidget(i.getText))
        i.addActionListener(InterfaceToolBar.this)
      }
    }
  }
}
