package org.nlogo.api

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
 * public Syntax getSyntax() {
 *   return Syntax.reporterSyntax(
 *     new int[] { Syntax.NumberType(), Syntax.NumberType() },
 *     Syntax.BooleanType());
 * }
 * </pre>
 * 
 * An input can be made variadic, meaning that the it can be repeated any number of
 * times when enclosed in parentheses, if you add the <code>RepeatableType</code> flag.
 * When using variadic inputs you should also define the default number of inputs, that
 * is, the number of inputs expect if the user does not use parentheses. For example:
 * 
 * <pre>
 *  public Syntax getSyntax() {
 *    return Syntax.reporterSyntax(
 *      new int[] { Syntax.WildcardType() | Syntax.RepeatableType() },
 *      Syntax.ListType(), 2);
 *  }
 * </pre>
 *
 * @see Primitive#getSyntax()
 */

case class Syntax(left: Int = Syntax.VoidType,
                  right: Array[Int] = Array(),
                  ret: Int = Syntax.VoidType,
                  precedence: Int = Syntax.NormalPrecedence,
                  dfault: Int = 0)

object Syntax {

  /** <i>Unsupported. Do not use.</i> */
  val VoidType = 0

  /** Type constant for number (integer or floating point). */
  val NumberType = 1

  /**
   * Type constant for boolean. *
   */
  val BooleanType = 2

  /**
   * Type constant for string. *
   */
  val StringType = 4

  /**
   * Type constant for list. *
   */
  val ListType = 8

  /**
   * Type constant for agentset of turtles. *
   */
  val TurtlesetType = 16

  /**
   * Type constant for agentset of patches. *
   */
  val PatchsetType = 32

  /**
   * Type constant for agentset of links. *
   */
  val LinksetType = 64

  /**
   * Type constant for set of agents.
   * <code>AgentsetType = TurtlesetType | PatchsetType | LinksetType</code>.
   */
  val AgentsetType = TurtlesetType | PatchsetType | LinksetType

  /**
   * Type constant for nobody. *
   */
  val NobodyType = 128

  /**
   * Type constant for turtles. *
   */
  val TurtleType = 256

  /**
   * Type constant for patches. *
   */
  val PatchType = 512

  /**
   * Type constant for links. *
   */
  val LinkType = 1024

  /**
   * Type constant for lambdas. *
   */
  val CommandTaskType = 2048
  val ReporterTaskType = 4096

  /**
   * Type constant for set of agents.
   * <code>AgentType = TurtleType | PatchType | LinkType</code>.
   */
  val AgentType = TurtleType | PatchType | LinkType

  /**
   * Type constant for readables.
   * <code>ReadableType = NumberType | BooleanType | StringType | ListType | NobodyType</code>
   */
  val ReadableType = NumberType | BooleanType | StringType | ListType | NobodyType
  /**
   * Type constant for wildcard (any input)
   * <code>WildcardType = NumberType | BooleanType | StringType | ListType | AgentType | AgentsetType | NobodyType</code>
   * this type is also used to identify extension types
   */
  val WildcardType = NumberType | BooleanType | StringType | ListType | AgentType |
      AgentsetType | NobodyType | CommandTaskType | ReporterTaskType

  val ReferenceType = 8192
  /**
   * Type constant for command blocks *
   */
  val CommandBlockType = 16384
  /**
   * Type constant for boolean reporter blocks *
   */
  val BooleanBlockType = 32768
  /**
   * Type constant for number reporter blocks *
   */
  val NumberBlockType = 65536
  /**
   * Type constant for non-boolean, non-number reporter blocks *
   */
  val OtherBlockType = 131072
  /**
   * Type constant for reporter blocks
   * <code>ReporterBlockType = BooleanBlockType | NumberBlockType | OtherBlockType</code>
   */
  val ReporterBlockType = BooleanBlockType | NumberBlockType | OtherBlockType
  val BracketedType = ListType | CommandBlockType | ReporterBlockType
  /**
   * Type constant to indicate that an input is variadic
   * it should be used with another type desgination, for example:
   * <code>NumberType | RepeatableType</code>
   * indicates that this input is a number and is variadic
   */
  val RepeatableType = 262144

  /**
   * Type constant for optional arguments.
   * At present, OptionalType is implemented only in combination with
   * CommandBlockType as the last argument - ST 5/25/06
   */
  val OptionalType = 524288

  val CommandPrecedence = 0
  val NormalPrecedence = 10

  /** Returns a Syntax object for commands with no arguments. */
  def commandSyntax() = Syntax()

  /**
   * Returns a <code>Syntax</code> for commands with one or more right arguments.
   *
   * @param right an array of Type flags that are to be to the right of the Primitive
   */
  def commandSyntax(right: Array[Int]) =
    Syntax(right = right, precedence = CommandPrecedence, dfault = right.size)

  /**
   * Returns a <code>Syntax</code> for commands with a variable number of arguments.
   *
   * @param right  an array of Type flags that are to be to the right of the primitive
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def commandSyntax(right: Array[Int], dfault: Int) =
    Syntax(right = right, precedence = CommandPrecedence, dfault = dfault)

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
   * @param right an array of Type flags that are to the be right of the Primitive
   * @param ret   the return type
   */
  def reporterSyntax(right: Array[Int], ret: Int) =
    Syntax(right = right, ret = ret, dfault = right.size)

  /**
   * Returns a <code>Syntax</code> for reporters with a variable number of
   * arguments.
   *
   * @param right  an array of Type flags that are to the be right of the primitive
   * @param ret    the return type
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int) =
    Syntax(right = right, ret = ret, dfault = dfault)

  def convertOldStyleAgentClassString(oldStyle: String) =
    "OTPL".map(c => if (oldStyle.contains(c)) c else '-')

  def getTypeConstant(clazz: Class[_]): Int =
    if (classOf[Agent].isAssignableFrom(clazz))
      AgentType
    else if (classOf[AgentSet].isAssignableFrom(clazz))
      AgentsetType
    else if (classOf[LogoList].isAssignableFrom(clazz))
      ListType
    else if (classOf[Turtle].isAssignableFrom(clazz))
      TurtleType
    else if (classOf[Patch].isAssignableFrom(clazz))
      PatchType
    else if (classOf[Link].isAssignableFrom(clazz))
      LinkType
    else if (classOf[ReporterTask].isAssignableFrom(clazz))
      ReporterTaskType
    else if (classOf[CommandTask].isAssignableFrom(clazz))
      CommandTaskType
    else if (classOf[String].isAssignableFrom(clazz))
      StringType
    else if (classOf[java.lang.Double].isAssignableFrom(clazz) || clazz == java.lang.Double.TYPE)
      NumberType
    else if (classOf[java.lang.Boolean].isAssignableFrom(clazz) || clazz == java.lang.Boolean.TYPE)
      BooleanType
    else if (classOf[AnyRef] eq clazz)
      WildcardType
    else
      // Sorry, probably should handle this better somehow.  ~Forrest (2/16/2007)
      throw new IllegalArgumentException(
        "no Syntax type constant found for " + clazz)

}
