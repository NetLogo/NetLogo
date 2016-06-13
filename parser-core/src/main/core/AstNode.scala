// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// here is the AstNode (AST = Abstract Syntax Tree) trait, along with its extending traits
// Expression and Application and their subclasses: ProcedureDefinition, Statements, Statement,
// ReporterApp, ReporterBlock, CommandBlock, DelayedBlock.

/*
 * The jargon here is a bit different from the usual NetLogo terminology:
 *  - "command" is an actual command token itself, e.g., show, run.
 *  - "reporter" is an actual reporter itself, e.g., +, round, with.
 *  - "statement" is a syntactic form with no value and a command as head (e.g., show 5)
 *  - "expression" is a syntactic form which can occur as an argument to a command or to a
 *    reporter. expressions denote values. there are two basic kinds of expression:
 *     - reporter applications (infix or prefix). Note that this is reporter in the internal sense,
 *       which includes variables and literals. So these include, e.g., turtles with [ true ], 5 +
 *       10, 5, [1 2 3].
 *     - blocks. command and reporter blocks are expressions of this type.  a command block contains
 *       zero or more statements, while a reporter block contains exactly one expression.
 */

/**
 * An interface representing a node in the NetLogo abstract syntax tree (AKA parse tree, in
 * NetLogo's case).
 *
 * Each AstNode, even if synthesized, should correspond to some particular source fragment, as
 * indicated by the position and length. It's the compiler's job to make sure these values are
 * always reasonable.
 */
trait AstNode {
  def start: Int
  def end: Int
  def file: String
}

/**
 * represents a NetLogo expression. An expression is either a block or a
 * reporter application (variable references and constants (including lists),
 * are turned into reporter applications).
 */
trait Expression extends AstNode {
  /**
   * returns the type of this expression. Generally synthesized from
   * types of subexpressions.
   */
  def reportedType(): Int
  def start_=(start: Int)
  def end_=(end: Int)
}

/**
 * represents an application, in the abstract (either a reporter application
 * of a command application). This is used when parsing arguments, when we
 * don't care what kind of application the args are for.
 */
trait Application extends AstNode {
  def args: Seq[Expression]
  def instruction: Instruction
  def end_=(end: Int)
  def addArgument(arg: Expression)
  def replaceArg(index: Int, expr: Expression)
}

/**
 * represents a single procedure definition.  really just a container
 * for the procedure body, which is a Statements object.
 */
class ProcedureDefinition(val procedure: FrontEndProcedure, val statements: Statements) extends AstNode {
  var start = procedure.nameToken.start
  var end   = statements.end
  var file  = procedure.filename

  def nonLocalExit = statements.nonLocalExit

  def copy(procedure:  FrontEndProcedure = procedure,
           statements: Statements        = statements): ProcedureDefinition = {
    new ProcedureDefinition(procedure, statements)
  }
}

/**
 * represents a chunk of zero or more NetLogo statements. Note that this is
 * not necessarily a "block" of statements, as block means something specific
 * (enclosed in [], in particular). This class is used to represent other
 * groups of statements as well, for instance procedure bodies.
 * nonLocalExit identifies that the statements contain one or more commands
 * (possibly nested) which may cause a non-local exit (like `stop` or `report`)
 */
class Statements(val file: String, val nonLocalExit: Boolean) extends AstNode {
  def this(file: String, stmts: Seq[Statement], nonLocalExit: Boolean) = {
    this(file, nonLocalExit)
    _stmts.appendAll(stmts)
    recomputeStartAndEnd()
  }

  def this(file: String) = { this(file, Seq(), false) }

  var start: Int = _
  var end: Int = _
  /**
   * a List of the actual Statement objects.
   */
  private val _stmts = collection.mutable.Buffer[Statement]()
  def stmts: Seq[Statement] = _stmts
  def addStatement(stmt: Statement) {
    _stmts.append(stmt)
    recomputeStartAndEnd()
  }
  private def recomputeStartAndEnd() {
    if (stmts.isEmpty) { start = 0; end = 0 }
    else { start = stmts(0).start; end = stmts(stmts.size - 1).end }
  }
  override def toString = stmts.mkString(" ")

