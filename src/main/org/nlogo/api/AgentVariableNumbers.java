// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api;

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

}
