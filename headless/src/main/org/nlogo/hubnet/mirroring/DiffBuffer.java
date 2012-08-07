// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import java.util.ArrayList;
import java.util.List;

public strictfp class DiffBuffer {
  public static final short EMPTY = 0x0000;

  public static final short MINX = 0x0001;
  public static final short MINY = 0x0002;
  public static final short SHAPES = 0x0004;
  public static final short FONT_SIZE = 0x0008;
  public static final short MAXX = 0x0010;
  public static final short MAXY = 0x0020;
  public static final short TURTLES = 0x0040;
  public static final short PATCHES = 0x0080;
  public static final short LINKS = 0x0100;
  public static final short DRAWING = 0x0200;
  public static final short WRAPX = 0x0400;
  public static final short WRAPY = 0x0800;
  public static final short PERSPECTIVE = 0x1000;

  public static final short EVERYTHING = MINX | MINY | MAXX | MAXY
      | SHAPES | FONT_SIZE | TURTLES | PATCHES | LINKS
      | WRAPX | WRAPY | PERSPECTIVE;

  private short mask;
  private int minPxcor;
  private int minPycor;
  private int maxPxcor;
  private int maxPycor;
  private int fontSize;
  private boolean xWrap;
  private boolean yWrap;
  private boolean shapes;
  private AgentPerspective perspective;

  private java.awt.image.BufferedImage drawing;

  private final List<PatchData> patchDiffs;
  private final List<TurtleData> turtleDiffs;
  private final List<LinkData> linkDiffs;

  public DiffBuffer() {
    mask = EMPTY;
    patchDiffs = new ArrayList<PatchData>();
    turtleDiffs = new ArrayList<TurtleData>();
    linkDiffs = new ArrayList<LinkData>();
  }

  public boolean isEmpty() {
    return mask == EMPTY;
  }

  void addMinX(int minPxcor) {
    mask |= MINX;
    this.minPxcor = minPxcor;
  }

  void addMinY(int minPycor) {
    mask |= MINY;
    this.minPycor = minPycor;
  }

  void addMaxX(int maxPxcor) {
    mask |= MAXX;
    this.maxPxcor = maxPxcor;
  }

  void addMaxY(int maxPycor) {
    mask |= MAXY;
    this.maxPycor = maxPycor;
  }

  void addWrapX(boolean xWrap) {
    mask |= WRAPX;
    this.xWrap = xWrap;
  }

  void addWrapY(boolean yWrap) {
    mask |= WRAPY;
    this.yWrap = yWrap;
  }

  void addShapes(boolean shapes) {
    mask |= SHAPES;
    this.shapes = shapes;
  }

  void addFontSize(int fontSize) {
    mask |= FONT_SIZE;
    this.fontSize = fontSize;
  }

  void addTurtle(TurtleData diffs) {
    mask |= TURTLES;
    turtleDiffs.add(diffs);
  }

  void addLink(LinkData diffs) {
    mask |= LINKS;
    linkDiffs.add(diffs);
  }

  void addPatch(PatchData diffs) {
    mask |= PATCHES;
    patchDiffs.add(diffs);
  }

  void addDrawing(java.awt.image.BufferedImage drawing) {
    mask |= DRAWING;
    this.drawing = drawing;
  }

  void addPerspective(AgentPerspective perspective) {
    mask |= PERSPECTIVE;
    this.perspective = perspective;
  }

  public void serialize(java.io.DataOutputStream os)
      throws java.io.IOException {
    os.writeShort(mask);
    if ((mask & MINX) == MINX) {
      os.writeInt(minPxcor);
    }
    if ((mask & MINY) == MINY) {
      os.writeInt(minPycor);
    }
    if ((mask & MAXX) == MAXX) {
      os.writeInt(maxPxcor);
    }
    if ((mask & MAXY) == MAXY) {
      os.writeInt(maxPycor);
    }
    if ((mask & SHAPES) == SHAPES) {
      os.writeBoolean(shapes);
    }
    if ((mask & FONT_SIZE) == FONT_SIZE) {
      os.writeInt(fontSize);
    }
    if ((mask & WRAPX) == WRAPX) {
      os.writeBoolean(xWrap);
    }
    if ((mask & WRAPY) == WRAPY) {
      os.writeBoolean(yWrap);
    }
    if ((mask & PERSPECTIVE) == PERSPECTIVE) {
      perspective.serialize(os);
    }
    if ((mask & PATCHES) == PATCHES) {
      os.writeInt(patchDiffs.size());
      for (PatchData patch : patchDiffs) {
        patch.serialize(os);
      }
    }
    if ((mask & TURTLES) == TURTLES) {
      os.writeInt(turtleDiffs.size());
      for (TurtleData turtle : turtleDiffs) {
        turtle.serialize(os);
      }
    }
    if ((mask & LINKS) == LINKS) {
      os.writeInt(linkDiffs.size());
      for (LinkData link : linkDiffs) {
        link.serialize(os);
      }
    }
    if ((mask & DRAWING) == DRAWING) {
      javax.imageio.ImageIO.write(drawing, "PNG", os);
    }
  }

  public byte[] toByteArray() {
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    try {
      serialize(new java.io.DataOutputStream(bos));
    } catch (java.io.IOException e) {
      // shouldn't happen, since we're writing to a byte array...
      throw new IllegalStateException(e);
    }
    // will be empty if an exception occurred, which is what we want...
    return bos.toByteArray();
  }
}
