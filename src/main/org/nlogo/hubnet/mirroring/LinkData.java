// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Describes a link.
 */
public strictfp class LinkData
    extends AgentData
    implements org.nlogo.api.Link {
  public static final short DEAD = 0x0000;
  public static final short ENDS = 0x0001;
  public static final short X1 = 0x0002;
  public static final short Y1 = 0x0004;
  public static final short X2 = 0x0008;
  public static final short Y2 = 0x0010;
  public static final short SHAPE = 0x0020;
  public static final short COLOR = 0x0040;
  public static final short HIDDEN = 0x0080;
  public static final short LABEL = 0x0100;
  public static final short LABEL_COLOR = 0x0200;
  public static final short LINE_THICKNESS = 0x0400;
  public static final short DESTINATION_SIZE = 0x0800;
  public static final short HEADING = 0x1000;
  public static final short SIZE = 0x2000;
  public static final short BREED = 0x4000;

  public static final short COMPLETE =
      ENDS | X1 | Y1 | X2 | Y2 | SHAPE | COLOR
          | HIDDEN | LABEL | LABEL_COLOR | LINE_THICKNESS
          | DESTINATION_SIZE | HEADING | SIZE | BREED;

  long id;

  public long id() {
    return id;
  }

  long end1;
  long end2;
  private short mask;
  private double x1;
  private double y1;
  private double x2;
  private double y2;
  private String shape = "";
  private LogoList color;
  private boolean hidden;
  private String label = "";
  private LogoList labelColor;
  private double lineThickness;
  private boolean directedLink;
  private double destSize;
  private double heading;
  private double size;
  private int breedIndex;

  public ClientWorld.LinkKey getKey() {
    return new ClientWorld.LinkKey(id, end1, end2, breedIndex);
  }

  @Override
  public String toString() {
    return "link " + id + " " + breedIndex;
  }

  public LinkData(HubNetLinkStamp link) {
    mask = COMPLETE;
    x1 = link.x1;
    y1 = link.y1;
    x2 = link.x2;
    y2 = link.y2;
    shape = link.shape;
    color(link.color);
    hidden = link.hidden;
    lineThickness = link.lineThickness;
    directedLink = link.directedLink;
    destSize = link.destSize;
    heading = link.heading;
    size = link.size;
  }

  /**
   * creates a new LinkData representing a link that has died.
   */
  LinkData(long id) {
    this.id = id;
    mask = DEAD;
    color = LogoList.Empty();
    labelColor = LogoList.Empty();
  }

  LinkData(long id, long end1, long end2, short mask,
           double x1, double y1, double x2, double y2,
           String shape, Object color, boolean hidden,
           String label, Object labelColor, double lineThickness, boolean directedLink,
           double destSize, double heading, double size, int breedIndex) {
    this.id = id;
    this.end1 = end1;
    this.end2 = end2;
    this.mask = mask;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.shape = shape;
    color(color);
    this.hidden = hidden;
    this.label = label;
    labelColor(labelColor);
    this.lineThickness = lineThickness;
    this.directedLink = directedLink;
    this.destSize = destSize;
    this.heading = heading;
    this.size = size;
    this.breedIndex = breedIndex;
  }

  LinkData(DataInputStream is)
      throws IOException {
    id = is.readLong();
    mask = is.readShort();
    if ((mask & ENDS) == ENDS) {
      end1 = is.readLong();
      end2 = is.readLong();
    }
    if ((mask & X1) == X1) {
      x1 = is.readDouble();
    }
    if ((mask & Y1) == Y1) {
      y1 = is.readDouble();
    }
    if ((mask & X2) == X2) {
      x2 = is.readDouble();
    }
    if ((mask & Y2) == Y2) {
      y2 = is.readDouble();
    }
    if ((mask & SHAPE) == SHAPE) {
      shape = is.readUTF();
    }
    if ((mask & COLOR) == COLOR) {
      color(is.readInt());
    }
    if ((mask & HIDDEN) == HIDDEN) {
      hidden = is.readBoolean();
    }
    if ((mask & LABEL) == LABEL) {
      label = is.readUTF();
    }
    if ((mask & LABEL_COLOR) == LABEL_COLOR) {
      labelColor(is.readInt());
    }
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS) {
      lineThickness = is.readDouble();
    }
    if ((mask & DESTINATION_SIZE) == DESTINATION_SIZE) {
      destSize = is.readDouble();
    }
    if ((mask & HEADING) == HEADING) {
      heading = is.readDouble();
    }
    if ((mask & SIZE) == SIZE) {
      size = is.readDouble();
    }
    if ((mask & BREED) == BREED) {
      breedIndex = is.readInt();
      directedLink = is.readBoolean();
    }
  }

  public int getBreedIndex() {
    return breedIndex;
  }

  public double x1() {
    return x1;
  }

  public double y1() {
    return y1;
  }

  public double x2() {
    return x2;
  }

  public double y2() {
    return y2;
  }

  public Object color() {
    return color;
  }

  public void color(Object color) {
    if (color instanceof Double) {
      color((Double) color);
    } else {
      color((LogoList) color);
    }
  }

  public void color(Double color) {
    color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(color));
  }

  public void color(org.nlogo.api.LogoList color) {
    this.color = color;
  }

  public void color(int argb) {
    this.color = org.nlogo.api.Color.getRGBAListByARGB(argb);
  }

  public String shape() {
    return shape;
  }

  public void shape(String shape) {
    this.shape = shape;
  }

  public boolean hidden() {
    return hidden;
  }

  public void hidden(Boolean hidden) {
    this.hidden = hidden.booleanValue();
  }

  public String label() {
    return label;
  }

  public void label(Object label) {
    this.label = Dump.logoObject(label);
  }

  public String labelString() {
    return label;
  }

  public Object labelColor() {
    return labelColor;
  }

  public void labelColor(Object labelColor) {
    if (labelColor instanceof Double) {
      labelColor((Double) labelColor);
    } else {
      labelColor((LogoList) labelColor);
    }
  }

  public void labelColor(Double labelColor) {
    labelColor(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(labelColor));
  }

  public void labelColor(org.nlogo.api.LogoList labelColor) {
    this.labelColor = labelColor;
  }

  public void labelColor(int argb) {
    this.labelColor = org.nlogo.api.Color.getRGBAListByARGB(argb);
  }

  public boolean hasLabel() {
    return (label != null && label.length() > 0);
  }

  public double lineThickness() {
    return lineThickness;
  }

  public void lineThickness(Double lineThickness) {
    this.lineThickness = lineThickness.doubleValue();
  }

  public double heading() {
    return heading;
  }

  @Override
  public double xcor() {
    return midpointX();
  }

  @Override
  public double ycor() {
    return midpointY();
  }

  @Override
  public double spotlightSize() {
    return 1;
  }

  @Override
  public boolean wrapSpotlight() {
    return false;
  }

  private static final String[] OVERRIDE_VARIABLES = new String[]
      {"COLOR", "LABEL", "LABEL-COLOR", "LINE-THICKNESS", "HIDDEN?", "SHAPE"};
  private static final String[] OVERRIDE_METHODS = new String[]
      {"color", "label", "labelColor", "lineThickness", "hidden", "shape"};

  @Override
  public String getMethodName(int index) {
    return OVERRIDE_METHODS[index];
  }

  public static int getOverrideIndex(String varName) {
    return getOverrideIndex(OVERRIDE_VARIABLES, varName);
  }

  public double midpointX() {
    return (x1 + x2) / 2;
  }

  public double midpointY() {
    return (y1 + y2) / 2;
  }

  // these are only used for 3D which we don't do in hubnet
  // however, I should clean this up eventually ev 12/14/06
  public org.nlogo.api.Turtle end2() {
    return null;
  }

  public org.nlogo.api.Turtle end1() {
    return null;
  }

  public boolean isDirectedLink() {
    return directedLink;
  }

  public double linkDestinationSize() {
    return destSize;
  }

  public double size() {
    return size;
  }

  public org.nlogo.api.AgentSet getBreed() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.World world() {
    throw new UnsupportedOperationException();
  }

  public String stringRep() {
    if (mask == COMPLETE) {
      return "Link " + id + " (" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", "
          + shape + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(color) + ", " + ", " + hidden + ", "
          + label + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(labelColor) + ", " + lineThickness + ")";
    }
    if (mask == DEAD) {
      return "Link " + id + " (dead)";
    }
    return "Link " + id + " update (mask " + Integer.toBinaryString(mask) + ")";
  }

  boolean isComplete() {
    return (mask & COMPLETE) == COMPLETE;
  }

  boolean isDead() {
    return mask == DEAD;
  }

  /**
   * updates this LinkData to include any changes specified by other.
   * Returns a new LinkData representing only items which actually
   * differed between the two. If no changes are required, returns null.
   */
  LinkData updateFrom(LinkData other) {
    // this is just a sanity check, and could be removed without changing
    // the behavior.
    if (other.isDead()) {
      // we shouldn't be here...
      System.err.println("@ " + new java.util.Date() + " : ");
      System.err.println("ERROR: attempting incremental update of a "
          + "dead link (" + stringRep() + ").");
      return null;
    }

    // start out with a "dead" link, but we'll fill it in...
    LinkData diffs = new LinkData(id);

    // update the values...
    if ((other.mask & ENDS) == ENDS && end1 != other.end1) {
      end1 = other.end1;
      end2 = other.end2;
      diffs.mask |= ENDS;
      diffs.end1 = end1;
      diffs.end2 = end2;
    }
    if ((other.mask & X1) == X1 && x1 != other.x1) {
      x1 = other.x1;
      diffs.mask |= X1;
      diffs.x1 = x1;
    }
    if ((other.mask & Y1) == Y1 && y1 != other.y1) {
      y1 = other.y1;
      diffs.mask |= Y1;
      diffs.y1 = y1;
    }
    if ((other.mask & X2) == X2 && x2 != other.x2) {
      x2 = other.x2;
      diffs.mask |= X2;
      diffs.x2 = x2;
    }
    if ((other.mask & Y2) == Y2 && y2 != other.y2) {
      y2 = other.y2;
      diffs.mask |= Y2;
      diffs.y2 = y2;
    }
    if ((other.mask & SHAPE) == SHAPE && !shape.equals(other.shape)) {
      shape = other.shape;
      diffs.mask |= SHAPE;
      diffs.shape = shape;
    }
    if ((other.mask & COLOR) == COLOR && !color.equals(other.color)) {
      color = other.color;
      diffs.mask |= COLOR;
      diffs.color = color;
    }
    if ((other.mask & HIDDEN) == HIDDEN && hidden != other.hidden) {
      hidden = other.hidden;
      diffs.mask |= HIDDEN;
      diffs.hidden = hidden;
    }
    if ((other.mask & LABEL) == LABEL && !label.equals(other.label)) {
      label = other.label;
      diffs.mask |= LABEL;
      diffs.label = label;
    }
    if ((other.mask & LABEL_COLOR) == LABEL_COLOR
        && !labelColor.equals(other.labelColor)) {
      labelColor = other.labelColor;
      diffs.mask |= LABEL_COLOR;
      diffs.labelColor = labelColor;
    }
    if ((other.mask & LINE_THICKNESS) == LINE_THICKNESS &&
        lineThickness != other.lineThickness) {
      lineThickness = other.lineThickness;
      diffs.mask |= LINE_THICKNESS;
      diffs.lineThickness = lineThickness;
    }
    if ((other.mask & DESTINATION_SIZE) == DESTINATION_SIZE &&
        destSize != other.destSize) {
      destSize = other.destSize;
      diffs.mask |= DESTINATION_SIZE;
      diffs.destSize = destSize;
    }
    if ((other.mask & HEADING) == HEADING &&
        heading != other.heading) {
      heading = other.heading;
      diffs.mask |= HEADING;
      diffs.heading = heading;
    }
    if ((other.mask & SIZE) == SIZE &&
        size != other.size) {
      size = other.size;
      diffs.mask |= SIZE;
      diffs.size = size;
    }
    if ((other.mask & BREED) == BREED &&
        breedIndex != other.breedIndex) {
      breedIndex = other.breedIndex;
      directedLink = other.directedLink;
      diffs.mask |= BREED;
      diffs.breedIndex = breedIndex;
      diffs.directedLink = directedLink;
    }

    if (diffs.mask != DEAD) {
      return diffs;
    } else {
      return null;
    }
  }

  void serialize(DataOutputStream os)
      throws IOException {
    os.writeLong(id);
    os.writeShort(mask);
    if ((mask & ENDS) == ENDS) {
      os.writeLong(end1);
      os.writeLong(end2);
    }
    if ((mask & X1) == X1) {
      os.writeDouble(x1);
    }
    if ((mask & Y1) == Y1) {
      os.writeDouble(y1);
    }
    if ((mask & X2) == X2) {
      os.writeDouble(x2);
    }
    if ((mask & Y2) == Y2) {
      os.writeDouble(y2);
    }
    if ((mask & SHAPE) == SHAPE) {
      os.writeUTF(shape);
    }
    if ((mask & COLOR) == COLOR) {
      os.writeInt(org.nlogo.api.Color.getARGBIntByRGBAList(color));
    }
    if ((mask & HIDDEN) == HIDDEN) {
      os.writeBoolean(hidden);
    }
    if ((mask & LABEL) == LABEL) {
      os.writeUTF(label);
    }
    if ((mask & LABEL_COLOR) == LABEL_COLOR) {
      os.writeInt(org.nlogo.api.Color.getARGBIntByRGBAList(labelColor));
    }
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS) {
      os.writeDouble(lineThickness);
    }
    if ((mask & DESTINATION_SIZE) == DESTINATION_SIZE) {
      os.writeDouble(destSize);
    }
    if ((mask & HEADING) == HEADING) {
      os.writeDouble(heading);
    }
    if ((mask & SIZE) == SIZE) {
      os.writeDouble(size);
    }
    if ((mask & BREED) == BREED) {
      os.writeInt(breedIndex);
      os.writeBoolean(directedLink);
    }
  }

  public String classDisplayName() {
    throw new UnsupportedOperationException();
  }

  public void setVariable(int vn, Object value) {
    throw new UnsupportedOperationException();
  }

  public Object getVariable(int vn) {
    throw new UnsupportedOperationException();
  }

  public Object[] variables() {
    throw new UnsupportedOperationException();
  }

  public int alpha() {
    throw new UnsupportedOperationException();
  }

  public boolean isPartiallyTransparent() {
    throw new UnsupportedOperationException();
  }
}
