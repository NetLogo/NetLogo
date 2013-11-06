// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise;

// ADDING SOMETHING TO THIS CLASS?
// WELL, ARE YA, PUNK?
// If you are, open your browser's console and type in "Math.<the name of that thing>".
// If it returns `undefined`, you need to add it to 'compat.coffee', too!
// Or, maybe just consult this page to figure out if it's a part of the spec or not:
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math
// --JAB (11/4/13)
public class Strict {

  // In Scala, members are always referenced through getters, so any call to access a member is
  // a method call.  This is problematic for browser interop, since we're expecting `PI` _not_
  // to be a method on `Math`, because the spec says that it's not.  So, if the thing you need
  // to add is a constant, the most sane solution seems to be to write this class in Java.
  // Other workarounds I've attempted haven't worked, since Rhino won't let you add a member
  // to the JS object after the fact.  Not pretty, but... it works. --JAB (11/5/13)
  public static final double PI = StrictMath.PI;


  public static          double abs(double x)             { return StrictMath.abs(x); }
  public static          double atan2(double x, double y) { return StrictMath.atan2(x, y); }
  public static          double cos(double x)             { return StrictMath.cos(x); }
  public static          double floor(double x)           { return StrictMath.floor(x); }
  public static          double pow(double x, double y)   { return StrictMath.pow(x, y); }
  public static          long   round(double x)           { return StrictMath.round(x); }
  public static          double sin(double x)             { return StrictMath.sin(x); }
  public static          double sqrt(double x)            { return StrictMath.sqrt(x); }
  public static strictfp double toRadians(double x)       { return StrictMath.toRadians(x); }
  public static strictfp double toDegrees(double x)       { return StrictMath.toDegrees(x); }

}