  def copy(file: String = file, stmts: Seq[Statement] = stmts, nonLocalExit: Boolean = nonLocalExit): Statements = {
    new Statements(file, stmts, nonLocalExit)
  }
}

/**
 * represents a NetLogo statement. Statements only have one form: command
 * application.
 */
class Statement(var command: Command, var start: Int, var end: Int, val file: String)
    extends Application {
  def this(command: Command, start: Int, end: Int, file: String, args: Seq[Expression]) = {
    this(command, start, end, file)
    _args.appendAll(args)
  }

  private val _args = collection.mutable.Buffer[Expression]()
  override def args: Seq[Expression] = _args
  def instruction = command // for Application
  def addArgument(arg: Expression) { _args.append(arg) }
  override def toString = command.toString + "[" + args.mkString(", ") + "]"
  def replaceArg(index: Int, expr: Expression) { _args(index) = expr }
  def removeArgument(index: Int) { _args.remove(index) }

  def copy(command: Command = command,
           start: Int = start,
           end: Int = end,
           file: String = file,
           args: Seq[Expression] = args): Statement = {
    new Statement(command, start, end, file, args)
  }
}

/**
 * represents a block containing zero or more statements. Called a command
 * block rather than a statement block for consistency with usual NetLogo
 * jargon. Note that this is an Expression, and as such can be an argument
 * to commands and reporters, etc.
 */
class CommandBlock(val statements: Statements, var start: Int, var end: Int, val file: String) extends Expression {
  def reportedType() = Syntax.CommandBlockType
  override def toString = "[" + statements.toString + "]"

  def copy(statements: Statements = statements,
           start: Int = start,
           end: Int = end,
           file: String = file): CommandBlock = {
    new CommandBlock(statements, start, end, file)
  }
}

/**
 * represents a block containing exactly one expression. Called a reporter
 * block rather than an expression block for consistency with usual NetLogo
 * jargon. Note that this is an Expression, and as such can be an argument
 * to commands and reporters, etc. However, it is a different expression from
 * the expression it contains... Its "blockness" is significant.
 */
class ReporterBlock(val app: ReporterApp, var start: Int, var end: Int, val file: String) extends Expression {
  override def toString = "[" + app.toString() + "]"
  /**
   * computes the type of this block. Reporter block types are
   * determined in a somewhat complicated way. This is derived from
   * code from the old parser.
   */
  def reportedType(): Int = {
    val appType = app.reportedType
    import Syntax._
    appType match {
      case BooleanType => BooleanBlockType
      case NumberType => NumberBlockType
      case _ =>
        if (compatible(appType, BooleanType)
            || compatible(appType, NumberType))
          ReporterBlockType
        else OtherBlockType
    }
  }

  def copy(app: ReporterApp = app, start: Int = start, end: Int = end, file: String = file): ReporterBlock = {
    new ReporterBlock(app, start, end, file)
  }
}

/**
 * represents a reporter application. This is the typical kind of NetLogo
 * expression, things like "round 5" and "3 + 4". However, this class also
 * represents things like constants, which are converted into no-arg reporter
 * applications as they're parsed.
 */
class ReporterApp(var reporter: Reporter, var start: Int, var end: Int, val file: String)
extends Expression with Application {

  def this(reporter: Reporter, start: Int, end: Int, file: String, args: Seq[Expression]) = {
    this(reporter, start, end, file)
    _args.appendAll(args)
  }
  /**
   * the args for this application.
   */
  private val _args = collection.mutable.Buffer[Expression]()
  override def args: Seq[Expression] = _args
  def instruction = reporter // for Application
  def addArgument(arg: Expression) { _args.append(arg) }
  def reportedType() = reporter.syntax.ret
  def removeArgument(index: Int) { _args.remove(index) }
  def replaceArg(index: Int, expr: Expression) { _args(index) = expr }
  def clearArgs() { _args.clear() }
  override def toString = reporter.toString + "[" + args.mkString(", ") + "]"

  def copy(reporter: Reporter = reporter,
           start: Int = start,
           end: Int = end,
           file: String = file,
           args: Seq[Expression] = args): ReporterApp = {
    new ReporterApp(reporter, start, end, file, args)
  }
}
