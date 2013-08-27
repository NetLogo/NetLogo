// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

// see org.nlogo.nvm.AssemblerAssistant for an explanation of what assembly and custom assembly are,
// and an explanation of each method in the AssemblerAssistant interface, which is implemented below
// - ST 2/22/08

import org.nlogo.api.{ Token, TokenType }
import org.nlogo.nvm.{ Command, CustomAssembled, AssemblerAssistant, Procedure }
import org.nlogo.prim.{ _call, _done, _recursefast, _goto, _return, _returnreport }

/**
 * fills the code array of the Procedure object with Commands.
 */
private class Assembler {
  private val code = new collection.mutable.ArrayBuffer[Command]
  def assemble(procdef: ProcedureDefinition) {
    val proc = procdef.procedure
    assembleStatements(procdef.statements)
    val ret =
      if (proc.isReporter)
        new _returnreport
      else if (proc.isTask)
        new _done
      else
        new _return
    ret.token(new Token("END", TokenType.Keyword, ret)(proc.end, proc.end, proc.filename))
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
    stmts.foreach(stmt =>
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
    private var gotoMark = -1
    private var storedGoto: Option[_goto] = None
    def add(cmd: Command) {
      if (cmd eq stmt.command)
        if (branchMark == -1) branchMark = code.size
        else stmt.command.offset = branchMark - code.size
      code += cmd
    }
    def goTo() {
      if (gotoMark == -1) {
        storedGoto = Some(new _goto) // we'll set the offset in comeFrom()
        add(storedGoto.get)
        gotoMark = code.size
      } else {
        val g = new _goto
        g.offset = gotoMark - code.size
        add(g)
      }
    }
    def comeFrom() {
      storedGoto match {
        case Some(g) =>
          g.offset = code.size - gotoMark + 1
          storedGoto = None
        case None => gotoMark = code.size
      }
    }
    def block() { block(stmt.size - 1) }
    def block(pos: Int) {
      assembleStatements(stmt(pos).asInstanceOf[CommandBlock].statements)
    }
    def argCount = stmt.size
    def arg(i: Int) = stmt(i).asInstanceOf[ReporterApp].reporter
    def removeArg(i: Int) {
      stmt.command.args =
        (stmt.command.args.take(i) ++ stmt.command.args.drop(i + 1)).toArray
    }
    def resume() {
      if (branchMark == -1) branchMark = code.size
      else stmt.command.offset = offset
    }
    def offset =
      if (branchMark == -1) throw new IllegalStateException
      else code.size - branchMark
    def done() { code += new _done }
  }
}
