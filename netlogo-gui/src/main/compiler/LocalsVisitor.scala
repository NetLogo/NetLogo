// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import CompilerExceptionThrowers._
import org.nlogo.core.{ I18N, Let }
import org.nlogo.nvm.{ Instruction, Procedure }
import org.nlogo.prim._

/**
 * This is an AstVisitor that optimizes "let" variables by converting them to "locals" variables
 * instead whenever possible, since the locals mechanism is speedier than the let mechanism.
 *
 * "Whenever possible" is "whenever it's not inside an ask". We must find and convert two prims:
 * _let and _letvariable.  We also must remove the variable from the procedure's lets list and add
 * it to the procedure's locals list.
 *
 * We also do the same thing with "repeat", which by default uses the "let" mechanism, but must be
 * changed to use the "locals" mechanism when used outside "ask". */

private class LocalsVisitor extends DefaultAstVisitor {

  private var procedure: Option[Procedure] = Option.empty[Procedure]
  private var askNestingLevel = 0
  private var vn = 0   // used when converting _repeat to _repeatlocal

  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
    procedure = None
  }

  override def visitStatement(stmt: Statement) {
    stmt.command match {
      case _: _ask | _: _askconcurrent =>
        askNestingLevel += 1
        super.visitStatement(stmt)
        askNestingLevel -= 1
      case l: _let =>
        // Using "__let" instead of "let" to indicates that this is a let we don't want converted
        // to a local. This can be useful for testing. - ST 11/3/10, 2/6/11
        val exempt = l.token.text.equalsIgnoreCase("__LET")
        if (! procedure.exists(_.isLambda) && askNestingLevel == 0 && !exempt) {
          procedure.foreach { proc =>
            convertSetToLocal(stmt,  newProcedureVar(proc.args.size, l.let, None))
            proc.alteredLets.put(l.let, proc.args.size)
            proc.localsCount += 1
            proc.args :+= l.let.name
          }
        } else for {
          proc <- procedure
          localIndex <- lookupLet(l.let, proc)
          } {
            convertSetToLocal(stmt, newProcedureVar(localIndex, l.let, None))
          }
        super.visitStatement(stmt)
      case r: _repeat =>
        if (! procedure.exists(_.isLambda) && askNestingLevel == 0) {
          procedure.foreach { proc =>
            vn = proc.args.size
            val newrepeat = new _repeatlocal(vn)
            newrepeat.copyMetadataFrom(stmt.command)
            stmt.command = newrepeat
            proc.localsCount += 1
            // actual name here doesn't really matter, I don't think - ST 11/10/05
            proc.args :+= "_repeatlocal:" + vn
          }
        }
        super.visitStatement(stmt)
      case ri: _repeatinternal =>
        if(askNestingLevel == 0) {
          val newRepeat = new _repeatlocalinternal(vn, // vn from the _repeat we just saw
                                                  ri.offset)
          newRepeat.copyMetadataFrom(stmt.command)
          stmt.command = newRepeat
        }
        super.visitStatement(stmt)
      case _ => super.visitStatement(stmt)
    }
  }

  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: _letvariable =>
        for {
          proc <- procedure
          index <- lookupLet(l.let, proc)
        } {
          val newVar = new _procedurevariable(index.intValue, l.let.name)
          newVar.copyMetadataFrom(expr.reporter)
          expr.reporter = newVar
        }
      case _ =>
    }
    super.visitReporterApp(expr)
  }

  private def convertSetToLocal(stmt: Statement, newVar: _procedurevariable): Unit = {
    val newSet = new _setprocedurevariable(newVar)
    newSet.copyMetadataFrom(stmt.command)
    stmt.command = newSet
  }

  private def lookupLet(let: Let, procedure: Procedure): Option[Int] = {
    if (procedure == null) None
    else procedure.alteredLets.get(let) orElse lookupLet(let, procedure.parent)
  }

  private def newProcedureVar(i: Int, l: Let, oldReporter: Option[Instruction]): _procedurevariable = {
    val newProcVar = new _procedurevariable(i, l.name)
    oldReporter.foreach(i => newProcVar.copyMetadataFrom(i))
    newProcVar
  }
}
