// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.Dump;
import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.LogoList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Describes a turtle.
 */
public strictfp class TurtleData
    extends AgentData
    implements org.nlogo.api.Turtle {

  public AgentKind kind() { return AgentKindJ.Turtle(); }

  public static final short DEAD = 0x0000;
  public static final short XCOR = 0x0001;
  public static final short YCOR = 0x0002;
  public static final short SHAPE = 0x0004;
  public static final short COLOR = 0x0008;
  public static final short HEADING = 0x0010;
  public static final short SIZE = 0x0020;
  public static final short HIDDEN = 0x0040;
  public static final short LABEL = 0x0080;
  public static final short LABEL_COLOR = 0x0100;
  public static final short BREED_INDEX = 0x0200;
  public static final short LINE_THICKNESS = 0x0400;

  public static final short COMPLETE =
      XCOR | YCOR | SHAPE | COLOR
          | HEADING | SIZE | HIDDEN | LABEL
          | LABEL_COLOR | BREED_INDEX | LINE_THICKNESS;

  private long who;
  private short mask;
  private double xcor;
  private double ycor;
  private String shape = "";
  private LogoList color;
  private double heading;
  private double size;
  private boolean hidden;
  private String label = "";
  private LogoList labelColor;
  private int breedIndex;
  private double lineThickness;

  /**
   * creates a new TurtleData representing a turtle that has died.
   */
  TurtleData(long who) {
    this.who = who;
    mask = DEAD;
    color = LogoList.Empty();
    labelColor = LogoList.Empty();
  }

  public TurtleData(HubNetTurtleStamp turtle) {
    mask = COMPLETE;
    xcor = turtle.xcor;
    ycor = turtle.ycor;
    shape = turtle.shape;
    color(turtle.color);
    heading = turtle.heading;
    size = turtle.size;
    hidden = turtle.hidden;
    lineThickness = turtle.lineThickness;
  }

  TurtleData(long who, short mask, double xcor, double ycor, String shape,
             Object color, double heading, double size, boolean hidden,
             String label, Object labelColor, int breedIndex, double lineThickness) {
    this.who = who;
    this.mask = mask;
    this.xcor = xcor;
    this.ycor = ycor;
    this.shape = shape;
    color(color);
    this.heading = heading;
    this.size = size;
    this.hidden = hidden;
    this.label = label;
    labelColor(labelColor);
    this.breedIndex = breedIndex;
    this.lineThickness = lineThickness;
  }

  TurtleData(DataInputStream is)
      throws IOException {
    who = is.readLong();
    mask = is.readShort();
    if ((mask & XCOR) == XCOR) {
      xcor = is.readDouble();
    }
    if ((mask & YCOR) == YCOR) {
      ycor = is.readDouble();
    }
    if ((mask & SHAPE) == SHAPE) {
      shape = is.readUTF();
    }
    if ((mask & COLOR) == COLOR) {
      color(is.readInt());
    }
    if ((mask & HEADING) == HEADING) {
      heading = is.readDouble();
    }
    if ((mask & SIZE) == SIZE) {
      size = is.readDouble();
    }
    if ((mask & HIDDEN) == HIDDEN) {
      hidden = is.readBoolean();
    }
    if ((mask & LABEL) == LABEL) {
      label = is.readUTF();
    }
    if ((mask & LABEL_COLOR) == LABEL_COLOR) {
      labelColor = org.nlogo.api.Color.getRGBAListByARGB(is.readInt());
    }
    breedIndex = is.readInt();
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS) {
      lineThickness = is.readDouble();
    }
  }

  public long id() {
    return who;
  }

  @Override
  public double xcor() {
    return xcor;
  }

  public void xcor(double xcor) {
    this.xcor = xcor;
  }

  @Override
  public double ycor() {
    return ycor;
  }

  public void ycor(double ycor) {
    this.ycor = ycor;
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

  public void color(org.nlogo.core.LogoList color) {
    this.color = color;
  }

  public void color(int argb) {
    this.color = org.nlogo.api.Color.getRGBAListByARGB(argb);
  }

  public double size() {
    return size;
  }

  public void size(Double size) {
    this.size = size.doubleValue();
  }

  public String shape() {
    return shape;
  }

  public void shape(String shape) {
    this.shape = shape;
  }

  public double heading() {
    return heading;
  }

  public void heading(Double heading) {
    this.heading = heading.doubleValue();
  }

  public boolean hidden() {
    return hidden;
  }

  public void hidden(Boolean hidden) {
    this.hidden = hidden.booleanValue();
  }

  public void label(Object label) {
    this.label = Dump.logoObject(label);
  }

  public String label() {
    return label;
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

  public void labelColor(org.nlogo.core.LogoList labelColor) {
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

  public int getBreedIndex() {
    return breedIndex;
  }

  private static final String[] OVERRIDE_VARIABLES = new String[]
      {"COLOR", "LABEL", "LABEL-COLOR", "LINE-THICKNESS", "HIDDEN?", "HEADING", "SHAPE", "SIZE"};
  private static final String[] OVERRIDE_METHODS = new String[]
      {"color", "label", "labelColor", "lineThickness", "hidden", "heading", "shape", "size"};

  @Override
  public String getMethodName(int index) {
    return OVERRIDE_METHODS[index];
  }

  public static int getOverrideIndex(String varName) {
    return getOverrideIndex(OVERRIDE_VARIABLES, varName);
  }

  @Override
  public double spotlightSize() {
    return size * 2;
  }

  @Override
  public boolean wrapSpotlight() {
    return true;
  }

  public String stringRep() {
    if (mask == COMPLETE) {
      return "Turtle " + who + " (" + xcor + ", " + ycor + ", "
          + shape + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(color) + ", " + heading + ", " + size
          + ", " + hidden + ", " + label + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(labelColor)
          + ", " + breedIndex + ", " + lineThickness + ")";
    }
    if (mask == DEAD) {
      return "Turtle " + who + " (dead)";
    }
    return "Turtle " + who + " update (mask "
        + Integer.toBinaryString(mask) + ")";
  }

  boolean isComplete() {
    return (mask & COMPLETE) == COMPLETE;
  }

  boolean isDead() {
    return mask == DEAD;
  }

  /**
   * updates this TurtleData to include any changes specified by other.
   * Returns a new TurtleData representing only items which actually
   * differed between the two. If no changes are required, returns null.
   */
  TurtleData updateFrom(TurtleData other) {
    // this is just a sanity check, and could be removed without changing
    // the behavior.
    if (other.isDead()) {
      // we shouldn't be here...
      System.err.println("@ " + new java.util.Date() + " : ");
      System.err.println("ERROR: attempting incremental update of a "
          + "dead turtle (" + stringRep() + ").");
      return null;
    }

    // start out with a "dead" turtle, but we'll fill it in...
    TurtleData diffs = new TurtleData(who);

    // update the values...
    if ((other.mask & XCOR) == XCOR && xcor != other.xcor) {
      xcor = other.xcor;
      diffs.mask |= XCOR;
      diffs.xcor = xcor;
    }
    if ((other.mask & YCOR) == YCOR && ycor != other.ycor) {
      ycor = other.ycor;
      diffs.mask |= YCOR;
      diffs.ycor = ycor;
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
    if ((other.mask & HEADING) == HEADING && heading != other.heading) {
      heading = other.heading;
      diffs.mask |= HEADING;
      diffs.heading = heading;
    }
    if ((other.mask & SIZE) == SIZE && size != other.size) {
      size = other.size;
      diffs.mask |= SIZE;
      diffs.size = size;
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

    // include breed index whether it changed or not
    breedIndex = other.breedIndex;
    diffs.breedIndex = breedIndex;


    if (diffs.mask != DEAD) {
      return diffs;
    } else {
      return null;
    }
  }

  void serialize(DataOutputStream os)
      throws IOException {
    os.writeLong(who);
    os.writeShort(mask);
    if ((mask & XCOR) == XCOR) {
      os.writeDouble(xcor);
    }
    if ((mask & YCOR) == YCOR) {
      os.writeDouble(ycor);
    }
    if ((mask & SHAPE) == SHAPE) {
      os.writeUTF(shape);
    }
    if ((mask & COLOR) == COLOR) {
      os.writeInt(org.nlogo.api.Color.getARGBIntByRGBAList(color));
    }
    if ((mask & HEADING) == HEADING) {
      os.writeDouble(heading);
    }
    if ((mask & SIZE) == SIZE) {
      os.writeDouble(size);
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
    os.writeInt(breedIndex);
    if ((mask & LINE_THICKNESS) == LINE_THICKNESS) {
      os.writeDouble(lineThickness);
    }
  }

  public org.nlogo.api.AgentSet getBreed() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.World world() {
    throw new UnsupportedOperationException();
  }

  public org.nlogo.api.Patch getPatchHere() {
    throw new UnsupportedOperationException();
  }

  public void jump(double d) {
    throw new UnsupportedOperationException();
  }

  public void heading(double d) {
    throw new UnsupportedOperationException();
  }

  public String classDisplayName() {
    throw new UnsupportedOperationException();
  }

  public Object getVariable(int vn) {
    throw new UnsupportedOperationException();
  }

  public void setVariable(int vn, Object value) {
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
