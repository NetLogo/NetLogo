// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.api.Dump;
import org.nlogo.api.Syntax;
import org.nlogo.api.TypeNames;

public strictfp class ArgumentTypeException
    extends EngineException {

  private final int wantedType;
  private final Object badValue;

  @SuppressWarnings("unused")  // Eclipse complains too
  private final int badArgIndex; // NOPMD pmd complains because the code that uses this is commented out below - ST 3/10/08

  public ArgumentTypeException(Context context,
                               Instruction problemInstr,
                               int badArgIndex,
                               int wantedType, Object badValue) {
    super(context, problemInstr, "message will be built later.");
    this.badArgIndex = badArgIndex;
    this.wantedType = wantedType;
    this.badValue = badValue;
  }

  /* this method should really only be called after
    * the resolveErrorInstruction() method has been called, otherwise
    * it may give faulty results.  ~Forrest (10/24/2006)
    */
  @Override
  public String getMessage() {
    String result = "";

    if (instruction != null) {
      result += instruction.displayName();
    }
    result += " expected input to be " + TypeNames.aName(wantedType);

    // if badValue is a Class object, then it's not REALLY
    // a value at all -- it's just something to tell us what
    // kind of bad value was returned.
    if (badValue instanceof Class<?>) {
      result += " but got " + TypeNames.aName(Syntax.getTypeConstant((Class<?>) badValue)) + " instead";
    } else if (badValue != null) {
      String badValueStr = Dump.logoObject(badValue, true, false);

      result += " but got " + (badValue == org.nlogo.core.Nobody$.MODULE$
          ? "NOBODY"
          : "the " + TypeNames.name(badValue) + " " + badValueStr)
          + " instead";
    }

    result += ".";

    return result;

  }

}
