// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ prim, Application, CommandBlock, Dump, LogoList },
    prim.{ _commandlambda, _const, _reporterlambda, Lambda }

import
  org.nlogo.core.{ AstTransformer, CompilationOperand, ReporterApp }

class SourceTagger(compilationOperand: CompilationOperand) extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case l: Lambda =>
        val newApp = super.visitReporterApp(app)
        val f = new Formatter()
        val source =
          f.visitReporterApp(newApp, AstPath())(using
            AstFormat("", Map(), Formatter.instructionString, new LambdaWhitespace(app)))
              .text.trim
        val newPrim = l match {
          case r: _reporterlambda => r.copy(source = Some(source))
          case c: _commandlambda  => c.copy(source = Some(source))
          case p => throw new IllegalStateException
        }
        newApp.copy(reporter = newPrim)
      case _ => super.visitReporterApp(app)
    }
  }
}

class LambdaWhitespace(startNode: ReporterApp) extends FormattingWhitespace {
  def get(path: AstPath, placement: WhiteSpace.Placement): Option[String] = None
  def backMargin(path: AstPath): String =
    path.components.lastOption match {
      case Some(AstPath.CmdBlk(b)) =>
        path.`../`.traverse(startNode) match {
          case Some(ReporterApp(c: _commandlambda, _, _)) => " "
          case other => " ]"
        }
      case Some(AstPath.RepBlk(i)) => " ]"
      case other => " "
    }
  def content(path: AstPath): String = {
    path.traverse(startNode) match {
      case Some(r: ReporterApp) =>
        r.reporter match {
          case _const(ll: LogoList) => Dump.logoObject(ll, true, false)
          case other => other.token.text
        }
      case other =>
        ""
    }
  }
  def frontMargin(path: AstPath): String = ""
  def leading(path: AstPath): String =
    path.components.lastOption match {
      case Some(AstPath.Stmt(_)) => " "
      case Some(AstPath.RepArg(_)) => " "
      case Some(AstPath.CmdBlk(i)) =>
        path.`../`.traverse(startNode) match {
          case Some(ReporterApp(c: _commandlambda, _, _)) => ""
          case Some(app: Application) =>
            app.args(i) match {
              case blk: CommandBlock if blk.synthetic => ""
              case _ => " ["
            }
          case other => " ["
        }
      case Some(AstPath.RepBlk(i)) => " ["
      case _ => ""
    }
  def trailing(path: AstPath): String = ""
}
