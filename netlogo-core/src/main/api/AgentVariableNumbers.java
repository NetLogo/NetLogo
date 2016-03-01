// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api;

// At least for now, we keep this in Java because agent.Turtle,
// agent.Patch and so on are in Java too, and they do "switch(...) {
// case VAR_..." which won't compile unless javac knows the numbers
// are constants, which (as far as I know anyway) means they must be
// defined in Java. - ST 4/26/13

public final strictfp class AgentVariableNumbers {

  // this class is not instantiable
  private AgentVariableNumbers() { throw new IllegalStateException(); }

  // Turtle variables
  public static final int VAR_WHO = 0;
  public static final int VAR_COLOR = 1;
  public static final int VAR_HEADING = 2;
  public static final int VAR_XCOR = 3;
  public static final int VAR_YCOR = 4;
  public static final int VAR_SHAPE = 5;
  public static final int VAR_LABEL = 6;
  public static final int VAR_LABELCOLOR = 7;
  public static final int VAR_BREED = 8;
  public static final int VAR_HIDDEN = 9;
  public static final int VAR_SIZE = 10;
  public static final int VAR_PENSIZE = 11;
  public static final int VAR_PENMODE = 12;

  // Patch variables
  public static final int VAR_PXCOR = 0;
  public static final int VAR_PYCOR = 1;
  public static final int VAR_PCOLOR = 2;
  public static final int VAR_PLABEL = 3;
  public static final int VAR_PLABELCOLOR = 4;

  // Link variables
  public static final int VAR_END1 = 0;
  public static final int VAR_END2 = 1;
  public static final int VAR_LCOLOR = 2;
  public static final int VAR_LLABEL = 3;
  public static final int VAR_LLABELCOLOR = 4;
  public static final int VAR_LHIDDEN = 5;
  public static final int VAR_LBREED = 6;
  public static final int VAR_THICKNESS = 7;
  public static final int VAR_LSHAPE = 8;
  public static final int VAR_TIEMODE = 9;

  // Turtle3D variables
  public static final int VAR_WHO3D = 0;
  public static final int VAR_COLOR3D = 1;
  public static final int VAR_HEADING3D = 2;
  public static final int VAR_PITCH3D = 3;
  public static final int VAR_ROLL3D = 4;
  public static final int VAR_XCOR3D = 5;
  public static final int VAR_YCOR3D = 6;
  public static final int VAR_ZCOR3D = 7;
  public static final int VAR_SHAPE3D = 8;
  public static final int VAR_LABEL3D = 9;
  public static final int VAR_LABELCOLOR3D = 10;
  public static final int VAR_BREED3D = 11;
  public static final int VAR_HIDDEN3D = 12;
  public static final int VAR_SIZE3D = 13;
  public static final int VAR_PENSIZE3D = 14;
  public static final int VAR_PENMODE3D = 15;

  // Patch3D variables
  public static final int VAR_PXCOR3D = 0;
  public static final int VAR_PYCOR3D = 1;
  public static final int VAR_PZCOR3D = 2;
  public static final int VAR_PCOLOR3D = 3;
  public static final int VAR_PLABEL3D = 4;
  public static final int VAR_PLABELCOLOR3D = 5;

}
