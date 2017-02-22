// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.{ core, nvm }, core.SourceLocation


/**
 * An interface representing a node in the NetLogo abstract syntax tree (AKA parse tree, in
 * NetLogo's case).
 *
 * Each AstNode, even if synthesized, should correspond to some particular source fragment, as
 * indicated by the position and length. It's the compiler's job to make sure these values are
 * always reasonable.
 */
trait AstNode extends core.AstNode {
  def accept(v: AstVisitor)
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
}

/**
 * represents an application, in the abstract (either a reporter application
 * of a command application). This is used when parsing arguments, when we
 * don't care what kind of application the args are for.
 */
trait Application extends AstNode {
  def args: Seq[Expression]
  def coreInstruction: core.Instruction
  def nvmInstruction: nvm.Instruction
}

/**
 * represents a single procedure definition.  really just a container
 * for the procedure body, which is a Statements object.
 */
class ProcedureDefinition(val procedure: nvm.Procedure, val statements: Statements) extends AstNode {
  val sourceLocation = SourceLocation(procedure.pos, procedure.end, procedure.filename)
  def accept(v: AstVisitor) { v.visitProcedureDefinition(this) }
  def copy(procedure: nvm.Procedure = procedure, statements: Statements = statements) =
    new ProcedureDefinition(procedure, statements)
}

/**
 * represents a chunk of zero or more NetLogo statements. Note that this is
 * not necessarily a "block" of statements, as block means something specific
 * (enclosed in [], in particular). This class is used to represent other
 * groups of statements as well, for instance procedure bodies.
 */
class Statements(var stmts: scala.collection.mutable.Seq[Statement], var sourceLocation: SourceLocation) extends AstNode {
  def this(stmts: Seq[Statement], sourceLocation: SourceLocation) =
    this(scala.collection.mutable.Seq[Statement](stmts: _*), sourceLocation)

  /**
   * a List of the actual Statement objects.
   */
  def body: Seq[Statement] = stmts
  private def recomputeStartAndEnd() {
    if (stmts.isEmpty) { sourceLocation = sourceLocation.copy(start = 0, end = 0) }
    else { sourceLocation = sourceLocation.copy(start = stmts(0).start, end = stmts(stmts.size - 1).end) }
  }
  override def toString = stmts.mkString(" ")
  def accept(v: AstVisitor) { v.visitStatements(this) }
  def copy(stmts: Seq[Statement] = stmts, sourceLocation: SourceLocation = sourceLocation): Statements =
    new Statements(scala.collection.mutable.Seq[Statement](stmts: _*), sourceLocation)

}

/**
 * represents a NetLogo statement. Statements only have one form: command
 * application.
 */
class Statement(
  val coreCommand: core.Command,
  var command: nvm.Command,
  _args: collection.mutable.Buffer[Expression],
  val sourceLocation: SourceLocation)
    extends Application {

  def this(coreCommand: core.Command, command: nvm.Command, args: Seq[Expression], sourceLocation: SourceLocation) =
    this(coreCommand, command, collection.mutable.Buffer[Expression](args: _*), sourceLocation)

  def args: Seq[Expression] = _args.toSeq
  def nvmInstruction = command // for Application
  def coreInstruction = coreCommand // for Application
  override def toString = command.toString + "[" + args.mkString(", ") + "]"
  def accept(v: AstVisitor) { v.visitStatement(this) }

  // should try to remove
  def addArgument(arg: Expression) = {
    _args += arg
  }
  def replaceArg(index: Int, expr: Expression) = {
    _args(index) = expr
  }
  def removeArgument(index: Int): Unit = { _args.remove(index) }

  def copy(coreCommand: core.Command = coreCommand,
    command: nvm.Command = command,
    args: Seq[Expression] = args,
    sourceLocation: SourceLocation = sourceLocation): Statement =
      new Statement(coreCommand, command, args, sourceLocation)
}

/**
 * represents a block containing zero or more statements. Called a command
 * block rather than a statement block for consistency with usual NetLogo
 * jargon. Note that this is an Expression, and as such can be an argument
 * to commands and reporters, etc.
 */
class CommandBlock(val statements: Statements, val sourceLocation: SourceLocation) extends Expression {
  def reportedType() = core.Syntax.CommandBlockType
  override def toString = "[" + statements.toString + "]"
  def accept(v: AstVisitor) { v.visitCommandBlock(this) }

  def copy(statements: Statements = statements, sourceLocation: SourceLocation = sourceLocation): CommandBlock =
    new CommandBlock(statements, sourceLocation)
}

/**
 * represents a set of code that should not be evaluated, but can be lexed
 * and tokenized.  This is an expression, and can be used as an argument
 * to commands and reports.
 */
class CodeBlock(val code: String, val sourceLocation: SourceLocation) extends Expression {
  def reportedType() = core.Syntax.CodeBlockType
  override def toString = "[" + code + "]"
  def accept(v: AstVisitor) {}
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
  def accept(v: AstVisitor) { v.visitReporterBlock(this) }
  /**
   * computes the type of this block. Reporter block types are
   * determined in a somewhat complicated way. This is derived from
   * code from the old parser.
   */
  def reportedType(): Int = {
    val appType = app.reportedType
    import core.Syntax._
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

  def copy(app: ReporterApp = app, sourceLocation: SourceLocation = sourceLocation): ReporterBlock =
    new ReporterBlock(app, sourceLocation)
}

/**
 * represents a reporter application. This is the typical kind of NetLogo
 * expression, things like "round 5" and "3 + 4". However, this class also
 * represents things like constants, which are converted into no-arg reporter
 * applications as they're parsed.
 */
class ReporterApp(var coreReporter: core.Reporter, var reporter: nvm.Reporter,
  _args: collection.mutable.Buffer[Expression],
  val sourceLocation: SourceLocation)
extends Expression with Application {

  def this(coreReporter: core.Reporter, reporter: nvm.Reporter, args: Seq[Expression], sourceLocation: SourceLocation) =
    this(coreReporter, reporter, collection.mutable.Buffer[Expression](args: _*), sourceLocation)

  def this(coreReporter: core.Reporter, reporter: nvm.Reporter, sourceLocation: SourceLocation) =
    this(coreReporter, reporter, collection.mutable.Buffer[Expression](), sourceLocation)

  /**
   * the args for this application.
   */
  override def args: Seq[Expression] = _args
  def coreInstruction = coreReporter // for Application
  def nvmInstruction = reporter // for Application
  def reportedType() = coreReporter.syntax.ret
  def accept(v: AstVisitor) { v.visitReporterApp(this) }
  def removeArgument(index: Int) { _args.remove(index) }
  def addArgument(arg: Expression) { _args.append(arg) }
  def replaceArg(index: Int, expr: Expression) { _args(index) = expr }
  def clearArgs() { _args.clear() }
  override def toString = reporter.toString + "[" + args.mkString(", ") + "]"

  def copy(coreReporter: core.Reporter = coreReporter,
    reporter: nvm.Reporter = reporter,
    args: Seq[Expression] = args,
    sourceLocation: SourceLocation = sourceLocation): ReporterApp =
      new ReporterApp(coreReporter, reporter, scala.collection.mutable.Buffer[Expression](args: _*), sourceLocation)
}

object ReporterApp {
  def unapply(app: ReporterApp): Option[(core.Reporter, nvm.Reporter, Seq[Expression], SourceLocation)] =
    Some((app.coreReporter, app.reporter, app.args, app.sourceLocation))
}
