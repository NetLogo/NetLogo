// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.middle

import org.nlogo.nvm.Procedure
import org.nlogo.prim.{ _ask, _askconcurrent, _repeat, _repeatinternal, _repeatlocal, _repeatlocalinternal }
import org.nlogo.compile.api.{ DefaultAstVisitor, ProcedureDefinition, Statement }

/**
 * Converts `repeat` to use local variables instead of let variables for holding the iteration
 * count. The parallel of LocalsVisitor, but used with `repeat` instead of `let`. */

private class RepeatVisitor extends DefaultAstVisitor {
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
      case r: _repeat =>
        if (! procedure.exists(_.isLambda) && askNestingLevel == 0) {
          procedure.foreach { proc =>
            vn = proc.args.size
            val newrepeat = new _repeatlocal(vn)
            newrepeat.copyMetadataFrom(stmt.command)
            stmt.command = newrepeat
            proc.localsCount += 1
            proc.size += 1
            // actual name here doesn't really matter, I don't think - ST 11/10/05
            proc.args :+= "_repeatlocal:" + vn
          }
          super.visitStatement(stmt)
        }
      case ri: _repeatinternal =>
        if (askNestingLevel == 0) {
          val newRepeat = new _repeatlocalinternal(vn, // vn from the _repeat we just saw
                                                  ri.offset)
          newRepeat.copyMetadataFrom(stmt.command)
          stmt.command = newRepeat
        }
        super.visitStatement(stmt)
      case _ => super.visitStatement(stmt)
    }
  }
}
