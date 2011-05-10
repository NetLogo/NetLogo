package org.nlogo.api;

// NOTE: This class mirrors the behavior of nvm.Syntax but is distinct
// from it. The only relationship is that nvm.Syntax uses the
// constants declared in here.

/**
 * Specifies the arguments accepted by a primitive.
 * Used by the compiler for type-checking.
 * <p>You cannot instantiate this class directly. Instead, use the static construction
 * methods <code>Syntax.commandSyntax(...)</code> or <code>Syntax.reporterSyntax(...)</code>.
 * <p/>
 * <p>For example, in a <code>Reporter</code> that takes two number arguments
 * and returns a boolean, implement <code>Primitive.getSyntax()</code> as follows:
 * <p/>
 * <pre>
 * Syntax getSyntax() {
 *   int[] right = new int[] { Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER };
 *   int ret = Syntax.TYPE_BOOLEAN;
 *   return Syntax.reporterSyntax( right, ret );
 * }
 * </pre>
 * <p/>
 * <p>An input can be made variadic, meaning that the it can be repeated any number of
 * times when enclosed in parentheses, if you add the <code>TYPE_REPEATABLE</code> flag.
 * When using variadic inputs you should also define the default number of inputs, that
 * is, the number of inputs expect if the user does not use parentheses. For example:
 * <p/>
 * <pre>
 *  Syntax getSyntax() {
 *    return Syntax.reporterSyntax
 *      ( new int[] { Syntax.TYPE_WILDCARD | Syntax.TYPE_REPEATABLE }, Syntax.TYPE_LIST , 2 ) ;
 *  }
 * </pre>
 *
 * @see Primitive#getSyntax()
 */
