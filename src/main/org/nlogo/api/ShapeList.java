package org.nlogo.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public strictfp class ShapeList {

  public static final String DEFAULT_SHAPE_NAME = "default";

  public static boolean isDefaultShapeName(String name) {
    return name.equals(DEFAULT_SHAPE_NAME);
  }

  ///

  private final Map<String, Shape> shapes = new HashMap<String, Shape>();

  public ShapeList(Shape... shapes) {
    for (Shape shape : shapes) {
      add(shape);
    }
  }

  public Shape shape(String name) {
    Shape shape = shapes.get(name);
    return shape == null
        ? shapes.get(DEFAULT_SHAPE_NAME)
        : shape;
  }

  // Returns vector of the list of shapes available to the current model
  public List<Shape> getShapes() {
    List<Shape> currentShapes = new ArrayList<Shape>();
    for (Shape nextShape : shapes.values()) {
      // don't add the default shape here, we will add it later so that it
      // is at the top of the list
      if (!isDefaultShapeName(nextShape.getName())) {
        currentShapes.add(nextShape);
      }
    }
    List<Shape> sortedShapes = sortShapes(currentShapes);
    // make sure that the shape with the name DEFAULT_SHAPE_NAME is at the
    // top of the list.
    sortedShapes.add(0, shapes.get(DEFAULT_SHAPE_NAME));
    return sortedShapes;
  }

  public static final List<Shape> sortShapes(List<Shape> unsortedShapes) {
    List<Shape> result = new ArrayList<Shape>(unsortedShapes);
    Collections.sort
        (result,
            new Comparator<Shape>() {
              // compare two instances of shapemodel so we can return a
              // sorted shape list
              public int compare(Shape shape1, Shape shape2) {
                return shape1.getName().compareTo(shape2.getName());
              }
            });
    return result;
  }

  // Returns a set of the names of all available shapes
  public Set<String> getNames() {
    return shapes.keySet();
  }

  // Returns true when a shape with the given name is already available to the current model
  public boolean exists(String name) {
    return shapes.containsKey(name);
  }

  // Clears the list of shapes currently available
  public void replaceShapes(Collection<Shape> newShapes) {
    shapes.clear();
    addAll(newShapes);
  }

  // Adds a new shape to the ones currently available for use
  public Shape add(Shape newShape) {
    return shapes.put(newShape.getName(), newShape);
  }

  // Adds a collection of shapes to the ones currently available for use
  public void addAll(Collection<Shape> collection) {
    for (Shape shape : collection) {
      add(shape);
    }
  }

  // Removes a shape from those currently in use
  public Shape removeShape(Shape shapeToRemove) {
    return shapes.remove(shapeToRemove.getName());
  }

}
