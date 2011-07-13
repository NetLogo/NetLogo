package org.nlogo.api

// NOTE: This class mirrors the behavior of nvm.Syntax but is distinct
// from it. The only relationship is that nvm.Syntax uses the
// constants declared in here.

/**
 * Specifies the arguments accepted by a primitive.
 * Used by the compiler for type-checking.
 *
 * You cannot instantiate this class directly. Instead, use the static construction
 * methods <code>Syntax.commandSyntax(...)</code> or <code>Syntax.reporterSyntax(...)</code>.
 * 
 * For example, in a <code>Reporter</code> that takes two number arguments
 * and returns a boolean, implement <code>Primitive.getSyntax()</code> as follows:
 * 
 * <pre>
 * Syntax getSyntax() {
 *   int[] right = new int[] { Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER };
 *   int ret = Syntax.TYPE_BOOLEAN;
 *   return Syntax.reporterSyntax( right, ret );
 * }
 * </pre>
 * 
 * An input can be made variadic, meaning that the it can be repeated any number of
 * times when enclosed in parentheses, if you add the <code>TYPE_REPEATABLE</code> flag.
 * When using variadic inputs you should also define the default number of inputs, that
 * is, the number of inputs expect if the user does not use parentheses. For example:
 * <
 * <pre>
 *  Syntax getSyntax() {
 *    return Syntax.reporterSyntax
 *      ( new int[] { Syntax.TYPE_WILDCARD | Syntax.TYPE_REPEATABLE }, Syntax.TYPE_LIST , 2 ) ;
 *  }
 * </pre>
 *
 * @see Primitive#getSyntax()
 */

case class Syntax(left: Int = Syntax.TYPE_VOID,
                  right: Array[Int] = Array(),
                  ret: Int = Syntax.TYPE_VOID,
                  precedence: Int = Syntax.NORMAL_PRECEDENCE,
                  dfault: Int = 0)

object Syntax {

  /** <i>Unsupported. Do not use.</i> */
  val TYPE_VOID = 0

  /** Type constant for number (integer or floating point). */
  val TYPE_NUMBER = 1

  /**
   * Type constant for boolean. *
   */
  val TYPE_BOOLEAN = 2

  /**
   * Type constant for string. *
   */
  val TYPE_STRING = 4

  /**
   * Type constant for list. *
   */
  val TYPE_LIST = 8

  /**
   * Type constant for agentset of turtles. *
   */
  val TYPE_TURTLESET = 16

  /**
   * Type constant for agentset of patches. *
   */
  val TYPE_PATCHSET = 32

  /**
   * Type constant for agentset of links. *
   */
  val TYPE_LINKSET = 64

  /**
   * Type constant for set of agents.
   * <code>TYPE_AGENTSET = TYPE_TURTLESET | TYPE_PATCHSET | TYPE_LINKSET</code>.
   */
  val TYPE_AGENTSET = TYPE_TURTLESET | TYPE_PATCHSET | TYPE_LINKSET

  /**
   * Type constant for nobody. *
   */
  val TYPE_NOBODY = 128

  /**
   * Type constant for turtles. *
   */
  val TYPE_TURTLE = 256

  /**
   * Type constant for patches. *
   */
  val TYPE_PATCH = 512

  /**
   * Type constant for links. *
   */
  val TYPE_LINK = 1024

  /**
   * Type constant for lambdas. *
   */
  val TYPE_COMMAND_LAMBDA = 2048
  val TYPE_REPORTER_LAMBDA = 4096

  /**
   * Type constant for set of agents.
   * <code>TYPE_AGENT = TYPE_TURTLE | TYPE_PATCH | TYPE_LINK</code>.
   */
  val TYPE_AGENT = TYPE_TURTLE | TYPE_PATCH | TYPE_LINK

