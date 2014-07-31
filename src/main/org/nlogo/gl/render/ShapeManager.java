// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render;

import org.nlogo.api.Shape;
import org.nlogo.api.ShapeList;
import org.nlogo.shape.InvalidShapeDescriptionException;
import org.nlogo.shape.LinkShape;
import org.nlogo.shape.VectorShape;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// the use of "@@@" in here is ugly. maybe put the patch shape and
// wireframe code in a diff. class? - ST 3/3/05

class ShapeManager {
  static final int SMOOTHNESS = 16;

  private final GLUquadric quadric;
  private int lastList;
  private final Map<String, GLShape> shapes = new HashMap<String, GLShape>();
  private final List<AddShapeRequest> queue = new LinkedList<AddShapeRequest>();
  private final Map<String, String> shapeMap = new HashMap<String, String>();
  final Map<String, List<String>> customShapes = new HashMap<String, List<String>>();
  private final ShapeList turtleShapeList;
  private final ShapeList linkShapeList;
  private final Map<String, GLShape> modelShapes = new HashMap<String, GLShape>();
  private final Map<String, GLLinkShape> linkShapes = new HashMap<String, GLLinkShape>();
  private final Tessellator tessellator = new Tessellator();
  private final GLUtessellator tess;
  private final boolean is3D;

  ShapeManager(GL2 gl, GLU glu, ShapeList turtleShapeList, ShapeList linkShapeList,
               Map<String, List<String>> customShapes, boolean is3D)

