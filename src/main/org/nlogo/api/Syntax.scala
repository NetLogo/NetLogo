// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

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
 * An input can be made variadic, meaning that it can be repeated any number of
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

case class Syntax(precedence: Int,
                  left: Int = Syntax.VoidType,
                  right: Array[Int] = Array(),
                  ret: Int = Syntax.VoidType,
                  defaultOption: Option[Int] = None,
                  minimumOption: Option[Int] = None, // minimum number of args might be different than the default
                  isRightAssociative: Boolean = false, // only relevant if infix
                  agentClassString: String = "OTPL",
                  blockAgentClassString: String = null,
                  switches: Boolean = false)
{

  import Syntax._

  require(agentClassString == null ||
          agentClassString.size == 4)
  require(blockAgentClassString == null ||
          blockAgentClassString.size == 4 ||
          blockAgentClassString == "?")

  /**
   * indicates whether this instruction should be parsed as infix. Infix
   * instructions expect exactly one argument on the left and should not
   * be variadic on the right.
   *
   * @return true if this instruction is infix, false otherwise.
   */
  def isInfix =
    left != VoidType

  def dfault =
    defaultOption.getOrElse(right.size)

  def minimum =
    minimumOption.getOrElse(dfault)

  /**
   * returns the number of args this instruction takes on the right
   * by default.
   */
  def rightDefault =
    if (takesOptionalCommandBlock) dfault - 1 else dfault

  /**
   * returns the total number of args, left and right, this instruction
   * takes by default.
   */
  def totalDefault =
    rightDefault + (if (isInfix) 1 else 0)

  def takesOptionalCommandBlock =
    right.lastOption.exists(compatible(_, OptionalType))

  def dump = {
    val buf = new java.lang.StringBuilder
    if (left != VoidType) {
      buf.append(TypeNames.name(left))
      buf.append(',')
    }
    for (i <- 0 until right.size) {
      if (i > 0)
        buf.append('/')
      buf.append(TypeNames.name(right(i)))
    }
    if (ret != VoidType) {
      buf.append(',')
      buf.append(TypeNames.name(ret))
    }
    buf.append(',')
    buf.append(agentClassString)
    buf.append(',')
    buf.append(blockAgentClassString)
    buf.append(',')
    buf.append(precedence)
    buf.append(',')
    buf.append(dfault)
    buf.append(',')
    buf.append(minimum)
    if (isRightAssociative)
      buf.append(" [RIGHT ASSOCIATIVE]")
    if (switches)
      buf.append(" *")
    buf.toString
  }

}


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
   * Type constant for command tasks. *
   */
  val CommandTaskType = 2048

  /**
   * Type constant for reporter tasks. *
   */
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

  def compatible(mask: Int, value: Int): Boolean =
    (mask & value) != 0

  /** Returns a Syntax object for commands with no arguments. */
  def commandSyntax() =
    Syntax(precedence = CommandPrecedence)

  /**
   * Returns a <code>Syntax</code> for commands with one or more right arguments.
   *
   * @param right an array of Type flags that are to be to the right of the Primitive
   */
  def commandSyntax(right: Array[Int]) =
    Syntax(precedence = CommandPrecedence,
           right = right)

  /**
   * Returns a <code>Syntax</code> for commands with a variable number of arguments.
   *
   * @param right  an array of Type flags that are to be to the right of the primitive
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def commandSyntax(right: Array[Int], dfault: Int) =
    Syntax(precedence = CommandPrecedence,
           right = right, defaultOption = Some(dfault))

  def commandSyntax(switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           switches = switches)

  def commandSyntax(agentClassString: String, switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           agentClassString = agentClassString, switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           right = right, switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String) =
    Syntax(precedence = CommandPrecedence,
           right = right, agentClassString = agentClassString)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String, switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           right = right, agentClassString = agentClassString, switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String, blockAgentClassString: String, switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           right = right, agentClassString = agentClassString, blockAgentClassString = blockAgentClassString,
           switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], dfault: Int, agentClassString: String,
                    blockAgentClassString: String, switches: Boolean) =
    Syntax(precedence = CommandPrecedence,
           right = right, defaultOption = Some(dfault), agentClassString = agentClassString,
           blockAgentClassString = blockAgentClassString, switches = switches)

  // for use by constants and no-argument reporters
  def reporterSyntax(ret: Int, agentClassString: String) =
    Syntax(precedence = NormalPrecedence,
           ret = ret, agentClassString = agentClassString)

  // for use by infix reporters
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int, isRightAssociative: Boolean) =
    Syntax(left = left, right = right, ret = ret, precedence = precedence, isRightAssociative = isRightAssociative)

  // for use by prefix reporters
  def reporterSyntax(right: Array[Int], ret: Int, agentClassString: String, blockAgentClassString: String) =
    Syntax(precedence = NormalPrecedence,
           right = right, ret = ret, agentClassString = agentClassString, blockAgentClassString = blockAgentClassString)

  // for use by prefix reporters
  def reporterSyntax(right: Array[Int], ret: Int, agentClassString: String) =
    Syntax(precedence = NormalPrecedence,
           right = right, ret = ret, agentClassString = agentClassString)

  // for use by variadic reporters when min is different than default
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int, minimum: Int) =
    Syntax(precedence = NormalPrecedence,
           right = right, ret = ret, defaultOption = Some(dfault), minimumOption = Some(minimum))

  // for use by reporters that take a reporter block
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int, isRightAssociative: Boolean,
                     agentClassString: String, blockAgentClassString: String) =
    Syntax(left = left, right = right, ret = ret, precedence = precedence, isRightAssociative = isRightAssociative,
           agentClassString = agentClassString, blockAgentClassString = blockAgentClassString)

  /**
   * Returns a <code>Syntax</code> for reporters with no arguments
   *
   * @param ret the return type
   */
  def reporterSyntax(ret: Int) =
    Syntax(precedence = NormalPrecedence,
           ret = ret)

  /**
   * Returns a <code>Syntax</code> for reporters with infix arguments.
   *
   * @param left
   * @param right
   * @param ret        the return type
   * @param precedence
   */
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int) =
    Syntax(left = left, right = right, ret = ret, precedence = precedence)

  /**
   * Returns a <code>Syntax</code> for reporters with one or more right arguments
   *
   * @param right an array of Type flags that are to the be right of the Primitive
   * @param ret   the return type
   */
  def reporterSyntax(right: Array[Int], ret: Int) =
    Syntax(precedence = NormalPrecedence,
           right = right, ret = ret)

  /**
   * Returns a <code>Syntax</code> for reporters with a variable number of
   * arguments.
   *
   * @param right  an array of Type flags that are to the be right of the primitive
   * @param ret    the return type
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int) =
    Syntax(precedence = NormalPrecedence,
           right = right, ret = ret, defaultOption = Some(dfault))

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

  def getAgentSetMask(kind: AgentKind): Int =
    (kind: @unchecked) match {  // unchecked so Observer gives MatchError
      case AgentKind.Turtle => Syntax.TurtlesetType
      case AgentKind.Patch  => Syntax.PatchsetType
      case AgentKind.Link   => Syntax.LinksetType
    }

}
