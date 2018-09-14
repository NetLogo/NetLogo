// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

// see org.nlogo.nvm.AssemblerAssistant for an explanation of what assembly and custom assembly are,
// and an explanation of each method in the AssemblerAssistant interface, which is implemented below
// - ST 2/22/08

import org.nlogo.core.{ SourceLocation, Token, TokenType }
import org.nlogo.nvm.{ Command, CustomAssembled, AssemblerAssistant }
import org.nlogo.prim.{ _call, _done, _recursefast, _goto, _return, _returnreport }
import org.nlogo.compile.api.{ CommandBlock, ProcedureDefinition, ReporterApp, Statement, Statements }

import scala.collection.mutable.{Buffer, Map => MMap}

/**
 * fills the code array of the Procedure object with Commands.
 */
private[compile] class Assembler {
  private val code = new collection.mutable.ArrayBuffer[Command]
  def assemble(procdef: ProcedureDefinition) {
    val proc = procdef.procedure
    assembleStatements(procdef.statements)
    val ret =
      if (proc.isReporter)
        new _returnreport
      else if (proc.isLambda)
        new _done
      else
        new _return
    ret.token = new Token("END", TokenType.Keyword, ret)(SourceLocation(proc.end, proc.end, proc.filename))
    code += ret
    for ((cmd, n) <- code.toList.zipWithIndex) {
      cmd.next = n + 1
      cmd.offset += n
    }
    // optimize: to foo ... foo end. like in Termites - ST 3/31/09
    def isTailRecursive(call: _call) =
      call.procedure.name == proc.name && proc.args.isEmpty
    def tailRecurse(cmd: Command) = cmd match {
      case call: _call if isTailRecursive(call) =>
        new _recursefast(call)
      case _ => cmd
    }
    proc.code = code.map(tailRecurse).toArray
  }
  def assembleStatements(stmts: Statements): collection.mutable.ArrayBuffer[Command] = {
    stmts.stmts.foreach(stmt =>
      stmt.command match {
        case ca: CustomAssembled => ca.assemble(new Assistant(stmt))
        case _ => code += stmt.command
      })
    code
  }
  /// CustomAssembled Commands use this to direct their own assembly
  /// (without being privy to implementation details)
  private class Assistant(stmt: Statement) extends AssemblerAssistant {
    private var branchMark = -1

    // Label -> (Position, Command)
    private val goTos: MMap[Int, Buffer[(Int, _goto)]] = MMap.empty[Int, Buffer[(Int, _goto)]]
    // Label -> Position
    private val labels: MMap[Int, Int] = MMap.empty[Int, Int]

    def add(cmd: Command) {
      if (cmd eq stmt.command)
        if (branchMark == -1) branchMark = code.size
        else stmt.command.offset = branchMark - code.size
      code += cmd
    }
    def goTo(label: Int = 0): Unit = {
      val pos = code.size
      val gt = new _goto // Will be fixed in a moment or when `comeFrom` is called
      goTos.getOrElseUpdate(label, Buffer.empty[(Int, _goto)]).append(pos -> gt)
      labels.get(label).foreach(l => gt.offset = l - pos)
      add(gt)
    }
    def comeFrom(label: Int = 0): Unit = {
      labels(label) = code.size
      goTos.get(label).foreach(_.foreach { case (pos, gt) =>
        gt.offset = code.size - pos
      })
    }
    def block() { block(stmt.args.size - 1) }
    def block(pos: Int) { assembleStatements(stmt.args(pos).asInstanceOf[CommandBlock].statements) }
    def argCount = stmt.args.size
    def arg(i: Int) = stmt.args(i).asInstanceOf[ReporterApp].reporter
    def removeArg(i: Int) {
      stmt.command.args = (stmt.command.args.take(i) ++ stmt.command.args.drop(i + 1)).toArray
    }
    def resume() {
      if (branchMark == -1) branchMark = code.size
      else stmt.command.offset = offset
    }
    def offset =
      if (branchMark == -1) throw new IllegalStateException
      else code.size - branchMark
    def done() { code += new _done }
    def next: Int = code.size
  }
}
