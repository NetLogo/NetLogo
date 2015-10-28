// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.util.HashSet
import javax.swing.{JMenuItem, JPopupMenu, JButton, ButtonGroup, JToggleButton, AbstractAction, Action, ImageIcon}
import org.nlogo.api.{Editable}
import org.nlogo.core.I18N
import java.awt.event.{ActionListener, MouseAdapter, MouseEvent, ActionEvent}
import org.nlogo.window.{WidgetInfo, EditDialogFactoryInterface, Widget}

class InterfaceToolBar(wPanel: WidgetPanel,
                       workspace: org.nlogo.window.GUIWorkspace,
                       WidgetInfos: List[WidgetInfo],
                       frame: java.awt.Frame,
                       dialogFactory: EditDialogFactoryInterface) extends org.nlogo.swing.ToolBar
  with WidgetCreator
  with org.nlogo.window.Events.WidgetForegroundedEvent.Handler
  with org.nlogo.window.Events.WidgetRemovedEvent.Handler
  with org.nlogo.window.Events.WidgetAddedEvent.Handler
  with Events.WidgetSelectedEvent.Handler
  with org.nlogo.window.Events.LoadBeginEvent.Handler
  with org.nlogo.window.Events.EditWidgetEvent.Handler
  with java.awt.event.ActionListener {

  private val selectedObjects = new collection.mutable.HashSet[Widget]
  private val editAction = new EditAction()
  private val editButton = new JButton(editAction)
  private val addAction = new AddAction
  private val addButton = new AddButton
  private val group = new ButtonGroup()
  private val noneButton = new JToggleButton()
  private val deleteAction = new DeleteAction()
  private val deleteButton = new JButton(deleteAction)
  private val widgetMenu = new WidgetMenu

  wPanel.setWidgetCreator(this)
  // on Macs we want the window background but not on other systems
  if(System.getProperty("os.name").startsWith("Mac")) {
    setOpaque(true)
    setBackground(java.awt.SystemColor.window)
  }
  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  addButton.setToolTipText(I18N.gui.get("tabs.run.addButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))
  widgetMenu.setToolTipText(I18N.gui.get("tabs.run.widgets.tooltip"))

  class EditAction extends AbstractAction(I18N.gui.get("tabs.run.editButton")) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceToolBar].getResource("/images/edit.gif")))
    def actionPerformed(e: java.awt.event.ActionEvent) {
      new org.nlogo.window.Events.EditWidgetEvent(null).raise(InterfaceToolBar.this)
    }
  }

  class AddAction extends AbstractAction(I18N.gui.get("tabs.run.addButton")) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceToolBar].getResource("/images/add.gif")))
    def actionPerformed(e: java.awt.event.ActionEvent) { }
  }

  def getWidget =
    if(noneButton.isSelected) null
    else {
      noneButton.setSelected(true)
      wPanel.makeWidget(widgetMenu.getSelectedWidgetType, false)
    }

  var editTarget: Option[Editable] = None

  def handle(e: org.nlogo.window.Events.EditWidgetEvent) {
    // this is to support the "Edit..." button in the view control strip - ST 7/18/03
    val targetOption = Option(e.widget).orElse{
      if(!editButton.isEnabled) return
      editTarget
    }.filter(wPanel.contains)
    for(target <- targetOption) {
      def suppress(b: Boolean) {
        target match {
          case w: org.nlogo.window.JobWidget => w.suppressRecompiles(b)
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
    def actionPerformed(e: java.awt.event.ActionEvent) {
      wPanel.deleteSelectedWidgets()
    }
  }

  private class AddButton extends JToggleButton(addAction) {
    // normally ToggleButtons when pressed again stay pressed, but we want it to pop back up if
    // pressed again; this variable is used to produce that behavior - ST 7/30/03, 2/22/07
    private var wasSelectedWhenMousePressed = false
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) { wasSelectedWhenMousePressed = isSelected }
    })
    addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) { if(wasSelectedWhenMousePressed) noneButton.setSelected(true) }
    })
  }

  override def addControls() {
    Seq(editButton, deleteButton, addButton, widgetMenu).foreach(add)
    group.add(noneButton)
    group.add(addButton)
    noneButton.setSelected(true)
  }

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) {
    editAction.setEnabled(false)
    deleteAction.setEnabled(false)
    noneButton.setSelected(true)
    widgetMenu.setSelectedString(I18N.gui.get("tabs.run.widgets.button"))
  }

  def handle(e: org.nlogo.window.Events.WidgetRemovedEvent) {
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

  def handle(e: org.nlogo.window.Events.WidgetAddedEvent) {
    for(i <- widgetMenu.items) i.setEnabled(wPanel.canAddWidget(i.getText))
    widgetMenu.updateSelected()
  }

  private val deleteableObjects = new HashSet[Widget]

  def handle(e: Events.WidgetSelectedEvent) {
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

  def handle(e: org.nlogo.window.Events.WidgetForegroundedEvent) {
    editTarget = Some(e.widget.getEditable).collect{case editable: Editable => editable}
    editAction.setEnabled(editTarget.isDefined)
  }

  def actionPerformed(e: java.awt.event.ActionEvent) { addButton.setSelected(true) }

  def getItems: Array[JMenuItem] = WidgetInfos.map( spec => new JMenuItem(spec.displayName, spec.icon)).toArray
  class WidgetMenu extends org.nlogo.swing.ToolBarComboBox(getItems) {
    def getSelectedWidgetType = WidgetInfos.find(_.displayName == getSelectedItem.getText).get.widgetType
    override def populate(menu: JPopupMenu) {
      super.populate(menu)
      for(i <- items) {
        i.setEnabled(wPanel.canAddWidget(i.getText))
        i.addActionListener(InterfaceToolBar.this)
      }
    }
  }
}
