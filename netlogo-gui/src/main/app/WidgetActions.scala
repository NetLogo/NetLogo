package org.nlogo.app

import java.awt.Rectangle
import javax.swing.JComponent
import javax.swing.undo.{AbstractUndoableEdit, UndoManager, UndoableEdit}

import org.nlogo.core.Widget
import org.nlogo.swing.ToolBarButton
import org.nlogo.window.MouseMode

object WidgetActions {

  private var counter = 0
  private val undoManager = new UndoManager() {
    override def addEdit(anEdit: UndoableEdit): Boolean = {
      undoButton.setEnabled(true)
      super.addEdit(anEdit)
    }
  }

  var undoButton : ToolBarButton = null
  var redoButton : ToolBarButton = null

  def undo(): Unit = {
    undoManager.undo()
    undoButton.setEnabled(undoManager.canUndo)
    redoButton.setEnabled(true)
    counter += 1
  }

  def redo(): Unit = {
    undoManager.redo()
    counter -= 1
    undoButton.setEnabled(true)
    redoButton.setEnabled(counter != 0)
//    undoManager.canRedo always return false for some reason!
//    redoButton.setEnabled(undoManager.canRedo)
  }

  def setButtons(undo: ToolBarButton, redo: ToolBarButton): Unit = {
    this.undoButton = undo
    this.redoButton = redo
  }

  def addWidget(interfacePanel: InterfacePanel, coreWidget: Widget, x: Int, y: Int): Unit ={
    val ww: WidgetWrapper = interfacePanel.createWidget(coreWidget, x, y)
    undoManager.addEdit(new AddWidget(interfacePanel, ww))
  }
  def removeWidget(interfacePanel: WidgetPanel, ww: WidgetWrapper): Unit = {
    interfacePanel.deleteWidget(ww)
    undoManager.addEdit(new RemoveWidget(interfacePanel, ww))
  }
  def removeWidgets(interfacePanel: WidgetPanel, wws: Seq[WidgetWrapper]): Unit = {
    interfacePanel.deleteWidgets(wws)
    undoManager.addEdit(new RemoveMultipleWidgets(interfacePanel, wws))
  }

  def moveWidgets(interfacePanel: WidgetPanel): Unit = {
    val initialMap: Map[WidgetWrapper, Rectangle] = interfacePanel.widgetsBeingDragged.map(a => a -> {
      val bounds = a.originalBounds
      bounds.x -= WidgetWrapper.BORDER_E
      bounds.width += WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
      bounds.y -= WidgetWrapper.BORDER_N
      bounds.height += WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
      bounds
    })(collection.breakOut)
    val widgets = interfacePanel.widgetsBeingDragged
    interfacePanel.dropSelectedWidgets()
    val finalMap: Map[WidgetWrapper, Rectangle] = widgets.map(a => a -> a.getBounds())(collection.breakOut)
    undoManager.addEdit(new MoveWidgets(interfacePanel, widgets, initialMap, finalMap))
  }

  def resizeWidget(widgetWrapper: WidgetWrapper): Unit = {
    val initialBounds = widgetWrapper.originalBounds
    initialBounds.x -= WidgetWrapper.BORDER_E
    initialBounds.width += WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
    initialBounds.y -= WidgetWrapper.BORDER_N
    initialBounds.height += WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S

    widgetWrapper.doDrop()
    undoManager.addEdit(new ResizeWidget(widgetWrapper, initialBounds, widgetWrapper.getBounds()))
  }

  class AddWidget(interfacePanel: InterfacePanel, widgetWrapper: WidgetWrapper) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      interfacePanel.reAddWidget(widgetWrapper)
    }
    override def undo(): Unit = {
      interfacePanel.deleteWidget(widgetWrapper)
    }
  }

  class RemoveWidget(interfacePanel: WidgetPanel, ww: WidgetWrapper) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      interfacePanel.deleteWidget(ww)
    }
    override def undo(): Unit = {
      interfacePanel.reAddWidget(ww)
    }
  }

  class RemoveMultipleWidgets(interfacePanel: WidgetPanel, wws: Seq[WidgetWrapper]) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      interfacePanel.deleteWidgets(wws)
    }
    override def undo(): Unit = {
      for(ww <- wws){
        interfacePanel.reAddWidget(ww)
      }
    }
  }

  class MoveWidgets(interfacePanel: WidgetPanel, wws: Seq[WidgetWrapper], initialMap: Map[WidgetWrapper, Rectangle], finalMap: Map[WidgetWrapper, Rectangle]) extends AbstractUndoableEdit {
    override def redo:Unit = {
      for(widgetWrapper <- wws){
        val finalBound = new Rectangle(finalMap(widgetWrapper))
        if(!widgetWrapper.selected()){
          finalBound.x += WidgetWrapper.BORDER_E
          finalBound.width -= WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
          finalBound.y += WidgetWrapper.BORDER_N
          finalBound.height -= WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
        }
        widgetWrapper.setBounds(finalBound)
      }
    }
    override def undo(): Unit = {
      for(widgetWrapper <- wws){
        val initialBound = new Rectangle(initialMap(widgetWrapper))
        if(!widgetWrapper.selected()) {
          initialBound.x += WidgetWrapper.BORDER_E
          initialBound.width -= WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
          initialBound.y += WidgetWrapper.BORDER_N
          initialBound.height -= WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
        }
        widgetWrapper.setBounds(initialBound)
      }
    }
  }

  class ResizeWidget(widgetWrapper: WidgetWrapper, initialBounds: Rectangle, finalBounds: Rectangle) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      val fb = new Rectangle(finalBounds)
      if(!widgetWrapper.selected()){
        fb.x += WidgetWrapper.BORDER_E
        fb.width -= WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
        fb.y += WidgetWrapper.BORDER_N
        fb.height -= WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
      }
      widgetWrapper.setBounds(fb)
    }
    override def undo(): Unit = {
      val ib = new Rectangle(initialBounds)
      if(!widgetWrapper.selected()) {
        ib.x += WidgetWrapper.BORDER_E
        ib.width -= WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
        ib.y += WidgetWrapper.BORDER_N
        ib.height -= WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
      }
      widgetWrapper.setBounds(ib)
    }
  }
}
