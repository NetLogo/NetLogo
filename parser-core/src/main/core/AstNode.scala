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
 *     - reporter applications (infix or prefix). Note that this is reporter in the internal
 *       sense, which includes variables and literals. So these include, e.g.,
 *       turtles with [ true ], 5 + 10, 5, [1 2 3].
 *     - blocks. command and reporter blocks are expressions of this type.
 *       A command block contains zero or more statements, while a reporter
 *       block contains exactly one expression.
 */

/**
 * An interface representing a node in the NetLogo abstract syntax tree (AKA parse tree, in
 * NetLogo's case).
 *
 * Each AstNode, even if synthesized, should correspond to some particular source fragment, as
 * indicated by the position and length. It's the compiler's job to make sure these values are
 * always reasonable.
 */
trait AstNode extends SourceLocatable

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

  def changeLocation(newLocation: SourceLocation): Expression
}

/**
 * represents an application, in the abstract (either a reporter application
 * of a command application). This is used when parsing arguments, when we
 * don't care what kind of application the args are for.
 */
trait Application extends AstNode {
  def args: Seq[Expression]
  def instruction: Instruction
}

/**
 * represents a single procedure definition.  really just a container
 * for the procedure body, which is a Statements object.
 */
class ProcedureDefinition(val procedure: FrontEndProcedure, val statements: Statements, _end: Int) extends AstNode {
  def this(procedure: FrontEndProcedure, stmts: Statements) = this(procedure, stmts, stmts.end)
  val sourceLocation = SourceLocation(procedure.nameToken.start, _end, procedure.filename)

  def nonLocalExit = statements.nonLocalExit

  def copy(procedure:  FrontEndProcedure = procedure,
           statements: Statements        = statements,
           end:        Int               = _end): ProcedureDefinition = {
    new ProcedureDefinition(procedure, statements, end)
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
class Statements(file: String, val stmts: Seq[Statement], val nonLocalExit: Boolean) extends AstNode {
  def this(file: String)                        = this(file, Seq(), false)
  def this(file: String, stmts: Seq[Statement]) = this(file, stmts, false)
  def this(file: String, nonLocalExit: Boolean) = this(file, Seq(), nonLocalExit)

  // def not val because `stmts` is mutable
  def sourceLocation =
    if (stmts.isEmpty) SourceLocation(0, 0, file)
    else SourceLocation(stmts(0).start, stmts.last.end, file)

  override def toString = stmts.mkString(" ")

  def copy(file: String = file, stmts: Seq[Statement] = stmts, nonLocalExit: Boolean = nonLocalExit) =
    new Statements(file, stmts, nonLocalExit)
}

/**
 * represents a NetLogo statement. Statements only have one form: command
 * application.
 */
class Statement(var command: Command, val args: Seq[Expression], val sourceLocation: SourceLocation) extends Application {
  def this(command: Command, sourceLocation: SourceLocation) =
    this(command, Seq[Expression](), sourceLocation)

  def instruction = command // for Application
  override def toString = command.toString + "[" + args.mkString(", ") + "]"

  def withArguments(args: Seq[Expression]) = new Statement(
    command, args, sourceLocation.copy(end = args.lastOption.map(_.end).getOrElse(sourceLocation.end)))

  def changeLocation(newLocation: SourceLocation): Statement = copy(location = newLocation)

  def copy(command:  Command        = command,
           args:     Seq[Expression] = args,
           location: SourceLocation = sourceLocation): Statement = {
    new Statement(command, args, location)
  }
}

/**
 * represents a block containing zero or more statements. Called a command
 * block rather than a statement block for consistency with usual NetLogo
 * jargon. Note that this is an Expression, and as such can be an argument
 * to commands and reporters, etc.
 */
class CommandBlock(val statements: Statements, val sourceLocation: SourceLocation, val synthetic: Boolean = false) extends Expression {
  def reportedType() = Syntax.CommandBlockType
  override def toString = "[" + statements.toString + "]"

  def changeLocation(newLocation: SourceLocation): CommandBlock = copy(location = newLocation)

  def copy(statements: Statements     = statements,
           location:   SourceLocation = sourceLocation): CommandBlock = {
    new CommandBlock(statements, sourceLocation, synthetic)
  }
}

/**
 * represents a block containing exactly one expression. Called a reporter
 * block rather than an expression block for consistency with usual NetLogo
 * jargon. Note that this is an Expression, and as such can be an argument
 * to commands and reporters, etc. However, it is a different expression from
 * the expression it contains... Its "blockness" is significant.
 */
class ReporterBlock(val app: ReporterApp, val sourceLocation: SourceLocation) extends Expression {
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

  def changeLocation(newLocation: SourceLocation): ReporterBlock = copy(location = newLocation)

  def copy(app: ReporterApp = app, location: SourceLocation = sourceLocation): ReporterBlock = {
    new ReporterBlock(app, location)
  }
}

object ReporterApp {
  def unapply(app: ReporterApp): Option[(Reporter, Seq[Expression], SourceLocation)] =
    Some((app.reporter, app.args, app.sourceLocation))
}

/**
 * represents a reporter application. This is the typical kind of NetLogo
 * expression, things like "round 5" and "3 + 4". However, this class also
 * represents things like constants, which are converted into no-arg reporter
 * applications as they're parsed.
 */
class ReporterApp(var reporter: Reporter, val args: Seq[Expression], val sourceLocation: SourceLocation) extends Application with Expression {
  def this(reporter: Reporter, location: SourceLocation) = this(reporter, Seq[Expression](), location)

  /**
   * the args for this application.
   */
  def instruction = reporter // for Application
  def reportedType() = reporter.syntax.ret
  override def toString = reporter.toString + "[" + args.mkString(", ") + "]"

  def withArguments(args: Seq[Expression]) =
    new ReporterApp(
      reporter,
      args,
      sourceLocation.copy(end = args.lastOption.map(_.end).getOrElse(sourceLocation.end)))

  def changeLocation(newLocation: SourceLocation): ReporterApp = copy(location = newLocation)

  def copy(reporter: Reporter = reporter, args: Seq[Expression] = args, location: SourceLocation = sourceLocation): ReporterApp =
    new ReporterApp(reporter, args, location)
}
