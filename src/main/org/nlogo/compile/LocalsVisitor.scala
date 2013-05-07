// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.api.{ I18N, Let }
import org.nlogo.nvm.Procedure
import org.nlogo.prim._
import org.nlogo.parse, parse.Fail._

/**
 * This is an AstVisitor that optimizes "let" variables by converting them to "locals" variables
 * instead whenever possible, since the locals mechanism is speedier than the let mechanism.
 *
 * "Whenever possible" is "whenever it's not inside an ask". We must find and convert two prims:
 * _let and _letvariable.  We also must add the variable to the procedure's locals list.
 *
 * We also do the same thing with "repeat", which by default uses the "let" mechanism, but must be
 * changed to use the "locals" mechanism when used outside "ask". */

private class LocalsVisitor(alteredLets: collection.mutable.Map[Procedure, collection.mutable.Map[Let, Int]])
extends parse.DefaultAstVisitor {

  private var procedure: Procedure = null
  private var currentLet: _let = null  // for forbidding "let x x" and the like
  private var askNestingLevel = 0
  private var vn = 0   // used when converting _repeat to _repeatlocal

  override def visitProcedureDefinition(procdef: parse.ProcedureDefinition) {
    procedure = procdef.procedure
    alteredLets(procedure) = collection.mutable.Map()
    super.visitProcedureDefinition(procdef)
  }

  override def visitStatement(stmt: parse.Statement) {
    stmt.command match {
      case _: _ask | _: _askconcurrent =>
        askNestingLevel += 1
        super.visitStatement(stmt)
        askNestingLevel -= 1
      case l: _let =>
        currentLet = l
        // Using "__let" instead of "let" to indicates that this is a let we don't want converted
        // to a local. This can be useful for testing. - ST 11/3/10, 2/6/11
        val exempt = l.token.text.equalsIgnoreCase("__LET")
        if(!procedure.isTask && askNestingLevel == 0 && !exempt) {
          stmt.command = new _setprocedurevariable(new _procedurevariable(procedure.args.size, l.let.name))
          stmt.command.token(stmt.command.token)
          stmt.removeArgument(0)
          alteredLets(procedure).put(l.let, procedure.args.size)
          procedure.localsCount += 1
          procedure.args :+= l.let.name
          super.visitStatement(stmt)
        }
        else stmt.drop(1).foreach(_.accept(this)) // drop(1) skips the _letvariable which won't be evaluated
        currentLet = null
      case r: _repeat =>
        if(!procedure.isTask && askNestingLevel == 0) {
          vn = procedure.args.size
          stmt.command = new _repeatlocal(vn)
          procedure.localsCount += 1
          // actual name here doesn't really matter, I don't think - ST 11/10/05
          procedure.args :+= "_repeatlocal:" + vn
        }
        super.visitStatement(stmt)
      case ri: _repeatinternal =>
        if(askNestingLevel == 0) {
          stmt.command = new _repeatlocalinternal(vn, // vn from the _repeat we just saw
                                                  ri.offset)
        }
        super.visitStatement(stmt)
      case _ => super.visitStatement(stmt)
    }
  }

  override def visitReporterApp(expr: parse.ReporterApp) {
    expr.reporter match {
      case l: _letvariable =>
        cAssert(currentLet == null || (currentLet.let ne l.let),
                I18N.errors.getN("compiler.LocalsVisitor.notDefined", l.token.text),
                l.token)
        // it would be nice if the next line were easier to read - ST 2/6/11
        for(index <- alteredLets(procedure).get(l.let).orElse(Option(procedure.parent).flatMap(parent => alteredLets(parent).get(l.let)))) {
          val oldToken = expr.reporter.token
          expr.reporter = new _procedurevariable(index.intValue, l.let.name)
          expr.reporter.token(oldToken)
        }
      case _ =>
    }
    super.visitReporterApp(expr)
  }

}
