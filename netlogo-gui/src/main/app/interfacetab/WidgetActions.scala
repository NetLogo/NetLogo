// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.Rectangle
import javax.swing.undo.AbstractUndoableEdit

import org.nlogo.swing.UndoManager
import org.nlogo.window.{ Events, InterfaceMode }

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

  def moveWidgets(widgetPanel: WidgetPanel, moves: Seq[(WidgetWrapper, Int, Int)]): Unit = {
    undoManager.addEdit(new MoveWidgets(widgetPanel, moves.map {
      case (wrapper: WidgetWrapper, x: Int, y: Int) =>
        val oldBounds: Rectangle = wrapper.widget.getUnzoomedBounds

        wrapper.setLocation(x, y)

        val newBounds: Rectangle = wrapper.unzoomBounds(wrapper.widgetBounds)

        wrapper.widget.setUnzoomedBounds(newBounds)

        (wrapper, oldBounds, newBounds)
    }))
  }

  def moveSelectedWidgets(widgetPanel: WidgetPanel): Unit = {
    val wrappers: Seq[WidgetWrapper] = widgetPanel.widgetsBeingDragged
    val oldBounds: Seq[Rectangle] = wrappers.map(_.widget.getUnzoomedBounds)

    widgetPanel.dropSelectedWidgets()

    val newBounds: Seq[Rectangle] = wrappers.map(wrapper => wrapper.unzoomBounds(wrapper.widgetBounds))

    undoManager.addEdit(new MoveWidgets(widgetPanel, wrappers.lazyZip(oldBounds).lazyZip(newBounds).toSeq))
  }

  def resizeWidget(widgetPanel: WidgetPanel, wrapper: WidgetWrapper): Unit = {
    val oldBounds: Rectangle = wrapper.widget.getUnzoomedBounds

    wrapper.doDrop()

    val newBounds: Rectangle = wrapper.unzoomBounds(wrapper.widgetBounds)

    undoManager.addEdit(new ResizeWidgets(widgetPanel, Seq((wrapper, oldBounds, newBounds))))
  }

  def resizeWidgets(widgetPanel: WidgetPanel, resizes: Seq[(WidgetWrapper, Int, Int)]): Unit = {
    undoManager.addEdit(new ResizeWidgets(widgetPanel, resizes.map {
      case (wrapper: WidgetWrapper, width: Int, height: Int) =>
        val oldBounds: Rectangle = wrapper.widget.getUnzoomedBounds

        wrapper.setSize(width, height)

        val newBounds: Rectangle = wrapper.unzoomBounds(wrapper.widgetBounds)

        wrapper.widget.setUnzoomedBounds(newBounds)

        (wrapper, oldBounds, newBounds)
    }))
  }

  def stretchWidgets(widgetPanel: WidgetPanel, stretches: Seq[(WidgetWrapper, Rectangle)]): Unit = {
    undoManager.addEdit(new StretchWidgets(widgetPanel, stretches.map {
      case (wrapper: WidgetWrapper, bounds: Rectangle) =>
        val oldBounds: Rectangle = wrapper.widget.getUnzoomedBounds

        wrapper.setBounds(bounds)

        val newBounds: Rectangle = wrapper.unzoomBounds(wrapper.widgetBounds)

        wrapper.widget.setUnzoomedBounds(newBounds)

        (wrapper, oldBounds, newBounds)
    }))
  }

  def convertWidgetSizes(widgetPanel: WidgetPanel, wrappers: Seq[(WidgetWrapper, Rectangle)]): Unit = {
    undoManager.addEdit(new ConvertWidgetSizes(widgetPanel, wrappers.map {
      case (ww, oldBounds) => (ww, oldBounds, ww.getBounds())
    }))
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

  abstract class ChangeWidgetBounds(widgetPanel: WidgetPanel, changes: Seq[(WidgetWrapper, Rectangle, Rectangle)])
    extends AbstractUndoableEdit {

    override def redo(): Unit = {
      changes.foreach {
        case (wrapper: WidgetWrapper, _, bounds: Rectangle) =>
          setBounds(wrapper, bounds)
      }

      new Events.DirtyEvent(None).raise(widgetPanel)
    }

    override def undo(): Unit = {
      changes.foreach {
        case (wrapper: WidgetWrapper, bounds: Rectangle, _) =>
          setBounds(wrapper, bounds)
      }

      new Events.DirtyEvent(None).raise(widgetPanel)
    }

    private def setBounds(wrapper: WidgetWrapper, bounds: Rectangle): Unit = {
      wrapper.setBounds(wrapper.addWrapperBorder(wrapper.zoomBounds(bounds)))
      wrapper.widget.setUnzoomedBounds(bounds)
    }
  }

  class MoveWidgets(widgetPanel: WidgetPanel, changes: Seq[(WidgetWrapper, Rectangle, Rectangle)])
    extends ChangeWidgetBounds(widgetPanel, changes) {

    override def getPresentationName: String = "Widget Movement"
  }

  class ResizeWidgets(widgetPanel: WidgetPanel, changes: Seq[(WidgetWrapper, Rectangle, Rectangle)])
    extends ChangeWidgetBounds(widgetPanel, changes) {

    override def getPresentationName: String = "Widget Resizing"
  }

  class StretchWidgets(widgetPanel: WidgetPanel, changes: Seq[(WidgetWrapper, Rectangle, Rectangle)])
    extends ChangeWidgetBounds(widgetPanel, changes) {

    override def getPresentationName: String = "Widget Stretching"
  }

  class ConvertWidgetSizes(widgetPanel: WidgetPanel, wrappers: Seq[(WidgetWrapper, Rectangle, Rectangle)]) extends AbstractUndoableEdit {
    override def redo: Unit = {
      widgetPanel.setInterfaceMode(InterfaceMode.Interact, true)

      for ((ww, _, bounds) <- wrappers) {
        if (ww.widget.oldSize) {
          ww.widget.oldSize(false)
          ww.setBounds(bounds)
        }
      }
    }

    override def undo: Unit = {
      widgetPanel.setInterfaceMode(InterfaceMode.Interact, true)

      for ((ww, bounds, _) <- wrappers) {
        ww.widget.oldSize(true)
        ww.setBounds(bounds)
      }
    }

    override def getPresentationName = "Convert Widget Sizes"
  }
}