  {
    this.is3D = is3D;
    // tessellation for concave polygons in model shapes
    tess = GLU.gluNewTess();
    GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN_DATA, tessellator);
    GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, tessellator);
    GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, tessellator);
    GLU.gluTessCallback(tess, GLU.GLU_TESS_END_DATA, tessellator);
    GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, tessellator);
    GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, tessellator);
    GLU.gluTessProperty
        (tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);

    quadric = glu.gluNewQuadric();
    glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
    lastList = new Builtins(gl, glu, quadric).add(shapes, shapeMap);
    this.turtleShapeList = turtleShapeList;
    this.linkShapeList = linkShapeList;
    addModelShapes(gl, glu);
    addLinkShapes(gl, glu);
    if (customShapes != null) {
      lastList = CustomShapes.updateShapes(gl, lastList, shapes, customShapes);
    }
  }

  GLShape getShape(String name) {
    if (shapes.containsKey(name)) {
      return shapes.get(name);
    } else if (shapeMap.containsKey(name)) {
      return shapes.get(shapeMap.get(name));
    } else if (modelShapes.containsKey(name)) {
      return modelShapes.get(name);
    } else {
      return shapes.get("default");
    }
  }

  GLLinkShape getLinkShape(String name) {
    return linkShapes.get(name);
  }

  // a model library shape that doesn't have a built-in 3D shape
  boolean modelLibraryShape(String name) {
    return modelShapes.containsKey(name)
        && (!shapes.containsKey(name))
        && (!shapeMap.containsKey(name));
  }

  private static enum AddShapeRequestType {IMPORT, LIBRARY_TURTLE, LIBRARY_LINK}

  private class AddShapeRequest {
    final AddShapeRequestType type;
    final Object data;

    AddShapeRequest(AddShapeRequestType type, Object data) {
      this.type = type;
      this.data = data;
    }
  }

  public void invalidateTurtleShape(String shape) {
    queue.add(new AddShapeRequest(AddShapeRequestType.LIBRARY_TURTLE, shape));
  }

  public void invalidateLinkShape(String shape) {
    queue.add(new AddShapeRequest(AddShapeRequestType.LIBRARY_LINK, shape));
  }

  public void addCustomShapes(String filename)
      throws java.io.IOException,
      InvalidShapeDescriptionException {
    for(CustomShapes.Description shape : CustomShapes.readShapes(filename)) {
      queue.add(new AddShapeRequest(AddShapeRequestType.IMPORT, shape));
    }
  }

  // Need to do it this way because we need the GL
  void checkQueue(GL2 gl, GLU glu) {
    for (AddShapeRequest req : queue) {
      if (req.type == AddShapeRequestType.IMPORT) {
        CustomShapes.Description shape = (CustomShapes.Description) req.data;
        lastList = CustomShapes.addNewShape(gl, shapes, shape);
        customShapes.put(shape.name(), shape.javaLines());
      } else if (req.type == AddShapeRequestType.LIBRARY_TURTLE) {
        List<Shape> modelShapeList = turtleShapeList.getShapes();

        if (modelShapes.containsKey(req.data)) {
          GLShape shape = modelShapes.get(req.data);
          gl.glDeleteLists(shape.displayListIndex(), 1);
          modelShapes.remove(req.data);
        }

        lastList = gl.glGenLists(1);

        // locate new/changed shape in model library
        for (int j = 0; j < modelShapeList.size(); j++) {
          VectorShape vShape =
              (VectorShape) modelShapeList.get(j);
          if (vShape.getName().equals(req.data)) {
            // compile shape
            addModelShape(gl, glu, vShape, lastList);
            break;
          }
        }
      } else if (req.type == AddShapeRequestType.LIBRARY_LINK) {
        if (modelShapes.containsKey(req.data)) {
          GLLinkShape shape = linkShapes.get(req.data);
          gl.glDeleteLists(shape.directionIndicator().displayListIndex(), 1);
          linkShapes.remove(req.data);
        }
        addLinkShape(gl, glu,
            (org.nlogo.shape.LinkShape) linkShapeList.shape((String) req.data),
            gl.glGenLists(1));
      } else {
        throw new IllegalStateException();
      }
    }
    queue.clear();
  }

  private void addLinkShapes(GL2 gl, GLU glu) {
    List<Shape> lols = linkShapeList.getShapes();
    int nextIndex = gl.glGenLists(lols.size());
    Iterator<Shape> iter = lols.iterator();
    for (int i = nextIndex; iter.hasNext(); i++) {
      addLinkShape(gl, glu, (LinkShape) iter.next(), i);
    }
  }

  private void addLinkShape(GL2 gl, GLU glu, LinkShape shape, int index) {
    VectorShape vShape = (VectorShape) shape.getDirectionIndicator();
    GLLinkShape gShape =
      is3D
      ? new GLLinkShape3D(shape, new GLShape(vShape.getName(), index, vShape.isRotatable()))
      : new GLLinkShape  (shape, new GLShape(vShape.getName(), index, vShape.isRotatable()));
    linkShapes.put(shape.getName(), gShape);
    compileShape(gl, glu, vShape, index, vShape.isRotatable());
  }

  private void addModelShapes(GL2 gl, GLU glu) {
    List<Shape> modelShapeList = turtleShapeList.getShapes();
    lastList = gl.glGenLists(modelShapeList.size());

    // compile each shape in the model
    for (int i = 0; i < modelShapeList.size(); i++) {
      VectorShape vShape =
          (VectorShape) modelShapeList.get(i);
      addModelShape(gl, glu, vShape, (lastList + i));
    }
  }

  private void addModelShape(GL2 gl, GLU glu,
                             VectorShape vShape,
                             int index) {
    boolean rotatable = vShape.isRotatable();
    modelShapes.put(vShape.getName(),
        new GLShape(vShape.getName(), index, rotatable));

    compileShape(gl, glu, vShape, index, rotatable);
  }

  void compileShape(GL2 gl, GLU glu,
                    VectorShape vShape,
                    int index, boolean rotatable) {
    gl.glNewList(index, GL2.GL_COMPILE);

    if (!rotatable) {
      gl.glDisable(GLLightingFunc.GL_LIGHTING);
    }

    // render each element in this shape
    List<org.nlogo.shape.Element> elements = vShape.getElements();
    for (int i = 0; i < elements.size(); i++) {
      org.nlogo.shape.Element element = elements.get(i);

      if (element instanceof org.nlogo.shape.Rectangle) {
        renderRectangle
            (gl, i,
                (org.nlogo.shape.Rectangle) element, rotatable);
      } else if (element instanceof org.nlogo.shape.Polygon) {
        Polygons.renderPolygon(gl, glu, tessellator, tess, i,
          (org.nlogo.shape.Polygon) element, rotatable, is3D);
      } else if (element instanceof org.nlogo.shape.Circle) {
        renderCircle(gl, glu, i,
            (org.nlogo.shape.Circle) element, rotatable);
      } else if (element instanceof org.nlogo.shape.Line) {
        renderLine(gl, i,
            (org.nlogo.shape.Line) element);
      } else if (element instanceof org.nlogo.shape.Curve) {
        throw new IllegalStateException();
      }
    }

    if (!rotatable) {
      gl.glEnable(GLLightingFunc.GL_LIGHTING);
    }

    gl.glEndList();
  }

  private void renderRectangle(GL2 gl, int offset,
                               org.nlogo.shape.Rectangle rect,
                               boolean rotatable) {
    float zDepth = 0.01f + offset * 0.0001f;

    if (!rect.marked()) {
      float[] rgb = rect.getColor().getRGBColorComponents(null);
      gl.glPushAttrib(GL2.GL_CURRENT_BIT);
      gl.glColor3fv(java.nio.FloatBuffer.wrap(rgb));
    }

    java.awt.Point[] corners = rect.getCorners();

    float coords[] =
        {corners[0].x * .001f - 0.15f,
            (300 - corners[0].y) * .001f - 0.15f,
            corners[1].x * .001f - 0.15f,
            (300 - corners[1].y) * .001f - 0.15f};

    // no need to "pancake" if it is always facing the user
    renderRectangle
        (gl, coords[0], coords[2], coords[3], coords[1],
            -zDepth, zDepth, !rect.filled(), rotatable);

    if (!rect.marked()) {
      gl.glPopAttrib();
    }
  }

  void renderRectangle(GL2 gl, float x0, float x1, float y0, float y1,
                       float z0, float z1, boolean filled, boolean rotatable) {
    Rectangle.renderRectangularPrism(gl, x0, x1, y0, y1,
        z0, z1, filled, is3D && rotatable, rotatable);
  }

  private void renderCircle(GL2 gl, GLU glu, int offset,
                            org.nlogo.shape.Circle circle,
                            boolean rotatable) {
    float zDepth = 0.01f + offset * 0.0001f;

    if (!circle.marked()) {
      float[] rgb = circle.getColor().getRGBColorComponents(null);
      gl.glPushAttrib(GL2.GL_CURRENT_BIT);
      gl.glColor3fv(java.nio.FloatBuffer.wrap(rgb));
    }

    // for now assume it is a circle

    float radius = (float) circle.getBounds().getWidth() * .0005f;
    float origin[] =
        {(float) circle.getOrigin().getX() * .001f - 0.15f,
            (300 - (float) circle.getOrigin().getY()) * .001f - 0.15f};

    gl.glPushMatrix();

    if (!circle.filled()) {
      glu.gluQuadricDrawStyle(quadric, GLU.GLU_SILHOUETTE);
    }

    // no need to "pancake" if it is always facing the user
    if (rotatable) {
      if (!circle.filled()) {
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
        gl.glDisable(GL.GL_CULL_FACE);
      }

      gl.glTranslatef(origin[0], origin[1], -zDepth);
      glu.gluCylinder(quadric, radius, radius, (2 * zDepth),
          SMOOTHNESS, 1);

      if (!circle.filled()) {
        gl.glEnable(GL.GL_CULL_FACE);
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_SILHOUETTE);
      }

      gl.glTranslatef(0.0f, 0.0f, (2 * zDepth));
    } else {
      gl.glTranslatef(origin[0], origin[1], zDepth);
    }

    renderCircle(gl, glu, 0.0f, radius, zDepth, rotatable);

    if (!circle.filled()) {
      glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
    }

    gl.glPopMatrix();

    if (!circle.marked()) {
      gl.glPopAttrib();
    }
  }

  private void renderCircle(GL2 gl, GLU glu,
                            float innerRadius, float outerRadius, float zDepth,
                            boolean rotatable) {
    glu.gluDisk(quadric, innerRadius, outerRadius, SMOOTHNESS, 1);
    if(is3D && rotatable) {
      gl.glRotatef(180f, 1f, 0f, 0f);
      gl.glTranslatef(0f, 0f, zDepth * 2);
      glu.gluDisk(quadric, innerRadius, outerRadius, SMOOTHNESS, 1);
    }
  }

  private void renderLine(GL2 gl, int offset, org.nlogo.shape.Line line) {
    float zDepth = offset * 0.0001f;

    if (!line.marked()) {
      float[] rgb = line.getColor().getRGBColorComponents(null);
      gl.glPushAttrib(GL2.GL_CURRENT_BIT);
      gl.glColor3fv(java.nio.FloatBuffer.wrap(rgb));
    }

    java.awt.Point start = line.getStart();
    java.awt.Point end = line.getEnd();
    float coords[] =
        {start.x * .001f - 0.15f, (300 - start.y) * .001f - 0.15f,
            end.x * .001f - 0.15f, (300 - end.y) * .001f - 0.15f};

    gl.glBegin(GL.GL_LINES);
    gl.glNormal3f(0.0f, 0.0f, -1.0f);

    // top line
    gl.glVertex3f(coords[0], coords[1], zDepth);
    gl.glVertex3f(coords[2], coords[3], zDepth);
    gl.glEnd();

    // we don't "pancake" line because we don't really need to,
    // by default the line tilts to always show the user
    // a flat face, and this way we can scale all lines
    // by line thickness and it will look good and it will be fast. ev 4/5/06

    if (!line.marked()) {
      gl.glPopAttrib();
    }
  }

}
