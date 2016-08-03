package org.nlogo.app

import java.awt.Rectangle
import javax.swing.undo.AbstractUndoableEdit

import org.nlogo.core.Widget

object WidgetActions {

  val undoManager = new org.nlogo.editor.UndoManager() {
    // The default one doesn't work!
    override def canRedo: Boolean = {
      if(editToBeRedone() != null) {
        return true
      }
      false
    }
  }

  def addWidget(widgetPanel: WidgetPanel, coreWidget: Widget, x: Int, y: Int): Unit = {
    val ww: WidgetWrapper = widgetPanel.createWidget(coreWidget, x, y)
    undoManager.addEdit(new AddWidget(widgetPanel, ww))
  }
  def addWidget(widgetPanel: WidgetPanel, widget: org.nlogo.window.Widget, x: Int, y: Int): Unit = {
    val ww: WidgetWrapper = widgetPanel.addWidget(widget, x, y, true, false)
    undoManager.addEdit(new AddWidget(widgetPanel, ww))
  }
  def removeWidget(widgetPanel: WidgetPanel, ww: WidgetWrapper): Unit = {
    widgetPanel.deleteWidget(ww)
    undoManager.addEdit(new RemoveWidget(widgetPanel, ww))
  }
  def removeWidgets(widgetPanel: WidgetPanel, wws: Seq[WidgetWrapper]): Unit = {
    widgetPanel.deleteWidgets(wws)
    undoManager.addEdit(new RemoveMultipleWidgets(widgetPanel, wws))
  }

  def addSelectionMargin(bounds: Rectangle): Rectangle = {
    bounds.x -= WidgetWrapper.BORDER_E
    bounds.width += WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
    bounds.y -= WidgetWrapper.BORDER_N
    bounds.height += WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
    bounds
  }

  def removeSelectionMargin(bounds: Rectangle): Rectangle = {
    bounds.x += WidgetWrapper.BORDER_E
    bounds.width -= WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W
    bounds.y += WidgetWrapper.BORDER_N
    bounds.height -= WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S
    bounds
  }

  def moveWidgets(widgetPanel: WidgetPanel): Unit = {
    val initialMap: Map[WidgetWrapper, Rectangle] = widgetPanel.widgetsBeingDragged.map(a => a -> {
      addSelectionMargin(a.originalBounds)
    })(collection.breakOut)
    val widgets = widgetPanel.widgetsBeingDragged
    widgetPanel.dropSelectedWidgets()
    val finalMap: Map[WidgetWrapper, Rectangle] = widgets.map(a => a -> a.getBounds())(collection.breakOut)
    undoManager.addEdit(new MoveWidgets(widgetPanel, widgets, initialMap, finalMap))
  }

  def resizeWidget(widgetWrapper: WidgetWrapper): Unit = {
    val initialBounds = widgetWrapper.originalBounds
    addSelectionMargin(initialBounds)
    widgetWrapper.doDrop()
    undoManager.addEdit(new ResizeWidget(widgetWrapper, initialBounds, widgetWrapper.getBounds()))
  }

  class AddWidget(widgetPanel: WidgetPanel, widgetWrapper: WidgetWrapper) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      widgetPanel.reAddWidget(widgetWrapper)
    }
    override def undo(): Unit = {
      widgetPanel.deleteWidget(widgetWrapper)
    }
    override def getPresentationName: String = "Widget Addition"
  }

  class RemoveWidget(widgetPanel: WidgetPanel, ww: WidgetWrapper) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      widgetPanel.deleteWidget(ww)
    }
    override def undo(): Unit = {
      widgetPanel.reAddWidget(ww)
    }
    override def getPresentationName: String = "Widget Deletion"
  }

  class RemoveMultipleWidgets(widgetPanel: WidgetPanel, wws: Seq[WidgetWrapper]) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      widgetPanel.deleteWidgets(wws)
    }
    override def undo(): Unit = {
      for(ww <- wws){
        widgetPanel.reAddWidget(ww)
      }
    }
    override def getPresentationName: String = "Widget(s) Deletion"
  }

  class MoveWidgets(widgetPanel: WidgetPanel, wws: Seq[WidgetWrapper], initialMap: Map[WidgetWrapper, Rectangle], finalMap: Map[WidgetWrapper, Rectangle]) extends AbstractUndoableEdit {
    override def redo:Unit = {
      for(widgetWrapper <- wws){
        val finalBound = new Rectangle(finalMap(widgetWrapper))
        if(!widgetWrapper.selected()){
          removeSelectionMargin(finalBound)
        }
        widgetWrapper.setBounds(finalBound)
      }
    }
    override def undo(): Unit = {
      for(widgetWrapper <- wws){
        val initialBound = new Rectangle(initialMap(widgetWrapper))
        if(!widgetWrapper.selected()) {
          removeSelectionMargin(initialBound)
        }
        widgetWrapper.setBounds(initialBound)
      }
    }
    override def getPresentationName: String = "Widget Movement"
  }

  class ResizeWidget(widgetWrapper: WidgetWrapper, initialBounds: Rectangle, finalBounds: Rectangle) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      val fb = new Rectangle(finalBounds)
      if(!widgetWrapper.selected()){
        removeSelectionMargin(fb)
      }
      widgetWrapper.setBounds(fb)
    }
    override def undo(): Unit = {
      val ib = new Rectangle(initialBounds)
      if(!widgetWrapper.selected()) {
        removeSelectionMargin(ib)
      }
      widgetWrapper.setBounds(ib)
    }
    override def getPresentationName: String = "Widget Resizing"
  }
}