  /**
   * Type constant for readables.
   * <code>TYPE_READABLE = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_NOBODY</code>
   */
  val TYPE_READABLE = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_NOBODY
  /**
   * Type constant for wildcard (any input)
   * <code>TYPE_WILDCARD = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_AGENT | TYPE_AGENTSET | TYPE_NOBODY</code>
   * this type is also used to identify extension types
   */
  val TYPE_WILDCARD = TYPE_NUMBER | TYPE_BOOLEAN | TYPE_STRING | TYPE_LIST | TYPE_AGENT |
      TYPE_AGENTSET | TYPE_NOBODY | TYPE_COMMAND_LAMBDA | TYPE_REPORTER_LAMBDA

  val TYPE_REFERENCE = 8192
  /**
   * Type constant for command blocks *
   */
  val TYPE_COMMAND_BLOCK = 16384
  /**
   * Type constant for boolean reporter blocks *
   */
  val TYPE_BOOLEAN_BLOCK = 32768
  /**
   * Type constant for number reporter blocks *
   */
  val TYPE_NUMBER_BLOCK = 65536
  /**
   * Type constant for non-boolean, non-number reporter blocks *
   */
  val TYPE_OTHER_BLOCK = 131072
  /**
   * Type constant for reporter blocks
   * <code>TYPE_REPORTER_BLOCK = TYPE_BOOLEAN_BLOCK | TYPE_NUMBER_BLOCK | TYPE_OTHER_BLOCK</code>
   */
  val TYPE_REPORTER_BLOCK = TYPE_BOOLEAN_BLOCK | TYPE_NUMBER_BLOCK | TYPE_OTHER_BLOCK
  val TYPE_BRACKETED = TYPE_LIST | TYPE_COMMAND_BLOCK | TYPE_REPORTER_BLOCK
  /**
   * Type constant to indicate that an input is variadic
   * it should be used with another type desgination, for example:
   * <code>TYPE_NUMBER | TYPE_REPEATABLE</code>
   * indicates that this input is a number and is variadic
   */
  val TYPE_REPEATABLE = 262144

  /**
   * Type constant for optional arguments.
   * At present, TYPE_OPTIONAL is implemented only in combination with
   * TYPE_COMMAND_BLOCK as the last argument - ST 5/25/06
   */
  val TYPE_OPTIONAL = 524288

  val COMMAND_PRECEDENCE = 0
  val NORMAL_PRECEDENCE = 10

  /** Returns a Syntax object for commands with no arguments. */
  def commandSyntax() = Syntax()

  /**
   * Returns a <code>Syntax</code> for commands with one or more right arguments.
   *
   * @param right an array of TYPE flags that are to be to the right of the Primitive
   */
  def commandSyntax(right: Array[Int]) =
    Syntax(right = right, precedence = COMMAND_PRECEDENCE, dfault = right.size)

  /**
   * Returns a <code>Syntax</code> for commands with a variable number of arguments.
   *
   * @param right  an array of TYPE flags that are to be to the right of the primitive
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def commandSyntax(right: Array[Int], dfault: Int) =
    Syntax(right = right, precedence = COMMAND_PRECEDENCE, dfault = dfault)

  /**
   * Returns a <code>Syntax</code> for reporters with no arguments
   *
   * @param ret the return type
   */
  def reporterSyntax(ret: Int) =
    Syntax(ret = ret)

  /**
   * Returns a <code>Syntax</code> for reporters with infix arguments.
   *
   * @param left
   * @param right
   * @param ret        the return type
   * @param precedence
   */
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int) =
    Syntax(left = left, right = right, ret = ret, precedence = precedence, dfault = right.size)

  /**
   * Returns a <code>Syntax</code> for reporters with one or more right arguments
   *
   * @param right an array of TYPE flags that are to the be right of the Primitive
   * @param ret   the return type
   */
  def reporterSyntax(right: Array[Int], ret: Int) =
    Syntax(right = right, ret = ret, dfault = right.size)

  /**
   * Returns a <code>Syntax</code> for reporters with a variable number of
   * arguments.
   *
   * @param right  an array of TYPE flags that are to the be right of the primitive
   * @param ret    the return type
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int) =
    Syntax(right = right, ret = ret, dfault = dfault)

  def convertOldStyleAgentClassString(oldStyle: String) =
    "OTPL".map(c => if (oldStyle.contains(c)) c else '-')

}
