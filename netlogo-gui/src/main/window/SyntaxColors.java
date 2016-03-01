// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public strictfp class SyntaxColors {
  public SyntaxColors() {
    throw new IllegalStateException("This class cannot be instantiated");
  }

  public static final java.awt.Color COMMENT_COLOR =
      new java.awt.Color(90, 90, 90); // gray

  public static final java.awt.Color COMMAND_COLOR =
      new java.awt.Color(0, 0, 170); // blue

  public static final java.awt.Color REPORTER_COLOR =
      new java.awt.Color(102, 0, 150); // purple

  public static final java.awt.Color KEYWORD_COLOR =
      new java.awt.Color(0, 127, 105); // bluish green

  public static final java.awt.Color CONSTANT_COLOR =
      new java.awt.Color(150, 55, 0); // orange

  public static final java.awt.Color DEFAULT_COLOR =
      java.awt.Color.BLACK; // black

}

