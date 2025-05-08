// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.middle

import org.nlogo.core
import org.nlogo.core.Syntax
import org.nlogo.core.Fail._
import org.nlogo.nvm.{ Referencer, ReferencerReporter }
import org.nlogo.compile.api.{ AstTransformer, Expression, ReporterApp, Statement }
import org.nlogo.prim.{ _extern, _externreport }

class ReferenceTransformer extends AstTransformer {
  override def visitStatement(stmt: Statement): Statement = {
    val newStmt = super.visitStatement(stmt)
    newStmt.command match {
      case cmd: Referencer =>
        newStmt.args(cmd.referenceIndex).asInstanceOf[ReporterApp].coreReporter match {
          case referenceable: core.Referenceable =>
            val args = newStmt.args
            val newArgs = args.slice(0, cmd.referenceIndex).toSeq ++
              args.slice(cmd.referenceIndex + 1 min args.length, args.length)
            newStmt.copy(command = cmd.applyReference(referenceable.makeReference), args = newArgs)
          case _ =>
            exception("Expected a variable name here.", stmt.args(cmd.referenceIndex))
        }
      case extern: _extern =>
        newStmt.coreCommand match {
          case core.prim._extern(syntax) if findReferenceIndex(syntax) != -1 =>
            val referenceIndex = findReferenceIndex(syntax)
            stmt.args(referenceIndex).asInstanceOf[ReporterApp].coreReporter match {
              case referenceable: core.Referenceable =>
                val constReference = refReporterApp(referenceable, referenceIndex, stmt.args)
                newStmt.copy(args = newStmt.args.updated(referenceIndex, constReference))
              case _ =>
                exception("Expected a variable name here.", stmt.args(referenceIndex))
            }
          // this represents the case where the org.nlogo.prim is an extern, but the core
          // prim is not an _extern, which shouldn't ever happen
          case _ => newStmt
        }
      case cmd => newStmt
    }
  }

  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    val newApp = super.visitReporterApp(app)
    newApp.reporter match {
      case rep: ReferencerReporter =>
        newApp.args(rep.referenceIndex).asInstanceOf[ReporterApp].coreReporter match {
          case referenceable: core.Referenceable =>
            val args = newApp.args
            val newArgs = args.slice(0, rep.referenceIndex).toSeq ++
              args.slice(rep.referenceIndex + 1 min args.length, args.length)
            newApp.copy(reporter = rep.applyReference(referenceable.makeReference), args = newArgs)
          case _ =>
            exception("Expected a variable name here.", app.args(rep.referenceIndex))
        }
      case externReport: _externreport =>
        newApp.coreReporter match {
          case core.prim._externreport(syntax) if findReferenceIndex(syntax) != -1 =>
            val referenceIndex = findReferenceIndex(syntax)
            newApp.args(referenceIndex).asInstanceOf[ReporterApp].coreReporter match {
              case referenceable: core.Referenceable =>
                val constReference = refReporterApp(referenceable, referenceIndex, newApp.args)
                newApp.copy(args = newApp.args.updated(referenceIndex, constReference))
              case _ =>
                exception("Expected a variable name here.", app.args(referenceIndex))
            }
          case _ => newApp
        }
      case _ => newApp
    }
  }

  private def findReferenceIndex(s: Syntax): Int = {
    if (s.isInfix && s.left == Syntax.ReferenceType) 0
    else if (! s.right.exists(_ == Syntax.ReferenceType)) -1
    else {
      s.right.indexWhere(_ == Syntax.ReferenceType) +
      (if (s.isInfix) 1 else 0)
    }
  }

  private def refReporterApp(
    referenceable: core.Referenceable & core.Reporter,
    referenceIndex: Int,
    args: Seq[Expression]): ReporterApp = {
    new ReporterApp(referenceable,
      new org.nlogo.prim._const(referenceable.makeReference),
      args(referenceIndex).sourceLocation)
  }
}
