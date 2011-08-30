package org.nlogo.nvm;

import org.nlogo.api.Agent;
import org.nlogo.api.LogoList;
import org.nlogo.api.TypeNames;
import static org.nlogo.api.Syntax.*;

public final strictfp class Syntax {

  public static final int TYPE_VOID = VoidType();
  public static final int TYPE_NUMBER = NumberType();
  public static final int TYPE_BOOLEAN = BooleanType();
  public static final int TYPE_STRING = StringType();
  public static final int TYPE_LIST = ListType();
  public static final int TYPE_TURTLESET = TurtlesetType();
  public static final int TYPE_PATCHSET = PatchsetType();
  public static final int TYPE_LINKSET = LinksetType();
  public static final int TYPE_AGENTSET = AgentsetType();
  public static final int TYPE_NOBODY = NobodyType();
  public static final int TYPE_TURTLE = TurtleType();
  public static final int TYPE_PATCH = PatchType();
  public static final int TYPE_LINK = LinkType();
  public static final int TYPE_COMMAND_TASK = CommandTaskType();
  public static final int TYPE_REPORTER_TASK = ReporterTaskType();
  public static final int TYPE_AGENT = AgentType();
  public static final int TYPE_READABLE = ReadableType();
  public static final int TYPE_WILDCARD = WildcardType();
  public static final int TYPE_REFERENCE = ReferenceType();
  public static final int TYPE_COMMAND_BLOCK = CommandBlockType();
  public static final int TYPE_BOOLEAN_BLOCK = BooleanBlockType();
  public static final int TYPE_NUMBER_BLOCK = NumberBlockType();
  public static final int TYPE_OTHER_BLOCK = OtherBlockType();
  public static final int TYPE_REPORTER_BLOCK = ReporterBlockType();
  public static final int TYPE_BRACKETED = BracketedType();
  public static final int TYPE_REPEATABLE = RepeatableType();
  public static final int TYPE_OPTIONAL = OptionalType();

  public static final int COMMAND_PRECEDENCE = CommandPrecedence();
  public static final int NORMAL_PRECEDENCE = NormalPrecedence();

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
    return new Syntax(VoidType(), new int[]{}, VoidType(),
      CommandPrecedence(), 0, 0, false, agentClassString,
      null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        right.length, false);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, boolean switches) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        right.length, right.length, false, "OTPL", null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        right.length, right.length, false, agentClassString,
        null, false);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString, boolean switches) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        right.length, right.length, false, agentClassString,
        null, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, String agentClassString,
                                     String blockAgentClassString, boolean switches) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        right.length, right.length, false, agentClassString,
        blockAgentClassString, switches);
  }

  // for use by commands
  public static Syntax commandSyntax(int[] right, int dfault, String agentClassString,
                                     String blockAgentClassString, boolean switches) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        dfault, right.length, false, agentClassString,
        blockAgentClassString, switches);
  }

  // for use by variadic commands
  public static Syntax commandSyntax(int[] right, int dfault) {
    return new Syntax(VoidType(), right, VoidType(), CommandPrecedence(),
        dfault, false);
  }

  // for use by constants and no-argument reporters
  public static Syntax reporterSyntax(int ret) {
    return new Syntax(VoidType(), new int[0], ret,
        NormalPrecedence(), 0, false);
  }

  // for use by constants and no-argument reporters
  public static Syntax reporterSyntax(int ret, String agentClassString) {
    return new Syntax(VoidType(), new int[0], ret,
        NormalPrecedence(), 0, 0, false, agentClassString,
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
    return new Syntax(VoidType(), right, ret, NormalPrecedence(),
        right.length, false);
  }

  // for use by prefix reporters
  public static Syntax reporterSyntax(int[] right, int ret, String agentClassString,
                                      String blockAgentClassString) {
    return new Syntax(VoidType(), right, ret, NormalPrecedence(),
        right.length, right.length, false,
        agentClassString, blockAgentClassString, false);
  }

  // for use by prefix reporters
  public static Syntax reporterSyntax(int[] right, int ret, String agentClassString) {
    return new Syntax(VoidType(), right, ret,
        NormalPrecedence(), right.length, right.length,
        false, agentClassString, null, false);
  }

  // for use by variadic reporters
  public static Syntax reporterSyntax(int[] right, int ret, int dfault) {
    return new Syntax(VoidType(), right, ret, NormalPrecedence(),
        dfault, false);
  }

  // for use by variadic reporters when min is different than default
  public static Syntax reporterSyntax(int[] right, int ret, int dfault, int min) {
    return new Syntax(VoidType(), right, ret, NormalPrecedence(),
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
    return left != VoidType();
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
        compatible(right[right.length - 1], OptionalType());
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
    if (org.nlogo.api.Turtle.class.isAssignableFrom(type)) {
      return TurtlesetType();
    }
    if (org.nlogo.api.Patch.class.isAssignableFrom(type)) {
      return PatchsetType();
    }
    if (org.nlogo.api.Link.class.isAssignableFrom(type)) {
      return LinksetType();
    }
    return AgentsetType();
  }

  public static boolean compatible(int mask, int value) {
    return (mask & value) > 0;
  }

  public String dump() {
    StringBuffer buf = new StringBuffer(); // NOPMD
    if (left != VoidType()) {
      buf.append(TypeNames.name(left));
      buf.append(',');
    }
    for (int i = 0; i < right.length; i++) {
      if (i > 0) {
        buf.append('/');
      }
      buf.append(TypeNames.name(right[i]));
    }
    if (ret != VoidType()) {
      buf.append(',');
      buf.append(TypeNames.name(ret));
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
