// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.Let
import org.nlogo.core.I18N
import org.nlogo.nvm.Procedure
import org.nlogo.prim._

/**
 * This is an AstVisitor that optimizes "let" variables by converting them to "locals" variables
 * instead whenever possible, since the locals mechanism is speedier than the let mechanism.
 *
 * "Whenever possible" is "whenever it's not inside an ask". We must find and convert two prims:
 * _let and _letvariable.  We also must add the variable to the procedure's locals list.
 *
 * We also do the same thing with "repeat", which by default uses the "let" mechanism, but must be
 * changed to use the "locals" mechanism when used outside "ask". */

class LocalsVisitor(alteredLets: collection.mutable.Map[Procedure, collection.mutable.Map[Let, Int]])
extends DefaultAstVisitor {

  private var procedure: Procedure = null
  private var askNestingLevel = 0
  private var vn = 0   // used when converting _repeat to _repeatlocal

  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = procdef.procedure
    alteredLets(procedure) = collection.mutable.Map()
    super.visitProcedureDefinition(procdef)
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
        if(!procedure.isTask && askNestingLevel == 0 && !exempt) {
          val newVar = new _setprocedurevariable(new _procedurevariable(procedure.args.size, l.let.name))
          newVar.copyMetadataFrom(stmt.command)
          stmt.command = newVar
          alteredLets(procedure).put(l.let, procedure.args.size)
          procedure.localsCount += 1
          procedure.args :+= l.let.name
        }
        super.visitStatement(stmt)
      case r: _repeat =>
        if(!procedure.isTask && askNestingLevel == 0) {
          vn = procedure.args.size
          val newRepeat = new _repeatlocal(vn)
          newRepeat.copyMetadataFrom(stmt.command)
          stmt.command = newRepeat
          procedure.localsCount += 1
          // actual name here doesn't really matter, I don't think - ST 11/10/05
          procedure.args :+= "_repeatlocal:" + vn
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
        // it would be nice if the next line were easier to read - ST 2/6/11
        for(index <- alteredLets(procedure).get(l.let).orElse(Option(procedure.parent).flatMap(parent => alteredLets(parent).get(l.let)))) {
          val newVar = new _procedurevariable(index.intValue, l.let.name)
          newVar.copyMetadataFrom(expr.reporter)
          expr.reporter = newVar
        }
      case _ =>
    }
    super.visitReporterApp(expr)
  }

}
