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
 * it to the procedure's locals list. */

private class LocalsVisitor extends DefaultAstVisitor {

  private var procedure: Option[Procedure] = Option.empty[Procedure]
  private var localEligibility: Map[Let, Boolean] = Map()
  private var askNestingLevel = 0

  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    // this transformation only applies to non-lambdas at the moment
    if (! procdef.procedure.isLambda) {
      procedure = Some(procdef.procedure)
      localEligibility = new LocalEligibility().visitProcedureDefinition(procdef)((0, Map()))._2
      super.visitProcedureDefinition(procdef)
      procedure = None
    }
  }


  // a Let is local-eligible if its first appearance is at askNestingLevel 0
  // and it is not closed over by any lambdas.
  //
  // Q. Why does being inside an `ask` make a let local-ineligible?
  // A. This has to do with how the NetLogo Virtual machine operates.
  //    `ask` create its own context, and a let-turned-local-variable introduced in the context
  //    of `ask` shouldn't be allowed to appear within the parent context.
  // Q. Shouldn't this apply to all primitives which rely on context.runExclusiveJob and not
  //    just ask?
  // A. Probably so. I'm uncertain whether there are historical reasons why this
  //    hasn't been the case.
  // Q. What about `__let`?
  // A. Using `__let` instead of `let` indicates that this is a let we don't
  //    want converted to a local. This can be useful for testing. - ST 11/3/10, 2/6/11
  // Q. Why does being closed-over by a lambda make a let local-ineligible?
  // A. This is a small change here, but it has some pretty profound implications. If
  //    let-variables closed over by lambdas are changed to local variables, this means,
  //    for instance, that all lambdas within a loop will share the same let variable (a-la
  //    javascript). If the let isn't changed to a local, each lambda within a loop will get
  //    a fresh binding (a-la all the languages that make sense).
  class LocalEligibility extends AstFolder[(Int, Map[Let, Boolean])] {
    type Eligibility = (Int, Map[Let, Boolean])
    override def visitStatement(stmt: Statement)(implicit eligibility: Eligibility): Eligibility = {
      val (askNestingLevel, eligibilityMap) = eligibility
      stmt.command match {
        case _: _ask | _: _askconcurrent =>
          val (_, newMap) =
            super.visitStatement(stmt)((askNestingLevel + 1, eligibilityMap))
          (askNestingLevel, newMap)
        case l: _let if askNestingLevel == 0 && ! l.token.text.equalsIgnoreCase("__LET") =>
            val newEligibility = eligibilityMap.getOrElse(l.let, true)
            super.visitStatement(stmt)((askNestingLevel, eligibilityMap + (l.let -> newEligibility)))
        case _ => super.visitStatement(stmt)
      }
    }

    override def visitReporterApp(app: ReporterApp)(implicit eligibility: Eligibility): Eligibility = {
      val (askNestingLevel, eligibilityMap) = eligibility
      def addLets(m: Map[Let, Boolean], ls: Seq[Let]): Map[Let, Boolean] = {
        ls.foldLeft(m) {
          case (acc, let) => acc + (let -> false)
        }
      }
      app.reporter match {
        case cl: _commandlambda =>
          super.visitReporterApp(app)((askNestingLevel, addLets(eligibilityMap, cl.closedLets)))
        case rl: _reporterlambda =>
          super.visitReporterApp(app)((askNestingLevel, addLets(eligibilityMap, rl.closedLets)))
        case _ =>
          super.visitReporterApp(app)
      }
    }
  }

  override def visitStatement(stmt: Statement) {
    stmt.command match {
      case l: _let =>
        if (localEligibility.getOrElse(l.let, false))
          procedure.foreach { proc =>
            convertSetToLocal(stmt,  newProcedureVar(proc.args.size, l.let, None))
            proc.alteredLets.put(l.let, proc.args.size)
            proc.localsCount += 1
            proc.args :+= l.let.name
          }
        else
          for {
            proc <- procedure
            localIndex <- lookupLet(l.let, proc) } {
              convertSetToLocal(stmt, newProcedureVar(localIndex, l.let, None))
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
