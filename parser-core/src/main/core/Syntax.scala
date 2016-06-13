// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

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

case class Syntax private(
  precedence: Int,
  left: Int,
  right: List[Int],
  ret: Int,
  defaultOption: Option[Int],
  minimumOption: Option[Int], // minimum number of args might be different than the default
  isRightAssociative: Boolean, // only relevant if infix
  agentClassString: String,
  blockAgentClassString: Option[String],
  introducesContext: Boolean)
{

  import Syntax._

  require(agentClassString == null ||
          agentClassString.size == 4)
  require(blockAgentClassString.forall(s => s.size == 4 || s == "?"))

  /**
   * indicates whether this instruction should be parsed as infix. Infix
   * instructions expect exactly one argument on the left and should not
   * be variadic on the right.
   *
   * @return true if this instruction is infix, false otherwise.
   */
  def isInfix =
    left != VoidType

  /**
   * determines whether an instruction allows a variable number of args.
   */
  def isVariadic: Boolean =
    right.exists(compatible(_, Syntax.RepeatableType))

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
    buf.toString
  }

}


object Syntax {

  private def apply = throw new UnsupportedOperationException

  def commandSyntax(
    right: List[Int] = List(),
    defaultOption: Option[Int] = None,
    minimumOption: Option[Int] = None, // minimum number of args might be different than the default
    agentClassString: String = "OTPL",
    blockAgentClassString: Option[String] = None,
    introducesContext: Boolean = false
  ): Syntax =
    new Syntax(
      precedence = CommandPrecedence,
      left = Syntax.VoidType,
      right = right,
      ret = Syntax.VoidType,
      defaultOption = defaultOption,
      minimumOption = minimumOption,
      isRightAssociative = false,
      agentClassString = agentClassString,
      blockAgentClassString = blockAgentClassString,
      introducesContext = introducesContext || blockAgentClassString.nonEmpty
  )

  def reporterSyntax(
    precedence: Int = NormalPrecedence,
    left: Int = Syntax.VoidType,
    right: List[Int] = List(),
    ret: Int,
    defaultOption: Option[Int] = None,
    minimumOption: Option[Int] = None,
    isRightAssociative: Boolean = false,
    agentClassString: String = "OTPL",
    blockAgentClassString: Option[String] = None
  ): Syntax = new Syntax(
    precedence = precedence,
    left = left,
    right = right,
    ret = ret,
    defaultOption = defaultOption,
    minimumOption = minimumOption,
    isRightAssociative = isRightAssociative,
    agentClassString = agentClassString,
    blockAgentClassString = blockAgentClassString,
    introducesContext = blockAgentClassString.nonEmpty
  )

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

  /**
   * for CodeBlocks, a list of tokens
   */
  val CodeBlockType = 1048576

  /**
   * for Symbols, a single token
   */
  val SymbolType = 2097152

  val CommandPrecedence = 0
  val NormalPrecedence = 10

  def compatible(mask: Int, value: Int): Boolean =
    (mask & value) != 0

  def getAgentSetMask(kind: AgentKind): Int =
    (kind: @unchecked) match {  // unchecked so Observer gives MatchError
      case AgentKind.Turtle => Syntax.TurtlesetType
      case AgentKind.Patch  => Syntax.PatchsetType
      case AgentKind.Link   => Syntax.LinksetType
    }

}