public final strictfp class Syntax {

  /**
   * <i>Unsupported. Do not use. </i>
   */
  public static final int TYPE_VOID = 0;

  /**
   * Type constant for number (integer or floating point). *
   */
  public static final int TYPE_NUMBER = 1;

  /**
   * Type constant for boolean. *
   */
  public static final int TYPE_BOOLEAN = 2;

  /**
   * Type constant for string. *
   */
  public static final int TYPE_STRING = 4;

  /**
   * Type constant for list. *
   */
  public static final int TYPE_LIST = 8;

  /**
   * Type constant for agentset of turtles. *
   */
  public static final int TYPE_TURTLESET = 16;

  /**
   * Type constant for agentset of patches. *
   */
  public static final int TYPE_PATCHSET = 32;

  /**
   * Type constant for agentset of links. *
   */
  public static final int TYPE_LINKSET = 64;

  /**
   * Type constant for set of agents.
   * <code>TYPE_AGENTSET = TYPE_TURTLESET | TYPE_PATCHSET | TYPE_LINKSET</code>.
   */
  public static final int TYPE_AGENTSET = TYPE_TURTLESET | TYPE_PATCHSET | TYPE_LINKSET;

  /**
   * Type constant for nobody. *
   */
  public static final int TYPE_NOBODY = 128;

  /**
   * Type constant for turtles. *
   */
  public static final int TYPE_TURTLE = 256;

  /**
   * Type constant for patches. *
   */
  public static final int TYPE_PATCH = 512;

  /**
   * Type constant for links. *
   */
  public static final int TYPE_LINK = 1024;

  /**
   * Type constant for lambdas. *
   */
  public static final int TYPE_COMMAND_LAMBDA = 2048;
  public static final int TYPE_REPORTER_LAMBDA = 4096;

  /**
   * Type constant for set of agents.
   * <code>TYPE_AGENT = TYPE_TURTLE | TYPE_PATCH | TYPE_LINK</code>.
   */
  public static final int TYPE_AGENT = TYPE_TURTLE | TYPE_PATCH | TYPE_LINK;

  /**
   * Type constant for readables.
   * <code>TYPE_READABLE = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_NOBODY</code>
   */
  public static final int TYPE_READABLE = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_NOBODY;
  /**
   * Type constant for wildcard (any input)
   * <code>TYPE_WILDCARD = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_AGENT | TYPE_AGENTSET | TYPE_NOBODY</code>
   * this type is also used to identify extension types
   */
  public static final int TYPE_WILDCARD = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_AGENT |
      TYPE_AGENTSET | TYPE_NOBODY | TYPE_COMMAND_LAMBDA | TYPE_REPORTER_LAMBDA;

  public static final int TYPE_REFERENCE = 8192;
  /**
   * Type constant for command blocks *
   */
  public static final int TYPE_COMMAND_BLOCK = 16384;
  /**
   * Type constant for boolean reporter blocks *
   */
  public static final int TYPE_BOOLEAN_BLOCK = 32768;
  /**
   * Type constant for number reporter blocks *
   */
  public static final int TYPE_NUMBER_BLOCK = 65536;
  /**
   * Type constant for non-boolean, non-number reporter blocks *
   */
  public static final int TYPE_OTHER_BLOCK = 131072;
  /**
   * Type constant for reporter blocks
   * <code>TYPE_REPORTER_BLOCK = TYPE_BOOLEAN_BLOCK | TYPE_NUMBER_BLOCK | TYPE_OTHER_BLOCK</code>
   */
  public static final int TYPE_REPORTER_BLOCK = TYPE_BOOLEAN_BLOCK | TYPE_NUMBER_BLOCK | TYPE_OTHER_BLOCK;
  public static final int TYPE_BRACKETED = TYPE_LIST | TYPE_COMMAND_BLOCK | TYPE_REPORTER_BLOCK;
  /**
   * Type constant to indicate that an input is variadic
   * it should be used with another type desgination, for example:
   * <code>TYPE_NUMBER | TYPE_REPEATABLE</code>
   * indicates that this input is a number and is variadic
   */
  public static final int TYPE_REPEATABLE = 262144;

  /**
   * Type constant for optional arguments.
   * At present, TYPE_OPTIONAL is implemented only in combination with
   * TYPE_COMMAND_BLOCK as the last argument - ST 5/25/06
   */
  public static final int TYPE_OPTIONAL = 524288;

  public static final int COMMAND_PRECEDENCE = 0;
  public static final int NORMAL_PRECEDENCE = 10;

  private final int left;
  private final int[] right;
  private final int ret;
  private final int precedence;
  private final int dfault;

  private static final Syntax EMPTY_SYNTAX = new Syntax(TYPE_VOID,
      new int[0], TYPE_VOID, NORMAL_PRECEDENCE, 0);

  private Syntax(int left, int[] right, int ret, int precedence, int dfault) {
    this.left = left;
    this.right = right;
    this.ret = ret;
    this.precedence = precedence;
    this.dfault = dfault;
  }

  public int[] getRight() {
    return right;
  }

  public int getLeft() {
    return left;
  }

  public int getRet() {
    return ret;
  }

  public int getPrecedence() {
    return precedence;
  }

  public int getDfault() {
    return dfault;
  }

  /**
   * Returns an <code>EMPTY_SYNTAX</code> Object for commands with no arguments.
   */
  public static Syntax commandSyntax() {
    return EMPTY_SYNTAX;
  }

  /**
   * Returns a <code>Syntax</code> for commands with one or more right arguments.
   *
   * @param right an array of TYPE flags that are to be to the right of the Primitive
   */
  public static Syntax commandSyntax(int[] right) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        right.length);
  }

  /**
   * Returns a <code>Syntax</code> for commands with a variable number of arguments.
   *
   * @param right  an array of TYPE flags that are to be to the right of the primitive
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  public static Syntax commandSyntax(int[] right, int dfault) {
    return new Syntax(TYPE_VOID, right, TYPE_VOID, COMMAND_PRECEDENCE,
        dfault);
  }

  /**
   * Returns a <code>Syntax</code> for reporters with no arguments
   *
   * @param ret the return type
   */
  public static Syntax reporterSyntax(int ret) {
    return new Syntax(TYPE_VOID, new int[0], ret,
        NORMAL_PRECEDENCE, 0);
  }

  /**
   * Returns a <code>Syntax</code> for reporters with infix arguments.
   *
   * @param left
   * @param right
   * @param ret        the return type
   * @param precedence
   */
  public static Syntax reporterSyntax(int left, int[] right, int ret, int precedence) {
    return new Syntax(left, right, ret, precedence, right.length);
  }

  /**
   * Returns a <code>Syntax</code> for reporters with one or more right arguments
   *
   * @param right an array of TYPE flags that are to the be right of the Primitive
   * @param ret   the return type
   */
  public static Syntax reporterSyntax(int[] right, int ret) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE,
        right.length);
  }

  /**
   * Returns a <code>Syntax</code> for reporters with a variable number of
   * arguments.
   *
   * @param right  an array of TYPE flags that are to the be right of the primitive
   * @param ret    the return type
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  public static Syntax reporterSyntax(int[] right, int ret, int dfault) {
    return new Syntax(TYPE_VOID, right, ret, NORMAL_PRECEDENCE, dfault);
  }

  ///

  public static String convertOldStyleAgentClassString(String oldStyle) {
    return
        (oldStyle.indexOf('O') == -1 ? "-" : "O") +
            (oldStyle.indexOf('T') == -1 ? "-" : "T") +
            (oldStyle.indexOf('P') == -1 ? "-" : "P") +
            (oldStyle.indexOf('L') == -1 ? "-" : "L");
  }

}
