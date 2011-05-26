package org.nlogo.gl.render;

import java.util.List;
import java.util.Map;
import javax.media.opengl.GL;
import org.nlogo.shape.InvalidShapeDescriptionException;

class Shapes {

  // this class is not instantiable
  private Shapes() {
    throw new IllegalStateException();
  }

  static int addNewShape(GL gl, Map<String, GLShape> shapes, Map<String, List<String>> customShapes,
                         CustomShapeDescription shape) {
    int lastList = gl.glGenLists(1);
    shapes.put(shape.name(), new GLShape(shape.name(), lastList));
    gl.glNewList(lastList, GL.GL_COMPILE);
    for (String next : shape.lines()) {
      if (next.equals("tris")) {
        gl.glBegin(GL.GL_TRIANGLES);
      } else if (next.equals("quads")) {
        gl.glBegin(GL.GL_QUADS);
      } else if (next.equals("stop")) {
        gl.glEnd();
      } else if (next.startsWith("normal: ")) {
        String[] floats = next.substring(8).split(" ");
        gl.glNormal3f(Float.parseFloat(floats[0]),
            Float.parseFloat(floats[1]),
            Float.parseFloat(floats[2]));
      } else {
        String[] floats = next.split(" ");
        gl.glVertex3f(Float.parseFloat(floats[0]),
            Float.parseFloat(floats[1]),
            Float.parseFloat(floats[2]));
      }
    }
    gl.glEndList();
    customShapes.put(shape.name(), shape.lines());
    return lastList;
  }

  static boolean isVertex(String line)
      throws InvalidShapeDescriptionException {
    String[] floats = line.split(" ");
    if (floats.length != 3) {
      throw new InvalidShapeDescriptionException();
    }
    try {
      Float.parseFloat(floats[0]);
      Float.parseFloat(floats[1]);
      Float.parseFloat(floats[2]);
    } catch (NumberFormatException e) {
      throw new InvalidShapeDescriptionException();
    }
    return true;
  }

  static int updateShapes(GL gl, int lastList, Map<String, GLShape> shapes,
                                  Map<String, List<String>> customShapes) {
    for (String shapeName : customShapes.keySet()) {
      lastList++;
      shapes.put(shapeName, new GLShape(shapeName, lastList));
      List<String> lines = customShapes.get(shapeName);
      customShapes.put(shapeName, lines);
      gl.glNewList(lastList, GL.GL_COMPILE);
      for (String next : lines) {
        if (next.equals("tris")) {
          gl.glBegin(GL.GL_TRIANGLES);
        } else if (next.equals("quads")) {
          gl.glBegin(GL.GL_QUADS);
        } else if (next.equals("stop")) {
          gl.glEnd();
        } else if (next.startsWith("normal: ")) {
          String[] floats = next.substring(8).split(" ");
          gl.glNormal3f(Float.parseFloat(floats[0]),
              Float.parseFloat(floats[1]),
              Float.parseFloat(floats[2]));
        } else {
          String[] floats = next.split(" ");
          gl.glVertex3f(Float.parseFloat(floats[0]),
              Float.parseFloat(floats[1]),
              Float.parseFloat(floats[2]));
        }
      }
      gl.glEndList();
    }
    return lastList;
  }

  static java.util.List<CustomShapeDescription> readShapes(String filename)
      throws java.io.IOException,
      InvalidShapeDescriptionException {
    java.io.File shapeFile = new java.io.File(filename);
    java.io.BufferedReader shapeReader =
        new java.io.BufferedReader(new java.io.FileReader(shapeFile));
    String line = shapeReader.readLine();
    int shapeCount = Integer.parseInt(line);
    java.util.List<CustomShapeDescription> result =
      new java.util.ArrayList<CustomShapeDescription>() ;
    for (int i = 0; i < shapeCount; i++) {
      String shapeName = shapeReader.readLine();
      String next = shapeReader.readLine();
      CustomShapeDescription shape = new CustomShapeDescription(shapeName);
      while (!next.equals("end-shape")) {
        if (next.equals("tris") ||
            next.equals("quads") ||
            next.equals("stop") ||
            next.startsWith("normal:") ||
            Shapes.isVertex(next)) {
          shape.lines().add(next);
        } else {
          throw new InvalidShapeDescriptionException();
        }
        next = shapeReader.readLine();
      }
      result.add(shape);
    }
    return result;
  }

}
