// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.shape.Circle;
import org.nlogo.shape.Element;
import org.nlogo.shape.Line;
import org.nlogo.shape.Polygon;
import org.nlogo.shape.Rectangle;
import org.nlogo.shape.VectorShape;

import java.util.List;
import java.util.Observable;

strictfp class ShapeView
    extends javax.swing.JPanel
    implements java.util.Observer {

  private final VectorShape shape;
  private final EditorDialog editorDialog;
  // Starting point for the shape, where mouse started
  private java.awt.Point start;
  // Most recent location of mouse
  private java.awt.Point last;
  // Point before most recent location of mouse
  private java.awt.Point previous;
  // this is the new element that the user is right in the middle of creating
  private Element tempElement;

  // Element that is selected for editing
  private Element selectedElement = null;
  private java.awt.Point[] handles;
  private boolean draggingHandle = false;
  private boolean draggingElement = false;
  private boolean drawingPolygon = false;
  private int handleIndex;

  private static final boolean IS_MAC =
      System.getProperty("os.name").startsWith("Mac");

  private double gridGapX =
      (double) getBounds().width / VectorShape.NUM_GRID_LINES;
  private double gridGapY =
      (double) getBounds().height / VectorShape.NUM_GRID_LINES;

  ShapeView(EditorDialog editorDialog, VectorShape shape) {
    this.editorDialog = editorDialog;
    this.shape = shape;
    setBackground(java.awt.Color.DARK_GRAY.darker().darker());
    setOpaque(true); // needed with new Quaqua - ST 11/3/05
    setCursor(java.awt.Cursor.getPredefinedCursor
        (java.awt.Cursor.CROSSHAIR_CURSOR));
    javax.swing.event.MouseInputAdapter handler =
        new javax.swing.event.MouseInputAdapter() {
          @Override
          public void mousePressed(java.awt.event.MouseEvent e) {
            ShapeView.this.mousePressed(e);
          }

          @Override
          public void mouseMoved(java.awt.event.MouseEvent e) {
            ShapeView.this.mouseMoved(e);
          }

          @Override
          public void mouseDragged(java.awt.event.MouseEvent e) {
            ShapeView.this.mouseDragged(e);
          }

          @Override
          public void mouseReleased(java.awt.event.MouseEvent e) {
            ShapeView.this.mouseReleased(e);
          }
        };

    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  public int getHandleIndex() {
    return handleIndex;
  }

  public Element getSelectedElement() {
    return selectedElement;
  }

  public boolean hasSelectedElement() {
    return selectedElement != null;
  }

  public boolean drawingPolygon() {
    return drawingPolygon;
  }

  public void setDrawingPolygon(boolean drawing) {
    drawingPolygon = drawing;
  }

  public void selectElement(org.nlogo.shape.Element el) {
    selectedElement = el;
    el.select();
  }

  public void setTempElement(org.nlogo.shape.Element el) {
    tempElement = el;
  }

  // Set the size of the view
  @Override
  public java.awt.Dimension getPreferredSize() {
    return new java.awt.Dimension(300, 300);
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(300, 300);
  }

  @Override
  public java.awt.Dimension getMaximumSize() {
    return new java.awt.Dimension(300, 300);
  }

  public void update(Observable o, Object rect) {
    if (rect == null) {
      repaint();
    } else {
      repaint((java.awt.Rectangle) rect);
    }
  }

  public void snapPointToGrid(java.awt.Point pointToChange) {
    double clickedGridX =
        ((int) (pointToChange.getX() / gridGapX)) * gridGapX;
    double clickedGridY =
        ((int) (pointToChange.getY() / gridGapY)) * gridGapY;

    if (pointToChange.getX() < clickedGridX + (gridGapX / 2)) {
      if (pointToChange.getY() < clickedGridY + (gridGapY / 2)) {
        pointToChange.setLocation(clickedGridX, clickedGridY);
      } else {
        pointToChange.setLocation
            (clickedGridX, clickedGridY + gridGapY);
      }
    } else {
      if (pointToChange.getY() < clickedGridY + (gridGapY / 2)) {
        pointToChange.setLocation
            (clickedGridX + gridGapX, clickedGridY);
      } else {
        pointToChange.setLocation
            (clickedGridX + gridGapX, clickedGridY + gridGapY);
      }
    }
  }

  // Finishes a polygon when the user presses a button before
  // finishing the polygon
  public void selfFinishPolygon(boolean add) {
    if (tempElement instanceof Polygon) {
      ((Polygon) tempElement).selfClose();
      tempElement.setFilled(editorDialog.fillShapes());
      editorDialog.makeUndoableDraw(tempElement);
      if (add) {
        shape.add(tempElement);
      }
      drawingPolygon = false;
      tempElement = null;
    }
  }

  // Paint all elements of the model
  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    org.nlogo.api.Graphics2DWrapper g2 = new org.nlogo.api.Graphics2DWrapper((java.awt.Graphics2D) g);
    java.awt.Rectangle bounds = getBounds();
    List<Element> elements = shape.getElements();
    Element element;

    // If the shape is rotatable, draw a gray background with a
    // black circle in the middle to show the user where to draw
    if (editorDialog.isRotatable()) {
      g.setColor(java.awt.Color.DARK_GRAY.darker());
      g.fillRect(0, 0, bounds.width, bounds.height);
      g.setColor(java.awt.Color.BLACK);
      g.fillOval(0, 0, bounds.width, bounds.height);
    }

    gridGapX = (double) bounds.width / VectorShape.NUM_GRID_LINES;
    gridGapY = (double) bounds.height / VectorShape.NUM_GRID_LINES;
    g.setColor(java.awt.Color.DARK_GRAY.darker());

    for (int i = 1; i < VectorShape.NUM_GRID_LINES; ++i) {
      g.drawLine(i * Element.round(gridGapX), 0,
          i * Element.round(gridGapX), bounds.height);
      g.drawLine(0, i * Element.round(gridGapY),
          bounds.width, i * Element.round(gridGapY));
    }

    // Draw crosshairs on top of the grid
    g.setColor(java.awt.Color.DARK_GRAY);
    g.drawLine(bounds.width / 2, 0, bounds.width / 2, bounds.height);
    g.drawLine(0, bounds.height / 2, bounds.width, bounds.width / 2);
    g.drawLine(0,
        VectorShape.NUM_GRID_LINES * Element.round(gridGapY),
        bounds.width,
        VectorShape.NUM_GRID_LINES * Element.round(gridGapY));

    // Draw the elements
    g2.antiAliasing(true);

    for (int i = 0; i < elements.size(); ++i) {
      element = elements.get(i);
      g.setColor(element.getColor());
      element.draw(g2, null,
          IS_MAC ? 299 : 300,
          0);
    }

    if (tempElement != null) {
      g.setColor(editorDialog.getElementColor());
      tempElement.draw(g2, null,
          IS_MAC ? 299 : 300,
          0);
    }

    if (hasSelectedElement()) {
      handles = getSelectedElement().getHandles();
      for (int i = 0; i < handles.length; i++) {
        g.setColor(java.awt.Color.WHITE);
        g.drawRect(handles[i].x - 2, handles[i].y - 2, 4, 4);
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(handles[i].x - 1, handles[i].y - 1, 3, 3);
      }
    }
  }

  // Return the index in the handles if a handle was hit, else -1
  int checkHandles(java.awt.Point start) {
    // handles are 5 pixels wide & high, but for mousing purposes
    // we allow one extra pixel of slop, hence it's the center point
    // plus or minus 3 pixels - SAB/ST 6/11/04
    for (int i = 0; i < handles.length; i++) {
      if ((start.x < (handles[i].x + 3)) &&
          (start.x > (handles[i].x - 3)) &&
          (start.y < (handles[i].y + 3)) &&
          (start.y > (handles[i].y - 3))) {
        return i;
      }
    }
    return -1;
  }

  void selectHandle(int index, Element lastElement) {
    draggingHandle = true;
    handleIndex = index;
    lastElement.setModifiedPoint(handles[index]);
    editorDialog.makeUndoableModification(selectedElement,
        shape.getElements().indexOf(selectedElement));
  }

  // Deselect all the elements in the shape
  void deselectAll() {
    for (int i = 0; i < shape.getElements().size(); i++) {
      shape.getElements().get(i).deselect();
    }
    selectedElement = null;
    shape.changed();
    repaint();
  }

  // Find which element the user pressed the mouse button inside
  private void checkElements(java.awt.Point start) {

    Element currentElement;

    // the elements are stored in back-to-front order, but we want
    // to favor front elements over back elements here, so we
    // scan the list backwards - SAB/ST 6/11/04
    for (int i = (shape.getElements().size() - 1); i >= 0; i--) {
      currentElement = shape.getElements().get(i);

      if (hasSelectedElement()) {
        int spIndex = checkHandles(start);
        if (spIndex != -1) {
          selectHandle(spIndex, selectedElement);
          return;
        }
      }

      if (currentElement.contains(start)) {
        draggingElement = true;
        if (hasSelectedElement()) {
          selectedElement.deselect();
        }
        currentElement.select();
        selectedElement = currentElement;
        editorDialog.makeUndoableModification
            (selectedElement,
                shape.getElements().indexOf(selectedElement));
        shape.changed();
        repaint();
        return;
      }
    }

    // if clicked on nothing, treat that as a request to deselect all
    deselectAll();
    repaint();
  }

  private void mousePressed(java.awt.event.MouseEvent e) {
    start = last = e.getPoint();

    // if editing is on, find out if one of the shapes was clicked in
    if (editorDialog.editingElements()) {
      checkElements(start);
    }

    // if snap to grid is on, change start to a grid corner
    if (editorDialog.snapToGrid()) {
      snapPointToGrid(start);
    }

    // If we're in the middle of drawing a polygon, don't start
    // over - instead, draw the next side
    if (tempElement instanceof Polygon) {
      ((Polygon) tempElement).addNewPoint(start);
      repaint();
    } else {
      // If this is the first click of a polygon, create the
      // element here, because the user may not drag
      if (editorDialog.getElementType() == Polygon.class) {
        tempElement = createElement(start, start);
        editorDialog.makeUndoableUnfinishedPolygon();
        drawingPolygon = true;
      }
    }
  }

  // Handler for mouse movement when drawing a polygon
  private void mouseMoved(java.awt.event.MouseEvent e) {
    last = e.getPoint();

    if (editorDialog.snapToGrid()) {
      snapPointToGrid(last);
    }

    // Sets the point that is currently last in the polygon to be <last>
    if (tempElement instanceof Polygon) {
      ((Polygon) tempElement).modifyPoint(last);
      repaint();
    }
  }

  private void mouseDragged(java.awt.event.MouseEvent e) {
    previous = last;
    last = e.getPoint();

    if (editorDialog.snapToGrid()) {
      snapPointToGrid(last);
    }

    if (editorDialog.editingElements()) {
      if (draggingHandle) {
        selectedElement.reshapeElement
            (handles[handleIndex], last);
      } else if (draggingElement) {
        selectedElement.moveElement(last.x - previous.x, last.y - previous.y);
      }
      repaint();
    }

    // Don't affect polygons, since their movement is tracked by mouseMoved
    if (editorDialog.getElementType() != Polygon.class) {
      // If the element doesn't exist, create it
      if (tempElement == null) {
        // (NOTE: this will never be true for polygons)
        tempElement = createElement(start, last);
      } else {
        // and modify it for another draw
        tempElement.modify(start, last);
      }
      repaint();
    }
  }

  private void mouseReleased(java.awt.event.MouseEvent e) {
    if (editorDialog.editingElements()) {
      if (draggingHandle) {
        draggingHandle = false;
        shape.changed();
      } else if (draggingElement) {
        draggingElement = false;
        shape.changed();
      }
    }

    if ((!(tempElement instanceof Polygon)) ||
        (e.getClickCount() == 2)) {
      if (tempElement != null) {
        // If it's a polygon, do some extra finishing work
        if (tempElement instanceof Polygon) {
          ((Polygon) tempElement).finishUp();
        }

        // Add the shape, unless it's a polygon that has too few points
        if (!((tempElement instanceof Polygon) &&
            (((Polygon) tempElement).getXcoords().size() < 3))) {
          tempElement.setFilled(editorDialog.fillShapes());
          shape.add(tempElement);
          editorDialog.makeUndoableDraw(tempElement);
          tempElement.select();
          selectedElement = tempElement;
          editorDialog.setEditingElements(true);
          shape.changed();
        }
        drawingPolygon = false;
        tempElement = null;
      }
      start = last = previous = null;
      repaint();
    }
  }

  // Private function to create and return a new shape based on
  // updated coordinates
  private Element createElement(java.awt.Point start, java.awt.Point last) {
    // don't create an element if the arrow tool is the current tool
    if (!editorDialog.editingElements()) {
      if (editorDialog.getElementType() == Line.class) {
        return new Line
            (start, last, editorDialog.getElementColor());
      } else if (editorDialog.getElementType() == Rectangle.class) {
        return new Rectangle
            (start, last, editorDialog.getElementColor());
      } else if (editorDialog.getElementType() == Circle.class) {
        return new Circle
            (start, last, editorDialog.getElementColor());
      }
      // curves are not currently supported - ST 6/11/04
      //case VectorShape.CURVE:
      //    return new Curve
      //        ( start, last, editorDialog.getElementColor() ) ;
      else if (editorDialog.getElementType() == Polygon.class) {
        return new Polygon
            (start, editorDialog.getElementColor());
      }
      // ellipses are not currently supported - ST 6/11/04
      //case VectorShape.ELLIPSE:
      //    return new Ellipse
      //        ( start, last, editorDialog.getElementColor() ) ;
    }
    return null;
  }
}
