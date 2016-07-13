// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.{ api, core, nvm }

// This can't be an ASTVisitor, unless ASTVisitor were generalized,
// because each of the methods needs to return a value, and the
// types are always different.
//
// You could imagine generalizing this into some visitor-like thing,
// but right now, this is the only place in the code where we need
// to do something like this with core ASTs.

class ASTBackifier(backifier: Backifier) {

  def backifyProcedure(proc: nvm.Procedure, pd: core.ProcedureDefinition): ProcedureDefinition =
    new ProcedureDefinition(proc, backify(pd.statements))

  def backify(expr: core.Expression): Expression =
    expr match {
      case cb: core.CommandBlock => backify(cb)
      case rb: core.ReporterBlock => backify(rb)
      case ra: core.ReporterApp => backify(ra)
    }

  def backify(stmts: core.Statements): Statements =
    new Statements(stmts.stmts.map(backify), stmts.sourceLocation)

  def backify(stmt: core.Statement): Statement =
    new Statement(stmt.command, backifier(stmt.command), stmt.args.map(backify), stmt.sourceLocation)

  def backify(cb: core.CommandBlock): CommandBlock =
    new CommandBlock(backify(cb.statements), cb.sourceLocation)

  def backify(rb: core.ReporterBlock): ReporterBlock =
    new ReporterBlock(backify(rb.app), rb.sourceLocation)

  def backify(ra: core.ReporterApp): ReporterApp = {
    val result =
      new ReporterApp(ra.reporter, backifier(ra.reporter), ra.sourceLocation)
    ra.args.map(backify).foreach(result.addArgument)
    result
  }

}
