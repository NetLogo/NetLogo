// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ prim, Dump, LogoList },
    prim.{ _commandlambda, _const, _lambdavariable, _reporterlambda, Lambda }

import
  org.nlogo.core.{ AstFolder, AstTransformer, CompilationOperand,
    ProcedureDefinition, ReporterApp, SourceLocation, Statement }

class SourceTagger(compilationOperand: CompilationOperand) extends AstTransformer {
  private def getSource(sourceLocation: SourceLocation): String = {
    val sourceText =
      compilationOperand.sources.get(sourceLocation.filename).getOrElse {
        val env = compilationOperand.compilationEnvironment
        env.getSource(sourceLocation.filename)
      }
    sourceText.substring(sourceLocation.start, sourceLocation.end)
  }

  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case l: Lambda =>
        val newApp = super.visitReporterApp(app)
        val f = new Formatter()
        val source =
          f.visitReporterApp(newApp, AstPath())(
            AstFormat("", Map(), Formatter.instructionString _, new LambdaWhitespace(app)))
              .text.trim
        val newPrim = l match {
          case r: _reporterlambda => r.copy(source = Some(source))
          case c: _commandlambda  => c.copy(source = Some(source))
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
      case Some(AstPath.CmdBlk(_)) =>
        path.`../`.traverse(startNode) match {
          case Some(ReporterApp(c: _commandlambda, _, _)) => " "
          case _ => " ]"
        }
      case _ => " "
    }
  def content(path: AstPath): String =
    path.traverse(startNode) match {
      case Some(r: ReporterApp) =>
        r.reporter match {
          case _const(ll: LogoList) => Dump.logoObject(ll, true, false)
          case other => other.token.text
        }
      case other => ""
    }
  def frontMargin(path: AstPath): String = ""
  def leading(path: AstPath): String =
    path.components.lastOption match {
      case Some(AstPath.Stmt(_)) => " "
      case Some(AstPath.RepArg(_)) => " "
      case Some(AstPath.CmdBlk(_)) =>
        path.`../`.traverse(startNode) match {
          case Some(ReporterApp(c: _commandlambda, _, _)) => ""
          case other => " ["
        }
      case _ => ""
    }
  def trailing(path: AstPath): String = ""
}
