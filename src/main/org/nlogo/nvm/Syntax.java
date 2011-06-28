package org.nlogo.nvm;

import org.nlogo.agent.Agent;
import org.nlogo.api.LogoList;

public final strictfp class Syntax {
  public static final int TYPE_VOID = org.nlogo.api.Syntax.TYPE_VOID;
  public static final int TYPE_NUMBER = org.nlogo.api.Syntax.TYPE_NUMBER;
  public static final int TYPE_BOOLEAN = org.nlogo.api.Syntax.TYPE_BOOLEAN;
  public static final int TYPE_STRING = org.nlogo.api.Syntax.TYPE_STRING;
  public static final int TYPE_LIST = org.nlogo.api.Syntax.TYPE_LIST;
  public static final int TYPE_TURTLESET = org.nlogo.api.Syntax.TYPE_TURTLESET;
  public static final int TYPE_PATCHSET = org.nlogo.api.Syntax.TYPE_PATCHSET;
  public static final int TYPE_LINKSET = org.nlogo.api.Syntax.TYPE_LINKSET;
  public static final int TYPE_AGENTSET = org.nlogo.api.Syntax.TYPE_AGENTSET;
  public static final int TYPE_NOBODY = org.nlogo.api.Syntax.TYPE_NOBODY;
  public static final int TYPE_TURTLE = org.nlogo.api.Syntax.TYPE_TURTLE;
  public static final int TYPE_PATCH = org.nlogo.api.Syntax.TYPE_PATCH;
  public static final int TYPE_LINK = org.nlogo.api.Syntax.TYPE_LINK;
  public static final int TYPE_COMMAND_LAMBDA = org.nlogo.api.Syntax.TYPE_COMMAND_LAMBDA;
  public static final int TYPE_REPORTER_LAMBDA = org.nlogo.api.Syntax.TYPE_REPORTER_LAMBDA;
  public static final int TYPE_AGENT = org.nlogo.api.Syntax.TYPE_AGENT;
  public static final int TYPE_READABLE = org.nlogo.api.Syntax.TYPE_READABLE;
  public static final int TYPE_WILDCARD = org.nlogo.api.Syntax.TYPE_WILDCARD;
  public static final int TYPE_REFERENCE = org.nlogo.api.Syntax.TYPE_REFERENCE;
  public static final int TYPE_COMMAND_BLOCK = org.nlogo.api.Syntax.TYPE_COMMAND_BLOCK;
  public static final int TYPE_BOOLEAN_BLOCK = org.nlogo.api.Syntax.TYPE_BOOLEAN_BLOCK;
  public static final int TYPE_NUMBER_BLOCK = org.nlogo.api.Syntax.TYPE_NUMBER_BLOCK;
  public static final int TYPE_OTHER_BLOCK = org.nlogo.api.Syntax.TYPE_OTHER_BLOCK;
  public static final int TYPE_REPORTER_BLOCK = org.nlogo.api.Syntax.TYPE_REPORTER_BLOCK;
  public static final int TYPE_BRACKETED = org.nlogo.api.Syntax.TYPE_BRACKETED;
  public static final int TYPE_REPEATABLE = org.nlogo.api.Syntax.TYPE_REPEATABLE;
  // At present, TYPE_OPTIONAL is implemented only in combination with
  // TYPE_COMMAND_BLOCK as the last argument - ST 5/25/06
  public static final int TYPE_OPTIONAL = org.nlogo.api.Syntax.TYPE_OPTIONAL;

  public static final int COMMAND_PRECEDENCE = org.nlogo.api.Syntax.COMMAND_PRECEDENCE;
  public static final int NORMAL_PRECEDENCE = org.nlogo.api.Syntax.NORMAL_PRECEDENCE;

  private final int left;
  private final int[] right;
  private final int ret;
  private final int precedence;
  private final int dfault;
  private final int minimum; // minimum number of args might be different than the default
  private final boolean isRightAssociative; // only relevant if infix
  private final String agentClassString;
  private final String blockAgentClassString;
  private final boolean switches;

  public int[] right() {
    return right;
  }

  public int left() {
    return left;
  }

  public int ret() {
    return ret;
  }

  public int precedence() {
    return precedence;
  }

  public boolean isRightAssociative() {
    return isRightAssociative;
  }

  public String agentClassString() {
    return agentClassString;
  }

  public String blockAgentClassString() {
    return blockAgentClassString;
  }

  public boolean switches() {
    return switches;
  }

  private Syntax(int left, int[] right, int ret, int precedence, int dfault,
                 boolean isRightAssociative) {
    this(left, right, ret, precedence, dfault, dfault, isRightAssociative,
        "OTPL", null, false);
  }

  private Syntax(int left, int[] right, int ret, int precedence,
                 int dfault, int minimum, boolean isRightAssociative,
                 String agentClassString, String blockAgentClassString,
                 boolean switches) {
    if (!(agentClassString == null || agentClassString.length() == 4)) {
      throw new IllegalArgumentException();
    }
    if (!((blockAgentClassString == null || blockAgentClassString.length() == 4 ||
        blockAgentClassString.equals("?")))) {
      throw new IllegalArgumentException();
    }
    this.left = left;
    this.right = right;
    this.ret = ret;
    this.precedence = precedence;
    this.dfault = dfault;
    this.minimum = minimum;
    this.isRightAssociative = isRightAssociative;
    this.agentClassString = agentClassString;
    this.blockAgentClassString = blockAgentClassString;
    this.switches = switches;
  }

  // for use by no-argument commands
  public static Syntax commandSyntax() {
    return commandSyntax(false);
  }

  public static Syntax commandSyntax(boolean switches) {
    return commandSyntax("OTPL", switches);
  }

  public static Syntax commandSyntax(String agentClassString, boolean switches) {
    return new Syntax(TYPE_VOID, new int[]{}, TYPE_VOID,
        COMMAND_PRECEDENCE, 0, 0, false, agentClassString,
        null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length, false);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, boolean switches) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length, right.length, false, "OTPL", null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length, right.length, false, agentClassString,
        null, false);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString, boolean switches) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length, right.length, false, agentClassString,
        null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString,
                                     String blockAgentClassString, boolean switches) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length, right.length, false, agentClassString,
        blockAgentClassString, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, int dfault, String agentClassString,
                                     String blockAgentClassString, boolean switches) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        dfault, right.length, false, agentClassString,
        blockAgentClassString, switches);
  }

  // for use by variadic commands
  public static Syntax commandSyntax(int[] right, int dfault) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        dfault, false);
  }

  // for use by constants and no-argument reporters
  public static Syntax reporterSyntax(int ret) {
    return new Syntax(TYPE_VOID, new int[0], ret,
        NORMAL_PRECEDENCE, 0, false);
  }

  // for use by constants and no-argument reporters
  public static Syntax reporterSyntax(int ret, String agentClassString) {
    return new Syntax(TYPE_VOID, new int[0], ret,
        NORMAL_PRECEDENCE, 0, 0, false, agentClassString,
        null, false);
  }

  // for use by infix reporters
  public static Syntax reporterSyntax(int left, int[] right, int ret,
                                      int precedence) {
    return new Syntax(left, right, ret, precedence, right.length,
        false);
  }

  // for use by infix reporters
  public static Syntax reporterSyntax(int left, int[] right, int ret,
                                      int precedence, boolean isRightAssociative) {
    return new Syntax(left, right, ret, precedence, right.length,
        isRightAssociative);
  }

  // for use by prefix reporters
  public static Syntax reporterSyntax(int[] right, int ret) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE,
        right.length, false);
  }

  // for use by prefix reporters
  public static Syntax reporterSyntax(int[] right, int ret, String agentClassString,
                                      String blockAgentClassString) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE,
        right.length, right.length, false,
        agentClassString, blockAgentClassString, false);
  }

  // for use by prefix reporters
  public static Syntax reporterSyntax(int[] right, int ret, String agentClassString) {
    return new Syntax(TYPE_VOID, right, ret,
        NORMAL_PRECEDENCE, right.length, right.length,
        false, agentClassString, null, false);
  }

  // for use by variadic reporters
  public static Syntax reporterSyntax(int[] right, int ret, int dfault) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE,
        dfault, false);
  }

  // for use by variadic reporters when min is different than default
  public static Syntax reporterSyntax(int[] right, int ret, int dfault, int min) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE,
        dfault, min, false, "OTPL", null, false);
  }

  // for use by reporters that take a reporter block
  public static Syntax reporterSyntax(int left, int[] right, int ret,
                                      int precedence, boolean isRightAssociative,
                                      String agentClassString,
                                      String blockAgentClassString) {
    return new Syntax(left, right, ret, precedence,
        right.length, right.length, isRightAssociative,
        agentClassString, blockAgentClassString, false);
  }

  public static Syntax reporterSyntax(int left, int[] right, int ret, int precedence,
                                      int dfault, boolean isRightAssociative,
                                      String agentClassString, String blockAgentClassString) {
    return new Syntax(left, right, ret, precedence, dfault, right.length, isRightAssociative,
        agentClassString, blockAgentClassString, false);

  }


  /**
   * indicates whether this instruction should be parsed as infix. Infix
   * instructions expect exactly one argument on the left and should not
   * be variadic on the right.
   *
   * @return true if this instruction is infix, false otherwise.
   */
  public boolean isInfix() {
    return left != TYPE_VOID;
  }

  /**
   * returns the number of args this instruction takes on the right
   * by default.
   */
  public int rightDefault() {
    return takesOptionalCommandBlock()
        ? dfault - 1
        : dfault;
  }

  /**
   * returns the total number of args, left and right, this instruction
   * takes by default.
   */
  public int totalDefault() {
    return rightDefault() + (isInfix() ? 1 : 0);
  }

  public int min() {
    return minimum;
  }

  public boolean takesOptionalCommandBlock() {
    return right.length > 0 &&
        compatible(right[right.length - 1], TYPE_OPTIONAL);
  }

  /**
   * This may not be perfect, but for our purposes we really
   * only need numbers less than 10 anyway.  ~Forrest (11/10/2006)
   *
   * @param num - the cardinal number
   * @return - the ordinal number (as text)
   */
  private static String getOrdinalNumberText(int num) {
    switch (num)  // NOPMD pmd is confused by "break" not appearing anywhere
    {
      case 1:
        return "first";
      case 2:
        return "second";
      case 3:
        return "third";
      case 4:
        return "fourth";
      case 5:
        return "fifth";
      case 6:
        return "sixth";
      case 7:
        return "seventh";
      case 8:
        return "eighth";
      case 9:
        return "ninth";
      case 10:
        return "tenth";
      case 11:
        return "eleventh";
      case 12:
        return "twelfth";
      default:
        switch (num % 10) {
          case 1:
            return num + "st";
          case 2:
            return num + "nd";
          case 3:
            return num + "rd";
          default:
            return num + "th";
        }
    }
  }

  /**
   * The returns an english-text phrase, describing the position
   * of the argument at the given index, relative to the
   * instruction itself.
   * (e.g. "the second input", "the first argument on the right", etc)
   * ~Forrest (11/10/2006)
   * <p/>
   * CURRENTLY NOT USED!
   * (But it seemed potentially useful, so I didn't delete it)
   * ~Forrest (11/13/2006)
   *
   * @param argIndex - index of the argument we want to describe
   * @return english-text description of the argument's position
   */
  public String getPositionPhrase(int argIndex) {
    if (isInfix()) {
      if (argIndex == 0) {
        return "the input on the left";
      } else {
        return "the " + getOrdinalNumberText(argIndex) + " input on the right";
      }
    } else {
      return "the " + getOrdinalNumberText(argIndex + 1) + " input";
    }
  }

  public static int getAgentSetMask(Class<? extends Agent> type) {
    if (type == org.nlogo.agent.Turtle.class) {
      return TYPE_TURTLESET;
    }
    if (type == org.nlogo.agent.Patch.class) {
      return TYPE_PATCHSET;
    }
    if (type == org.nlogo.agent.Link.class) {
      return TYPE_LINKSET;
    }
    return TYPE_AGENTSET;
  }

  public static boolean compatible(int mask, int value) {
    return (mask & value) > 0;
  }

  public static String aTypeName(Object obj) {
    String result = typeName(obj);
    if (obj == org.nlogo.api.Nobody$.MODULE$) {
      return result;
    } else {
      return addAOrAn(result);
    }
  }

  public static String typeName(Object obj) {
    if (obj instanceof Number) {
      return typeName(TYPE_NUMBER);
    } else if (obj instanceof Boolean) {
      return typeName(TYPE_BOOLEAN);
    } else if (obj instanceof String) {
      return typeName(TYPE_STRING);
    } else if (obj instanceof LogoList) {
      return typeName(TYPE_LIST);
    } else if (obj instanceof org.nlogo.agent.AgentSet) {
      return typeName(TYPE_AGENTSET);
    } else if (obj == org.nlogo.api.Nobody$.MODULE$) {
      return typeName(TYPE_NOBODY);
    } else if (obj instanceof org.nlogo.agent.Turtle) {
      return typeName(TYPE_TURTLE);
    } else if (obj instanceof org.nlogo.agent.Patch) {
      return typeName(TYPE_PATCH);
    } else if (obj instanceof org.nlogo.agent.Link) {
      return typeName(TYPE_LINK);
    } else if (obj instanceof org.nlogo.nvm.ReporterLambda) {
      return typeName(TYPE_REPORTER_LAMBDA);
    } else if (obj instanceof org.nlogo.nvm.CommandLambda) {
      return typeName(TYPE_COMMAND_LAMBDA);
    } else if (obj == null) {
      return "null";
    } else {
      return obj.getClass().getName();
    }
  }

  public static String aTypeName(int mask) {
    String result = typeName(mask);
    if (result.equals("NOBODY")) {
      return "NOBODY";
    } else if (result.equals("anything")) {
      return result;
    } else {
      return addAOrAn(result);
    }
  }

  public static String typeName(int mask) {
    String result = "(none)";
    if (compatible(mask, TYPE_REPEATABLE)) {
      mask = subtractMasks(mask, TYPE_REPEATABLE);
    }
    if (compatible(mask, TYPE_REFERENCE)) {
      return "variable";
    } else if ((mask & TYPE_BRACKETED) == TYPE_BRACKETED) {
      result = "list or block";
      mask = subtractMasks(mask, TYPE_BRACKETED);
    } else if ((mask & TYPE_WILDCARD) == TYPE_WILDCARD) {
      result = "anything";
      mask = subtractMasks(mask, TYPE_WILDCARD);
    } else if ((mask & TYPE_AGENT) == TYPE_AGENT) {
      result = "agent";
      mask = subtractMasks(mask, TYPE_AGENT | TYPE_NOBODY);
    } else if (compatible(mask, TYPE_NUMBER)) {
      result = "number";
      mask = subtractMasks(mask, TYPE_NUMBER);
    } else if (compatible(mask, TYPE_BOOLEAN)) {
      result = "TRUE/FALSE";
      mask = subtractMasks(mask, TYPE_BOOLEAN);
    } else if (compatible(mask, TYPE_STRING)) {
      result = "string";
      mask = subtractMasks(mask, TYPE_STRING);
    } else if (compatible(mask, TYPE_LIST)) {
      result = "list";
      mask = subtractMasks(mask, TYPE_LIST);
    } else if ((mask & TYPE_AGENTSET) == TYPE_AGENTSET) {
      result = "agentset";
      mask = subtractMasks(mask, TYPE_AGENTSET);
    } else if (compatible(mask, TYPE_TURTLESET)) {
      result = "turtle agentset";
      mask = subtractMasks(mask, TYPE_TURTLESET);
    } else if (compatible(mask, TYPE_PATCHSET)) {
      result = "patch agentset";
      mask = subtractMasks(mask, TYPE_PATCHSET);
    } else if (compatible(mask, TYPE_LINKSET)) {
      result = "link agentset";
      mask = subtractMasks(mask, TYPE_LINKSET);
    } else if (compatible(mask, TYPE_TURTLE)) {
      result = "turtle";
      mask = subtractMasks(mask, TYPE_TURTLE | TYPE_NOBODY);
    } else if (compatible(mask, TYPE_PATCH)) {
      result = "patch";
      mask = subtractMasks(mask, TYPE_PATCH | TYPE_NOBODY);
    } else if (compatible(mask, TYPE_LINK)) {
      result = "link";
      mask = subtractMasks(mask, TYPE_LINK | TYPE_NOBODY);
    } else if (compatible(mask, TYPE_REPORTER_LAMBDA)) {
      result = "reporter task";
      mask = subtractMasks(mask, TYPE_REPORTER_LAMBDA);
    } else if (compatible(mask, TYPE_COMMAND_LAMBDA)) {
      result = "command task";
      mask = subtractMasks(mask, TYPE_COMMAND_LAMBDA);
    } else if (compatible(mask, TYPE_NOBODY)) {
      result = "NOBODY";
      mask = subtractMasks(mask, TYPE_NOBODY);
    } else if (compatible(mask, TYPE_COMMAND_BLOCK)) {
      result = "command block";
      mask = subtractMasks(mask, TYPE_COMMAND_BLOCK);
    } else if ((mask & TYPE_REPORTER_BLOCK) == TYPE_REPORTER_BLOCK) {
      result = "reporter block";
      mask = subtractMasks(mask, TYPE_REPORTER_BLOCK);
    } else if (compatible(mask, TYPE_OTHER_BLOCK)) {
      result = "different kind of block";
      mask = subtractMasks(mask, TYPE_REPORTER_BLOCK);
    } else if (compatible(mask, TYPE_BOOLEAN_BLOCK)) {
      result = "TRUE/FALSE block";
      mask = subtractMasks(mask, TYPE_BOOLEAN_BLOCK);
    } else if (compatible(mask, TYPE_NUMBER_BLOCK)) {
      result = "number block";
      mask = subtractMasks(mask, TYPE_NUMBER_BLOCK);
    }
    if (mask == 0) {
      return result;
    } else if (mask == Syntax.TYPE_OPTIONAL) {
      return result + " (optional)";
    } else {
      return result + " or " + typeName(mask);
    }
  }

  public static int getTypeConstant(Class<?> typeC) {
    if (typeC.equals(java.lang.Object.class)) {
      return Syntax.TYPE_WILDCARD;
    } else if (typeC.equals(org.nlogo.agent.Agent.class)) {
      return Syntax.TYPE_AGENT;
    } else if (typeC.equals(org.nlogo.agent.AgentSet.class)) {
      return Syntax.TYPE_AGENTSET;
    } else if (typeC.equals(org.nlogo.api.LogoList.class)) {
      return Syntax.TYPE_LIST;
    } else if (typeC.equals(org.nlogo.agent.Turtle.class)) {
      return Syntax.TYPE_TURTLE;
    } else if (typeC.equals(org.nlogo.agent.Patch.class)) {
      return Syntax.TYPE_PATCH;
    } else if (typeC.equals(org.nlogo.agent.Link.class)) {
      return Syntax.TYPE_LINK;
    } else if (typeC.equals(org.nlogo.nvm.ReporterLambda.class)) {
      return Syntax.TYPE_REPORTER_LAMBDA;
    } else if (typeC.equals(org.nlogo.nvm.CommandLambda.class)) {
      return Syntax.TYPE_COMMAND_LAMBDA;
    } else if (typeC.equals(java.lang.String.class)) {
      return Syntax.TYPE_STRING;
    } else if (typeC.equals(java.lang.Double.class) || typeC.equals(java.lang.Double.TYPE)) {
      return Syntax.TYPE_NUMBER;
    } else if (typeC.equals(java.lang.Boolean.class) || typeC.equals(java.lang.Boolean.TYPE)) {
      return Syntax.TYPE_BOOLEAN;
    }
    // Sorry, probably should handle this better somehow.  ~Forrest (2/16/2007)
    throw new IllegalArgumentException
        ("There was no Syntax type constant found for this class " + typeC);
  }

  private static String addAOrAn(String str) {
    switch (str.toUpperCase().charAt(0))  // NOPMD intentional fallthrough
    {
      case 'A':
      case 'E':
      case 'I':
      case 'O':
      case 'U':
        return "an " + str;
      default:
        return "a " + str;
    }
  }

  private static int subtractMasks(int mask1, int mask2) {
    return mask1 - (mask1 & mask2);
  }

  public String dump() {
    StringBuffer buf = new StringBuffer(); // NOPMD
    if (left != TYPE_VOID) {
      buf.append(typeName(left));
      buf.append(',');
    }
    for (int i = 0; i < right.length; i++) {
      if (i > 0) {
        buf.append('/');
      }
      buf.append(typeName(right[i]));
    }
    if (ret != TYPE_VOID) {
      buf.append(',');
      buf.append(typeName(ret));
    }
    buf.append(',');
    buf.append(agentClassString);
    buf.append(',');
    buf.append(blockAgentClassString);
    buf.append(',');
    buf.append(precedence);
    buf.append(',');
    buf.append(dfault);
    buf.append(',');
    buf.append(minimum);
    if (isRightAssociative()) {
      buf.append(" [RIGHT ASSOCIATIVE]");
    }
    if (switches) {
      buf.append(" *");
    }
    return buf.toString();
  }

}
