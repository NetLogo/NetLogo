package org.nlogo.app.interfacetab

import java.awt.Rectangle
import javax.swing.undo.AbstractUndoableEdit

import org.nlogo.core.Widget
import org.nlogo.editor.UndoManager

object WidgetActions {

  val undoManager = new UndoManager {
    // The default one doesn't work!
    override def canRedo = editToBeRedone() != null
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

  def moveWidgets(moves: Seq[(WidgetWrapper, Int, Int)]) {
    undoManager.addEdit(new MoveWidgets(moves.map(move => {
      val oldBounds = move._1.getBounds()
      val newBounds = new Rectangle(move._2, move._3, move._1.getWidth, move._1.getHeight)

      move._1.setBounds(newBounds)
      
      (move._1, oldBounds, newBounds)
    })))
  }

  def moveSelectedWidgets(widgetPanel: WidgetPanel): Unit = {
    val initialMap: Map[WidgetWrapper, Rectangle] = widgetPanel.widgetsBeingDragged.map(a => a -> {
      addSelectionMargin(a.originalBounds)
    })(collection.breakOut)
    val widgets = widgetPanel.widgetsBeingDragged
    widgetPanel.dropSelectedWidgets()
    val finalMap: Map[WidgetWrapper, Rectangle] = widgets.map(a => a -> a.getBounds())(collection.breakOut)
    undoManager.addEdit(new MoveSelectedWidgets(widgetPanel, widgets, initialMap, finalMap))
  }

  def resizeWidget(widgetWrapper: WidgetWrapper): Unit = {
    val initialBounds = addSelectionMargin(widgetWrapper.originalBounds)
    widgetWrapper.doDrop()
    undoManager.addEdit(new ResizeWidget(widgetWrapper, initialBounds, widgetWrapper.getBounds()))
  }

  private def addSelectionMargin(bounds: Rectangle): Rectangle = {
    new Rectangle(bounds.x - WidgetWrapper.BORDER_E, bounds.y - WidgetWrapper.BORDER_N,
                  bounds.width + WidgetWrapper.BORDER_E + WidgetWrapper.BORDER_W,
                  bounds.height + WidgetWrapper.BORDER_N + WidgetWrapper.BORDER_S)
  }

  private def removeSelectionMargin(bounds: Rectangle): Rectangle = {
    new Rectangle(bounds.x + WidgetWrapper.BORDER_E, bounds.y + WidgetWrapper.BORDER_N,
                  bounds.width - WidgetWrapper.BORDER_E - WidgetWrapper.BORDER_W,
                  bounds.height - WidgetWrapper.BORDER_N - WidgetWrapper.BORDER_S)
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

  class MoveWidgets(moves: Seq[(WidgetWrapper, Rectangle, Rectangle)]) extends AbstractUndoableEdit {
    override def redo {
      for (move <- moves)
        setBounds(move._1, move._3)
    }

    override def undo {
      for (move <- moves)
        setBounds(move._1, move._2)
    }

    override def getPresentationName = "Widget Movement"

    private def setBounds(widgetWrapper: WidgetWrapper, bounds: Rectangle) {
      widgetWrapper.setBounds(
        if (widgetWrapper.selected)
          bounds
        else
          removeSelectionMargin(bounds)
      )
    }
  }

  class MoveSelectedWidgets(widgetPanel: WidgetPanel, wws: Seq[WidgetWrapper], initialMap: Map[WidgetWrapper, Rectangle], finalMap: Map[WidgetWrapper, Rectangle]) extends AbstractUndoableEdit {
    override def redo:Unit = {
      positionWidgets(finalMap)
    }

    override def undo(): Unit = {
      positionWidgets(initialMap)
    }

    override def getPresentationName: String = "Widget Movement"

    def positionWidgets(map: Map[WidgetWrapper, Rectangle]): Unit = {
      for (widgetWrapper <- wws) {
        widgetWrapper.setBounds(
          if (widgetWrapper.selected)
            map(widgetWrapper)
          else
            removeSelectionMargin(map(widgetWrapper))
        )
      }
    }
  }

  class ResizeWidget(widgetWrapper: WidgetWrapper, initialBounds: Rectangle, finalBounds: Rectangle) extends AbstractUndoableEdit {
    override def redo(): Unit = {
      setWidgetSize(finalBounds)
    }

    override def undo(): Unit = {
      setWidgetSize(initialBounds)
    }

    override def getPresentationName: String = "Widget Resizing"

    def setWidgetSize(bounds: Rectangle): Unit = {
      widgetWrapper.setBounds(
        if (widgetWrapper.selected)
          bounds
        else
          removeSelectionMargin(bounds)
      )
    }
  }
}
