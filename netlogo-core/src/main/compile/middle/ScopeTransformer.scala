// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import
  org.nlogo.{ core, nvm, prim },
    core.{ ClosedLet, Let, SourceLocation, Token, TokenHolder, TokenType },
    nvm.{ CompilerScoping, Scoping }

import
  org.nlogo.compile.{ api, prim => compileprim },
    api.{ AstFolder, AstTransformer, CommandBlock, ReporterApp, Statement }

class ScopeTransformer extends AstTransformer {
  override def visitStatement(stmt: Statement): Statement = {
    stmt.command match {
      case s: CompilerScoping if stmt.args.length > s.scopedBlockIndex =>
        stmt.args(s.scopedBlockIndex) match {
          case c: CommandBlock =>
            val introducedLets = IntroducedLetFolder.visitExpression(c)(using Set[Let]())
            val closedLets = ClosedLetFolder.visitExpression(c)(using Set[Let]())
            def tag[A <: TokenHolder](cmd: A): A = {
              val emptyToken =
                Token("", TokenType.Command, cmd.getClass.getName)(SourceLocation(c.start, c.start, c.filename))
              cmd.token_=(emptyToken)
              cmd
            }
            if ((closedLets & introducedLets).nonEmpty) {
              val enter = new Statement(
                tag(new compileprim._enterscope()),
                tag(new prim._enterscope()),
                Seq(),
                c.sourceLocation)
              val exit = new Statement(
                tag(new compileprim._exitscope()),
                tag(new prim._exitscope()),
                Seq(),
                c.sourceLocation)
              val newCmdBlock = c.copy(statements =
                c.statements.copy(stmts = (enter +: c.statements.stmts) :+ exit))
              val newStmt = stmt.copy(args = stmt.args.updated(s.scopedBlockIndex, newCmdBlock))
              super.visitStatement(newStmt)
            } else
              super.visitStatement(stmt)
          case _ => super.visitStatement(stmt)
        }
      case _ => super.visitStatement(stmt)
    }
  }
}

object IntroducedLetFolder extends AstFolder[Set[Let]] {
  override def visitStatement(stmt: Statement)(implicit lets: Set[Let]): Set[Let] = {
    stmt.command match {
      case l: prim._let => super.visitStatement(stmt)(using lets + l.let)
      case s: Scoping => lets
      case _ => super.visitStatement(stmt)
    }
  }
}

object ClosedLetFolder extends AstFolder[Set[Let]] {
  override def visitReporterApp(app: ReporterApp)(implicit lets: Set[Let]): Set[Let] = {
    app.reporter match {
      case l: prim._commandlambda =>
        super.visitReporterApp(app)(using
          lets ++ l.closedVariables.collect {
            case ClosedLet(let) => let
          })
      case l: prim._reporterlambda =>
        super.visitReporterApp(app)(using
          lets ++ l.closedVariables.collect {
            case ClosedLet(let) => let
          })
      case _ => super.visitReporterApp(app)
    }
  }
}
