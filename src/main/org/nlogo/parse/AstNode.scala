// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

// here is the AstNode (AST = Abstract Syntax Tree) trait, along with its extending traits
// Expression and Application and their subclasses: ProcedureDefinition, Statements, Statement,
// ReporterApp, ReporterBlock, CommandBlock, DelayedBlock.

// see also AstVisitor.scala which implements the Visitor pattern on these AST's.

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Procedure, Command, Reporter, Instruction }

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
  def start_=(start: Int)
  def end_=(end: Int)
}

/**
 * represents an application, in the abstract (either a reporter application
 * of a command application). This is used when parsing arguments, when we
 * don't care what kind of application the args are for.
 */
trait Application extends AstNode with collection.SeqProxy[Expression] {
  def instruction: Instruction
  def end_=(end: Int)
  def addArgument(arg: Expression)
  def replaceArg(index: Int, expr: Expression)
}

/**
 * represents a single procedure definition.  really just a container
 * for the procedure body, which is a Statements object.
 */
class ProcedureDefinition(val procedure: Procedure, val statements: Statements) extends AstNode {
  def start = procedure.pos
  def end = procedure.endPos
  def file = procedure.fileName
  def accept(v: AstVisitor) { v.visitProcedureDefinition(this) }
}

/**
 * represents a chunk of zero or more NetLogo statements. Note that this is
 * not necessarily a "block" of statements, as block means something specific
 * (enclosed in [], in particular). This class is used to represent other
 * groups of statements as well, for instance procedure bodies.
 */
class Statements(val file: String) extends AstNode with collection.SeqProxy[Statement] {
  var start: Int = _
  var end: Int = _
  def self = stmts // for SeqProxy
  /**
   * a List of the actual Statement objects.
   */
  private val stmts = new collection.mutable.ArrayBuffer[Statement]
  def addStatement(stmt: Statement) {
    stmts.append(stmt)
    recomputeStartAndEnd()
  }
  private def recomputeStartAndEnd() {
    if (stmts.isEmpty) { start = 0; end = 0 }
    else { start = stmts(0).start; end = stmts(stmts.size - 1).end }
  }
  override def toString = stmts.mkString(" ")
  def accept(v: AstVisitor) { v.visitStatements(this) }
}

/**
 * represents a NetLogo statement. Statements only have one form: command
 * application.
 */
class Statement(var command: Command, var start: Int, var end: Int, val file: String)
    extends Application with collection.SeqProxy[Expression] {
  val args = new collection.mutable.ArrayBuffer[Expression]
  def instruction = command // for Application
  def self = args // for SeqProxy
  def addArgument(arg: Expression) { args.append(arg) }
  override def toString = command.toString + "[" + args.mkString(", ") + "]"
  def accept(v: AstVisitor) { v.visitStatement(this) }
  def replaceArg(index: Int, expr: Expression) { args(index) = expr }
  def removeArgument(index: Int) { args.remove(index) }
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
  def accept(v: AstVisitor) { v.visitCommandBlock(this) }
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
  def accept(v: AstVisitor) { v.visitReporterBlock(this) }
  /**
   * computes the type of this block. Reporter block types are
   * determined in a somewhat complicated way. This is derived from
   * code from the old parser.
   */
  def reportedType(): Int = {
    val appType = app.reportedType
    appType match {
      case Syntax.BooleanType => Syntax.BooleanBlockType
      case Syntax.NumberType => Syntax.NumberBlockType
      case _ =>
        if (Syntax.compatible(appType, Syntax.BooleanType)
            || Syntax.compatible(appType, Syntax.NumberType))
          Syntax.ReporterBlockType
        else Syntax.OtherBlockType
    }
  }
}

/**
 * represents a reporter application. This is the typical kind of NetLogo
 * expression, things like "round 5" and "3 + 4". However, this class also
 * represents things like constants, which are converted into no-arg reporter
 * applications as they're parsed.
 */
class ReporterApp(var reporter: Reporter, var start: Int, var end: Int, val file: String)
extends Expression with Application with collection.SeqProxy[Expression] {
  /**
   * the args for this application.
   */
  val args = new collection.mutable.ArrayBuffer[Expression]
  def self = args // for SeqProxy
  def instruction = reporter // for Application
  def addArgument(arg: Expression) { args.append(arg) }
  def reportedType() = reporter.syntax.ret
  def accept(v: AstVisitor) { v.visitReporterApp(this) }
  def removeArgument(index: Int) { args.remove(index) }
  def replaceArg(index: Int, expr: Expression) { args(index) = expr }
  def clearArgs() { args.clear() }
  override def toString = reporter.toString + "[" + args.mkString(", ") + "]"
}
