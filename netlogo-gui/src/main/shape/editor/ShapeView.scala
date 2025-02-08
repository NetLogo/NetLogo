// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Color, Cursor, Dimension, Graphics, Point }
import java.awt.event.MouseEvent
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.JPanel
import javax.swing.event.MouseInputAdapter

import org.nlogo.api.Graphics2DWrapper
import org.nlogo.shape.{ Circle, Element, Line, Polygon, Rectangle, VectorShape }
import org.nlogo.swing.Utils

class ShapeView(editorDialog: EditorDialog, shape: VectorShape) extends JPanel with PropertyChangeListener {
  // Starting point for the shape, where mouse started
  private var start = new Point(0, 0)
  // Most recent location of mouse
  private var last = new Point(0, 0)
  // Point before most recent location of mouse
  private var previous = new Point(0, 0)
  // this is the new element that the user is right in the middle of creating
  private var tempElement: Option[Element] = None
  // Element that is selected for editing
  private var selectedElement: Option[Element] = None

  private var handles = Seq[Point]()

  private var draggingHandle = false
  private var draggingElement = false

  private var handleIndex = -1

  setBackground(Color.DARK_GRAY.darker.darker)
  setOpaque(true)
  setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))

  locally {
    val handler = new MouseInputAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        ShapeView.this.mousePressed(e)
      }

      override def mouseMoved(e: MouseEvent): Unit = {
        ShapeView.this.mouseMoved(e)
      }

      override def mouseDragged(e: MouseEvent): Unit = {
        ShapeView.this.mouseDragged(e)
      }

      override def mouseReleased(e: MouseEvent): Unit = {
        ShapeView.this.mouseReleased(e)
      }
    }

    addMouseListener(handler)
    addMouseMotionListener(handler)
  }

  def getHandleIndex: Int =
    handleIndex

  def getSelectedElement: Option[Element] =
    selectedElement

  def selectElement(element: Element): Unit = {
    selectedElement = Some(element)

    element.select()
  }

  def setTempElement(element: Element): Unit = {
    tempElement = Option(element)
  }

  // Set the size of the view
  override def getPreferredSize: Dimension =
    new Dimension(300, 300)

  override def getMinimumSize: Dimension =
    new Dimension(300, 300)

  override def getMaximumSize: Dimension =
    new Dimension(300, 300)

  // When a VectorShape has changed a PropertyChangeEvent is fired and handled here.
  // The corresponding instance of ShapeView is added as PropertyChangeListener in EditorDialog. AAB 9-22
  def propertyChange(e: PropertyChangeEvent): Unit = {
    repaint()
  }

  private def gridGapX =
    getWidth / VectorShape.NUM_GRID_LINES

  private def gridGapY =
    getHeight / VectorShape.NUM_GRID_LINES

  private def snapPointToGrid(point: Point): Point = {
    // val gridX = (point.x / gridGapX) * gridGapX
    // val gridY = (point.y / gridGapY) * gridGapY

    // if (point.x < gridX + gridGapX / 2) {
    //   if (point.y < gridY + gridGapY / 2) {
    //     new Point(gridX.toInt, gridY.toInt)
    //   } else {
    //     new Point(gridX.toInt, (gridY + gridGapY).toInt)
    //   }
    // } else {
    //   if (point.y < gridY + gridGapY / 2) {
    //     new Point((gridX + gridGapX).toInt, gridY.toInt)
    //   } else {
    //     new Point((gridX.toInt + gridGapX).toInt, (gridY + gridGapY).toInt)
    //   }
    // }

    new Point((point.x.toDouble / gridGapX).round.toInt * gridGapX, (point.y.toDouble / gridGapY).round.toInt * gridGapY)
  }

  // Finishes a polygon when the user presses a button before
  // finishing the polygon
  def selfFinishPolygon(add: Boolean): Unit = {
    tempElement.foreach(_ match {
      case p: Polygon =>
        p.selfClose()
        p.filled = editorDialog.isFillShapes

        editorDialog.makeUndoableDraw(p)

        if (add)
          shape.add(p)

        tempElement = None

      case _ =>
    })
  }

  // Paint all elements of the model
  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val g2d = new Graphics2DWrapper(Utils.initGraphics2D(g))

    // If the shape is rotatable, draw a gray background with a
    // black circle in the middle to show the user where to draw
    if (editorDialog.isRotatable) {
      g2d.setColor(Color.DARK_GRAY.darker)
      g2d.fillRect(0, 0, getWidth, getHeight)
      g2d.setColor(Color.BLACK)
      g.fillOval(0, 0, getWidth, getHeight)
    }

    g2d.setColor(Color.DARK_GRAY.darker)

    for (i <- 1 until VectorShape.NUM_GRID_LINES) {
      g2d.drawLine(i * Element.round(gridGapX), 0, i * Element.round(gridGapX), getHeight)
      g2d.drawLine(0, i * Element.round(gridGapY), getWidth, i * Element.round(gridGapY))
    }

    // Draw crosshairs on top of the grid
    g2d.setColor(Color.DARK_GRAY)
    g2d.drawLine(getWidth / 2.0, 0, getWidth / 2.0, getHeight)
    g2d.drawLine(0, getHeight / 2.0, getWidth, getWidth / 2.0)
    g2d.drawLine(0, VectorShape.NUM_GRID_LINES * Element.round(gridGapY), getWidth,
               VectorShape.NUM_GRID_LINES * Element.round(gridGapY))

    // Draw the elements
    g2d.antiAliasing(true)

    shape.getElements.foreach(element => {
      g2d.setColor(element.awtColor)

      element.draw(g2d, null, 300, 0)
    })

    tempElement.foreach(element => {
      g2d.setColor(editorDialog.getElementColor)

      element.draw(g2d, null, 300, 0)
    })

    handles = selectedElement.map(_.getHandles.map(handle => {
      g2d.setColor(Color.WHITE)
      g.drawRect(handle.x - 2, handle.y - 2, 4, 4)
      g2d.setColor(Color.BLACK)
      g.fillRect(handle.x - 1, handle.y - 1, 3, 3)

      handle
    }).toSeq).getOrElse(handles)
  }

  // Return the index in the handles if a handle was hit, else -1
  private def checkHandles(start: Point): Int = {
    // handles are 5 pixels wide & high, but for mousing purposes
    // we allow one extra pixel of slop, hence it's the center point
    // plus or minus 3 pixels - SAB/ST 6/11/04
    handles.indexWhere(handle => {
      start.x < handle.x + 3 && start.x > handle.x - 3 && start.y < handle.y + 3 && start.y > handle.y - 3
    })
  }

  private def selectHandle(index: Int, lastElement: Element): Unit = {
    draggingHandle = true
    handleIndex = index
    lastElement.setModifiedPoint(handles(index))
    editorDialog.makeUndoableModification(selectedElement.get, shape.getElements.indexOf(selectedElement.get))
  }

  // Deselect all the elements in the shape
  def deselectAll(): Unit = {
    shape.getElements.foreach(_.deselect())

    selectedElement = None

    shape.changed()
    repaint()
  }

  // Find which element the user pressed the mouse button inside
  private def checkElements(start: Point): Unit = {
    selectedElement.foreach(element => {
      val index = checkHandles(start)

      if (index != -1) {
        selectHandle(index, element)

        return
      }
    })

    // the elements are stored in back-to-front order, but we want
    // to favor front elements over back elements here, so we
    // scan the list backwards - SAB/ST 6/11/04
    shape.getElements.reverse.foreach(element => {
      if (element.contains(start)) {
        draggingElement = true

        selectedElement.foreach(_.deselect())

        element.select()

        selectedElement = Some(element)

        editorDialog.makeUndoableModification(element, shape.getElements.indexOf(element))

        shape.changed()
        repaint()

        return
      }
    })

    // if clicked on nothing, treat that as a request to deselect all
    deselectAll()
    repaint()
  }

  private def mousePressed(e: MouseEvent): Unit = {
    start = e.getPoint

    // if editing is on, find out if one of the shapes was clicked in
    if (editorDialog.isEditingElements)
      checkElements(start)

    // if snap to grid is on, change start to a grid corner
    if (editorDialog.isSnapToGrid) {
      start = snapPointToGrid(e.getPoint)
    } else {
      start = e.getPoint
    }

    last = start

    // If we're in the middle of drawing a polygon, don't start
    // over - instead, draw the next side
    tempElement.foreach(_ match {
      case p: Polygon =>
        p.addNewPoint(start)

        repaint()

      case _ =>
        if (editorDialog.getElementType == ElementType.Polygon) {
          tempElement = createElement(start, start)

          editorDialog.makeUndoableUnfinishedPolygon()
        }
    })
  }

  // Handler for mouse movement when drawing a polygon
  private def mouseMoved(e: MouseEvent): Unit = {
    // if snap to grid is on, change last to a grid corner
    if (editorDialog.isSnapToGrid) {
      last = snapPointToGrid(e.getPoint)
    } else {
      last = e.getPoint
    }

    // Sets the point that is currently last in the polygon to be <last>
    tempElement.foreach(_ match {
      case p: Polygon =>
        p.modifyPoint(last)

        repaint()

      case _ =>
    })
  }

  private def mouseDragged(e: MouseEvent): Unit = {
    previous = last

    // if snap to grid is on, change last to a grid corner
    if (editorDialog.isSnapToGrid) {
      last = snapPointToGrid(e.getPoint)
    } else {
      last = e.getPoint
    }

    if (editorDialog.isEditingElements) {
      if (draggingHandle) {
        selectedElement.foreach(_.reshapeElement(handles(handleIndex), last))
      } else if (draggingElement) {
        selectedElement.foreach(_.moveElement(last.x - previous.x, last.y - previous.y))
      }
    }

    // Don't affect polygons, since their movement is tracked by mouseMoved
    if (editorDialog.getElementType != ElementType.Polygon) {
      // If the element doesn't exist, create it
      // (NOTE: this will never be true for polygons)
      tempElement = tempElement.map(element => {
        element.modify(start, last)
        element
      }).orElse(createElement(start, last))
    }

    repaint()
  }

  private def mouseReleased(e: MouseEvent): Unit = {
    if (editorDialog.isEditingElements) {
      if (draggingHandle) {
        draggingHandle = false

        shape.changed()
      } else if (draggingElement) {
        draggingElement = false

        shape.changed()
      }
    }

    tempElement.foreach(element => {
      element match {
        case p: Polygon =>
          if (e.getClickCount == 2) {
            p.finishUp()

            if (p.xCoords.size >= 3) {
              p.filled = editorDialog.isFillShapes
              shape.add(p)
              editorDialog.makeUndoableDraw(p)
              p.select()
              selectedElement = Some(p)
              editorDialog.setEditingElements(true)
              shape.changed()
            }
          }

        case _ =>
          element.filled = editorDialog.isFillShapes
          shape.add(element)
          editorDialog.makeUndoableDraw(element)
          element.select()
          selectedElement = Some(element)
          editorDialog.setEditingElements(true)
          shape.changed()
      }

      tempElement = None

      repaint()
    })
  }

  // Private function to create and return a new shape based on
  // updated coordinates
  private def createElement(start: Point, last: Point): Option[Element] = {
    // don't create an element if the arrow tool is the current tool
    if (!editorDialog.isEditingElements) {
      editorDialog.getElementType match {
        case ElementType.Line =>
          Some(new Line(start, last, editorDialog.getElementColor))

        case ElementType.Rectangle =>
          Some(new Rectangle(start, last, editorDialog.getElementColor))

        case ElementType.Circle =>
          Some(new Circle(start, last, editorDialog.getElementColor))

        case ElementType.Polygon =>
          Some(new Polygon(start, editorDialog.getElementColor))

        // curves are not currently supported - ST 6/11/04
        // case VectorShape.CURVE:
        //   new Curve(start, last, editorDialog.getElementColor)

        // ellipses are not currently supported - ST 6/11/04
        // case VectorShape.ELLIPSE:
        //   new Ellipse(start, last, editorDialog.getElementColor)

        case _ =>
          None
      }
    } else {
      None
    }
  }
}
