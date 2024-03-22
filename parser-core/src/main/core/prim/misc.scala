// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

//scalastyle:off number.of.types
case class _turtleson() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentType | Syntax.AgentsetType),
      ret = Syntax.TurtlesetType)
}
case class _and() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.BooleanType,
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 6)
}
case class _any() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.BooleanType)
}
case class _ask() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentsetType | Syntax.AgentType, Syntax.CommandBlockType),
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _askconcurrent() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentsetType, Syntax.CommandBlockType),
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _bk() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _breed(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
}
case class _breedvariable(name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "-T--")
}
case class _call(proc: FrontEndProcedure) extends Command {
  agentClassString = proc.agentClassString

  def name: String = proc.name
  override def syntax = proc.syntax
  override def toString =
    s"_call($name)"
}
case class _callreport(proc: FrontEndProcedure) extends Reporter {
  agentClassString = proc.agentClassString

  def name: String = proc.name
  override def syntax = proc.syntax
  override def toString =
    s"_call($name)"
}
case class _carefully() extends Command {
  //TODO: This may not work...
  val let: Let = Let("~CAREFULLY_ERROR")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType, Syntax.CommandBlockType),
      introducesContext = true)
}
case class _commandlambda(
  arguments:       Lambda.Arguments,
  closedVariables: Set[ClosedVariable],
  source:          Option[String]) extends Lambda with Reporter {
  def this(args: Lambda.Arguments) = this(args, Set(), None)

  override def syntax =
    Syntax.reporterSyntax(ret = Syntax.CommandType)

  override def toString =
    "_commandlambda" + argumentNames.mkString("(", ", ", ")")

  def copy(
    arguments:       Lambda.Arguments    = arguments,
    closedVariables: Set[ClosedVariable] = closedVariables,
    source:          Option[String]      = source): _commandlambda = {
    val ct = new _commandlambda(arguments, closedVariables, source)
    copyInstruction(ct)
  }
}
case class _const(value: AnyRef) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      ret = value match {
        case b: java.lang.Boolean => Syntax.BooleanType
        case d: java.lang.Double  => Syntax.NumberType
        case l: LogoList          => Syntax.ListType
        case s: String            => Syntax.StringType
        case _                    => Syntax.WildcardType
      })
}
case class _constcodeblock(value: Seq[Token]) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(ret = Syntax.CodeBlockType)
  override def toString = value.map(_.text).mkString("`[ ", " ", " ]`")
}
case class _count() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.NumberType)
}
case class _createorderedturtles(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "O---",
      blockAgentClassString = Option("-T--"))
}
case class _createturtles(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "O---",
      blockAgentClassString = Option("-T--"))
}
case class _done() extends Command {
  override def syntax = Syntax.commandSyntax(canBeConcise = false)
}
case class _equal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.WildcardType,
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 5)
}
case class _errormessage(let: Option[Let]) extends Reporter {
  def this() = this(None)

  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)

  override def toString =
    "_errormessage()"

  def copy(let: Option[Let] = let): _errormessage = {
    val em = new _errormessage(let)
    copyInstruction(em)
  }
}
case class _extern(syntax: Syntax) extends Command {
  override def toString =
    s"_extern(${token.text.toUpperCase})"
}
case class _externreport(syntax: Syntax) extends Reporter {
  override def toString =
    s"_externreport(${token.text.toUpperCase})"
}
case class _fd() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _greaterthan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _hatch(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "-T--",
      blockAgentClassString = Option("-T--"))
}
case class _homedirectory() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType,
      defaultOption = Some(0),
      minimumOption = Some(0))
}
case class _inradius() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.TurtlesetType | Syntax.PatchsetType,
      right = List(Syntax.NumberType),
      ret = Syntax.TurtlesetType | Syntax.PatchsetType,
      precedence = Syntax.NormalPrecedence + 3,
      agentClassString = "-TP-")
}
case class _jump() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
// This is the `->` sigil used by lambda to separate arguments from body.
// We want it treated like a primitive, but the parser ignores the value.
case class _lambdaargs() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(),
      ret = Syntax.VoidType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _lambdavariable(name: String, synthetic: Boolean = false) extends Reporter {
  def this(name: String) = this(name, false)
  override def syntax =
    Syntax.reporterSyntax(ret = Syntax.WildcardType)
  override def toString =
    s"_lambdavariable($name)"
}
case class _lessthan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _multilet(lets: Seq[(Token, Let)]) extends Command {
  override def syntax =
    Syntax.commandSyntax(right = List(Syntax.ListType))

  def letList: String =
    lets.map(_._1.text).mkString("[", " ", "]")

}
case class _multiassignitem() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(ret = Syntax.WildcardType, right = List(Syntax.ListType))
}
case class _let(let: Option[Let], tokenText: Option[String]) extends Command {
  def this() = this(None, None)
  override def syntax =
    Syntax.commandSyntax(right = List(Syntax.WildcardType, Syntax.WildcardType))

  override def toString = {
    val vals = Seq(let.map(_.toString).getOrElse("None"), tokenText.getOrElse("None"))
    s"_let(${vals.mkString(",")})"
  }

  def copy(let: Let, tokenText: Option[String]): _let = {
    val newLet = new _let(Some(let), tokenText)
    copyInstruction(newLet)
  }
}
case class _letname() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _letvariable(let: Let) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _linkbreedvariable(name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "---L")
}
case class _linkvariable(vn: Int, returnType: Int = Syntax.WildcardType) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(ret = returnType | Syntax.ReferenceType, agentClassString = "---L")
}
case class _list() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.ListType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
case class _minus() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 3)
}
case class _neighbors() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType,
      agentClassString = "-TP-")
}
case class _neighbors4() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType,
      agentClassString = "-TP-")
}
case class _nobody() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NobodyType)
}
case class _not() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType)
}
case class _notequal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.WildcardType,
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 5)
}
case class _observervariable(vn: Int, returnType: Int = Syntax.WildcardType) extends Reporter with Referenceable {
  override def syntax =
    Syntax.reporterSyntax(ret = returnType | Syntax.ReferenceType)
  def makeReference =
    new Reference(AgentKind.Observer, vn, this)
  override def toString = s"_observervariable($vn)"
}
case class _of() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.ReporterBlockType,
      right = List(Syntax.AgentType | Syntax.AgentsetType),
      ret = Syntax.WildcardType,
      precedence = Syntax.NormalPrecedence + 1,
      isRightAssociative = true,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _oneof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType | Syntax.ListType),
      ret = Syntax.WildcardType)
}
case class _or() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.BooleanType,
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 6)
}
case class _other() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.AgentsetType)
}
case class _patchat() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType | Syntax.NobodyType,
      agentClassString = "-TP-")
}
case class _patches() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType)
}
case class _patchvariable(vn: Int, returnType: Int = Syntax.WildcardType) extends Reporter with Referenceable {
  override def syntax =
    Syntax.reporterSyntax(
      ret = returnType | Syntax.ReferenceType,
      agentClassString = "-TP-")
  def makeReference =
    new Reference(AgentKind.Patch, vn, this)
  override def toString = s"_patchvariable($vn)"
}
case class _procedurevariable(vn: Int, name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _random() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _repeat() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType))
}
case class _report() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}
case class _reporterlambda(
  arguments:       Lambda.Arguments,
  closedVariables: Set[ClosedVariable],
  source:          Option[String]) extends Lambda with Reporter {
  def this(args: Lambda.Arguments) = this(args, Set(), None)

  override def syntax = {
    Syntax.reporterSyntax(
      right = List(Syntax.CodeBlockType, Syntax.ReporterType),
      ret = Syntax.ReporterType)
  }

  override def toString =
    "_reporterlambda" + argumentNames.mkString("(", ", ", ")")

  def copy(
    arguments:       Lambda.Arguments    = arguments,
    closedVariables: Set[ClosedVariable] = closedVariables,
    source:          Option[String]      = source): _reporterlambda = {
    val cr = new _reporterlambda(arguments, closedVariables, source)
    copyInstruction(cr)
  }
}
case class _return() extends Command {
  override def syntax =
    Syntax.commandSyntax()
  // for use in error messages
  override def displayName =
    "END"
}
case class _run() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(
        Syntax.StringType | Syntax.CommandType,
        Syntax.RepeatableType | Syntax.WildcardType),
      defaultOption = Some(1))
}
case class _sentence() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.ListType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
case class _multiset(sets: Seq[Token]) extends Command {
  override def syntax =
    Syntax.commandSyntax(right = List(Syntax.ListType))

  def setList: String =
    sets.map(_.text).mkString("[", " ", "]")

}
case class _set() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType, Syntax.WildcardType))
}
case class _sprout(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "--P-",
      blockAgentClassString = Option("-T--"))
}
case class _stop() extends Command {
  override def syntax =
    Syntax.commandSyntax()
}
case class _sum() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
case class _symbol() extends Reporter with Pure {
  override def syntax = Syntax.reporterSyntax(ret = Syntax.SymbolType)
}
case class _turtle() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.TurtleType | Syntax.NobodyType)
}
case class _turtles() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
}
case class _turtleorlinkvariable(varName: String, returnType: Int = Syntax.WildcardType) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(ret = returnType | Syntax.ReferenceType, agentClassString = "-T-L")
}
case class _turtlevariable(vn: Int, returnType: Int = Syntax.WildcardType) extends Reporter with Referenceable {
  def makeReference =
    new Reference(AgentKind.Turtle, vn, this)
  override def syntax =
    Syntax.reporterSyntax(ret = returnType | Syntax.ReferenceType, agentClassString = "-T--")
}
case class _unaryminus() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _unknownidentifier() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(ret = Syntax.WildcardType)
}
case class _unknowncommand(val syntax: Syntax) extends Command
case class _whoarenot() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.AgentsetType,
      right = List(Syntax.AgentType | Syntax.AgentsetType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 2,
      isRightAssociative = false,
      agentClassString = "OTPL"
    )
}
case class _with() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.AgentsetType,
      right = List(Syntax.BooleanBlockType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 3,
      isRightAssociative = false,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _word() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.StringType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
//scalastyle:on number.of.types
