// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import static org.nlogo.shape.Element.SHAPE_WIDTH;

public strictfp class VectorShape
    extends Observable
    implements org.nlogo.api.Shape, Cloneable, java.io.Serializable, DrawableShape {
  static final long serialVersionUID = 0L;
  protected String name = "";

  // Accessors for the name of the model
  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected int editableColorIndex;


  /// data members

  protected List<Element> elementList =
      new ArrayList<Element>();    // The list of elements currently contained in the model
  protected boolean rotatable = true;

  // Accessors for the index of the model's editable color
  public void setEditableColorIndex(int editableColorIndex) {
    this.editableColorIndex = editableColorIndex;
  }

  public int getEditableColorIndex() {
    return editableColorIndex;
  }

  ///

  @Override
  public Object clone() {
    VectorShape newShape;
    try {
      newShape = (VectorShape) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException(ex);
    }
    newShape.elementList = new ArrayList<Element>();
    for (Element e : elementList) {
      newShape.elementList.add((Element) (e.clone()));
    }
    return newShape;
  }

  public void setOutline() {
    for (Element e : elementList) {
      e.setFilled(false);
    }
  }

  // Returns the vector containing all the elements of the model
  public List<Element> getElements() {
    return elementList;
  }

  // Accessors for rotation
  public void setRotatable(boolean rotatable) {
    this.rotatable = rotatable;
  }

  public boolean isRotatable() {
    return rotatable;
  }

  private enum Recolorable {UNKNOWN, TRUE, FALSE}

  private Recolorable fgRecolorable = Recolorable.UNKNOWN;

  public boolean fgRecolorable() {
    if (fgRecolorable == Recolorable.UNKNOWN) {
      fgRecolorable = Recolorable.FALSE;

      java.awt.Color editableColor =
          new java.awt.Color(org.nlogo.api.Color.getARGBByIndex
              (editableColorIndex));
      int n = elementList.size();
      for (int i = 0; i < n; i++) {
        if (element(i).getColor().equals(editableColor)) {
          fgRecolorable = Recolorable.TRUE;
          break;
        }
      }
    }

    return (fgRecolorable == Recolorable.TRUE);
  }

  // This is called after a shape is done being drawn/edited.  It marks all the elements that were drawn
  // in the editable color selected by the user, so that when the turtle's color is set in netlogo, only
  // those elements are recolored
  public void markRecolorableElements(java.awt.Color editableColor, int editableColorIndex) {
    this.editableColorIndex = editableColorIndex;   // Remember the color for the next time this shape is edited
    fgRecolorable = Recolorable.FALSE;
    for (int i = 0; i < elementList.size(); ++i) {
      if (element(i).getColor().equals(editableColor))  // If an element was drawn in the special color,
      {
        element(i).setMarked(true);          // mark it, otherwise, unmark it
        fgRecolorable = Recolorable.TRUE;
      } else {
        element(i).setMarked(false);
      }
    }
  }

  public boolean isTooSimpleToCache() {
    switch (elementList.size()) {
      case 0:
        return true;
      case 1:
        Element element = element(0);
        return !(element instanceof Polygon);
      default:
        return false;
    }
  }

  // Clean accessor to the list of elements
  protected Element element(int i) {
    return elementList.get(i);
  }


  // Removes the specified element from the model
  public void remove(Element element) {
    boolean removed = elementList.remove(element);
    if (removed) {
      setChanged();
      notifyObservers();
    }
  }

  // Allows other classes to make a shape dirty, so it will be redrawn (used in ShapesEditor.init(shape))
  public void changed() {
    setChanged();
    notifyObservers();
  }


  // Removes the last element added to the model
  public void removeLast() {
    if (!elementList.isEmpty()) {
      int lastIndex = elementList.size() - 1;
      elementList.remove(lastIndex);
      setChanged();
      notifyObservers();        //(elementRemoved.getBounds());
    }
  }


  // Removes all elements from the model
  public void removeAll() {
    if (!elementList.isEmpty()) {
      elementList.clear();
      setChanged();
      notifyObservers();
    }
  }


  // Add a new element to the model
  // Adds at the end of the list
  public void add(Element element) {
    elementList.add(element);

    // Rectangles' max and min coords must be set once the rectangle is no longer being modified
    if (element instanceof Rectangle) {
      ((Rectangle) element).setMaxsAndMins();
    }

    setChanged();
    notifyObservers();
  }

  // Add an element at an arbitrary spot in the shape
  // This is used when changing the z-ordering of the elements in a shape
  public void addAtPosition(int index, Element element) {
    elementList.add(index, element);
    setChanged();
    notifyObservers();
  }

  public void rotateLeft() {
    for (int i = 0; i < elementList.size(); ++i)        // For each element...
    {
      element(i).rotateLeft();
    }
  }

  public void rotateRight() {
    for (int i = 0; i < elementList.size(); ++i)        // For each element...
    {
      element(i).rotateRight();
    }
  }

  public void flipHorizontal() {
    for (int i = 0; i < elementList.size(); ++i)        // For each element...
    {
      element(i).flipHorizontal();
    }
  }

  public void flipVertical() {
    for (int i = 0; i < elementList.size(); ++i)        // For each element...
    {
      element(i).flipVertical();
    }
  }

  // Method called by TurtleDrawer to paint this shape on a patch in
  // the graphics window
  public void paint(GraphicsInterface g, java.awt.Color turtleColor,
                    double x, double y, double size, double cellSize, int angle,
                    double lineThickness) {
    g.push();
    // Mac and non-Mac VM's draw circles and rectangles differently;
    // on Macs everything is a pixel bigger.  so on Mac if we've
    // been asked to fill a 9x9 pixel area, say, what we actually do is draw
    // within an 8x8 area, and the extra row and column will get filled
    // because everything is a pixel bigger than it should be.
    // - ST 8/15/05
    double scale = size * cellSize;
    if (!isRotatable()) {
      angle = 0;
    }
    try {
      if (angle != 0) {
        g.rotate(angle / 180.0 * StrictMath.PI, x, y, scale);
      }
      g.translate(x, y);
      g.scale(scale, scale, SHAPE_WIDTH);
      g.setStrokeFromLineThickness(lineThickness, scale, cellSize, SHAPE_WIDTH);
      for (int i = 0; i < elementList.size(); i++) {
        // we've already handled scaling & rotation ourselves,
        // but we need to let some element types know how much
        // we scaled and rotated in order for them to adjust
        // their size and/or position slightly in order for
        // the results to be consistent cross-platform
        // - ST 8/25/05
        element(i).draw(g, turtleColor, scale, angle);
      }
    } finally {
      g.pop();
    }
  }

  public void paint(GraphicsInterface g, java.awt.Color turtleColor,
                    int x, int y, double cellSize, int angle) {
    paint(g, turtleColor, x, y, 1, cellSize, angle, 0.0f);
  }

  // Returns a readable serialization of the model, which should only be used for debugging purposes
  public String toReadableString() {
    String ret = "Shape " + name + ":\n";
    for (int i = 0; i < elementList.size(); ++i) {
      ret += elementList.get(i).toString();
    }

    return ret;
  }


  // Returns a serialization of the model, which can be saved in a model file
  @Override
  public String toString() {
    String ret = name + "\n" + rotatable + "\n" + editableColorIndex;
    for (int i = 0; i < elementList.size(); ++i) {
      Element elt = elementList.get(i);
      if (elt.shouldSave()) {
        ret += "\n" + elt.toString();
      }
    }
    return ret;
  }

  public void addElement(String line) {
    org.nlogo.shape.Element element = null;
    if (line.startsWith("Line"))        // See what shape it is, and parse it accordingly
    {
      element = Line.parseLine(line);
    } else if (line.startsWith("Rectangle")) {
      element = Rectangle.parseRectangle(line);
    } else if (line.startsWith("Circle")) {
      element = Circle.parseCircle(line);
    } else if (line.startsWith("Polygon")) {
      element = Polygon.parsePolygon(line);
    }
    // ellipses aren't supported right now - SAB/ST 6/11/04
    //else if( line.startsWith( "Ellipse" ) )
    //{
    //  element = Ellipse.parseEllipse( line ) ;
    //}
    else {
      throw new IllegalStateException
          ("Invalid shape format in file: " + line);
    }
    if (element != null) {
      add(element);
    }
  }

  ///

  public static List<org.nlogo.api.Shape> parseShapes(String[] shapes, String version) {
    int index = 0;
    List<org.nlogo.api.Shape> ret =
        new ArrayList<org.nlogo.api.Shape>();
    VectorShape shape;

    // Skip initial blank lines, if any
    while ((shapes.length > index) &&
        (0 == getString(shapes, index).length())) {
      index++;
    }

    // Go through the lines of text, reading in shapes
    while (shapes.length > index) {
      try {
        shape = new VectorShape();
        index = parseShape(shapes, version, shape, index);
        ret.add(shape);    // Add the shape to the return vector
      } catch (IllegalStateException e) {
        continue;
      }
      index++;          // Skip the blank line we're on before looking for the next shape
    }
    return ret;
  }

  public static int parseShape(String[] shapes, String version, VectorShape shape, int index) {
    // Read in the name and rotatability of a shape
    shape.setName(getString(shapes, index++));

    if (shape.getName().indexOf("StarLogoT") != -1) {
      // oops, it's not really shapes, it's the version line of a
      // StarLogoT model... so ignore it
      throw new IllegalStateException("found StarLogoT version instead of shape");
    }

    shape.setRotatable(getString(shapes, index++).equals("true"));
    int rgb = Integer.valueOf(getString(shapes, index++)).intValue();
    shape.setEditableColorIndex(rgb);
    // Read in the elements of that shape
    while (0 != getString(shapes, index).length()) {
      shape.addElement
          (getString(shapes, index++));
    }

    return index;
  }

  static String getString(String[] v, int index) {
    if ((null != v) && (v.length > index)) {
      return v[index];
    }
    return "";
  }

  ///

  // ugly that this is hardcoded here instead of being pulled
  // out of defaultShapes.txt - ST 6/27/05
  public static VectorShape getDefaultShape() {
    VectorShape result = new org.nlogo.shape.VectorShape();
    result.setName(org.nlogo.api.ShapeList.DefaultShapeName());
    result.setRotatable(true);
    result.setEditableColorIndex(0);
    result.addElement
        ("Polygon -7500403 true true 150 5 40 250 150 205 260 250");
    return result;
  }

  /// constants

  // Initial conditions
  public static final int TURTLE_WIDTH = 25;

  // Measure of how close two points need to be to close a polygon
  public static final int CLOSE_ENOUGH = 10;

  public static final int NUM_GRID_LINES = 20;

}
