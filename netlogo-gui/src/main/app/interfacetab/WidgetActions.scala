package org.nlogo.app.interfacetab

import java.awt.Rectangle
import javax.swing.undo.AbstractUndoableEdit

import org.nlogo.editor.UndoManager
import org.nlogo.window.InterfaceMode

object WidgetActions {

  val undoManager = new UndoManager {
    // The default one doesn't work!
    override def canRedo = editToBeRedone() != null
  }

  def addWidget(widgetPanel: WidgetPanel, ww: WidgetWrapper): Unit = {
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

  def moveWidgets(moves: Seq[(WidgetWrapper, Int, Int)]): Unit = {
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
    }).toMap
    val widgets = widgetPanel.widgetsBeingDragged
    widgetPanel.dropSelectedWidgets()
    val finalMap: Map[WidgetWrapper, Rectangle] = widgets.map(a => a -> a.getBounds()).toMap
    undoManager.addEdit(new MoveSelectedWidgets(widgetPanel, widgets, initialMap, finalMap))
  }

  def resizeWidget(widgetWrapper: WidgetWrapper): Unit = {
    val initialBounds = addSelectionMargin(widgetWrapper.originalBounds)
    widgetWrapper.doDrop()
    undoManager.addEdit(new ResizeWidget(widgetWrapper, initialBounds, widgetWrapper.getBounds()))
  }

  def resizeWidgets(wrappers: Seq[(WidgetWrapper, Int, Int)]): Unit = {
    undoManager.addEdit(new ReboundWidgets(wrappers.map {
      case (ww, width, height) =>
        val oldBounds = ww.getBounds()
        val newBounds = new Rectangle(ww.getX, ww.getY, width, height)

        ww.setBounds(newBounds)

        (ww, oldBounds, newBounds)
    }))
  }

  def reboundWidgets(wrappers: Seq[(WidgetWrapper, Rectangle)]): Unit = {
    undoManager.addEdit(new ReboundWidgets(wrappers.map {
      case (ww, newBounds) =>
        val oldBounds = ww.getBounds()

        ww.setBounds(newBounds)

        (ww, oldBounds, newBounds)
    }))
  }

  def convertWidgetSizes(widgetPanel: WidgetPanel, wrappers: Seq[(WidgetWrapper, Rectangle)]): Unit = {
    undoManager.addEdit(new ConvertWidgetSizes(widgetPanel, wrappers.map {
      case (ww, oldBounds) => (ww, oldBounds, ww.getBounds())
    }))
  }

  private def addSelectionMargin(bounds: Rectangle): Rectangle = {
    new Rectangle(bounds.x - WidgetWrapper.BorderSize, bounds.y - WidgetWrapper.BorderSize,
                  bounds.width + WidgetWrapper.BorderSize + WidgetWrapper.BorderSize,
                  bounds.height + WidgetWrapper.BorderSize + WidgetWrapper.BorderSize)
  }

  private def removeSelectionMargin(bounds: Rectangle): Rectangle = {
    new Rectangle(bounds.x + WidgetWrapper.BorderSize, bounds.y + WidgetWrapper.BorderSize,
                  bounds.width - WidgetWrapper.BorderSize - WidgetWrapper.BorderSize,
                  bounds.height - WidgetWrapper.BorderSize - WidgetWrapper.BorderSize)
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
    override def redo: Unit = {
      for (move <- moves)
        setBounds(move._1, move._3)
    }

    override def undo: Unit = {
      for (move <- moves)
        setBounds(move._1, move._2)
    }

    override def getPresentationName = "Widget Movement"

    private def setBounds(widgetWrapper: WidgetWrapper, bounds: Rectangle): Unit = {
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

  class ReboundWidgets(wrappers: Seq[(WidgetWrapper, Rectangle, Rectangle)]) extends AbstractUndoableEdit {
    override def redo: Unit = {
      for ((ww, _, bounds) <- wrappers)
        setBounds(ww, bounds)
    }

    override def undo: Unit = {
      for ((ww, bounds, _) <- wrappers)
        setBounds(ww, bounds)
    }

    override def getPresentationName = "Widget Stretching"

    private def setBounds(widgetWrapper: WidgetWrapper, bounds: Rectangle): Unit = {
      widgetWrapper.setBounds(
        if (widgetWrapper.selected)
          bounds
        else
          removeSelectionMargin(bounds)
      )
    }
  }

  class ConvertWidgetSizes(widgetPanel: WidgetPanel, wrappers: Seq[(WidgetWrapper, Rectangle, Rectangle)]) extends AbstractUndoableEdit {
    override def redo: Unit = {
      widgetPanel.setInterfaceMode(InterfaceMode.Interact, true)

      for ((ww, _, bounds) <- wrappers) {
        if (ww.widget().oldSize) {
          ww.widget().oldSize(false)
          ww.setBounds(bounds)
        }
      }
    }

    override def undo: Unit = {
      widgetPanel.setInterfaceMode(InterfaceMode.Interact, true)

      for ((ww, bounds, _) <- wrappers) {
        ww.widget().oldSize(true)
        ww.setBounds(bounds)
      }
    }

    override def getPresentationName = "Convert Widget Sizes"
  }
}
