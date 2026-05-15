// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstFolder, ProcedureDefinition, ReporterApp, Statement }

object NoopFolder extends PositionalAstFolder[AstEdit]

case class RewriteContext(source: String, text: String = "", position: Int = 0) {
  def inserted(text: String): RewriteContext =
    RewriteContext(source, this.text + text, position)

  def through(position: Int, text: Option[String] = None): RewriteContext =
    RewriteContext(source, this.text + text.getOrElse(source.substring(this.position, position)), position)

  def throughEnd: RewriteContext =
    RewriteContext(source, text + source.substring(position))
}

class RewriteFolder extends AstFolder[RewriteContext] {
  override def visitProcedureDefinition(proc: ProcedureDefinition)(ctx: RewriteContext): RewriteContext =
    super.visitProcedureDefinition(proc)(ctx).through(proc.end)

  override def visitReporterApp(app: ReporterApp)(implicit ctx: RewriteContext): RewriteContext =
    ctx.through(app.end)
}

class RemoveVisitor(command: String) extends RewriteFolder {
  override def visitStatement(stmt: Statement)(implicit ctx: RewriteContext): RewriteContext = {
    if (stmt.command.token.text.equalsIgnoreCase(command)) {
      ctx.through(stmt.start).through(stmt.end, Some(""))
    } else {
      super.visitStatement(stmt)
    }
  }
}

class AddVisitor(command: String, addition: String) extends RewriteFolder {
  override def visitStatement(stmt: Statement)(implicit ctx: RewriteContext): RewriteContext = {
    if (stmt.command.token.text.equalsIgnoreCase(command)) {
      val args: Seq[String] = stmt.args.foldLeft(Seq[String]()) {
        case (acc, arg) =>
          acc :+ visitExpression(arg)(using RewriteContext(ctx.source, "", arg.start)).text
      }

      val formatted: String = "\\{(\\d)\\}".r.replaceAllIn(addition, m => args(m.group(1).toInt)) + {
        // if the existing command is the first command on its line, add the new command on the previous line for
        // cleaner formatting. but doing so when the target command is in the middle of its line might break the
        // execution order, so in that case insert it right before the existing command. (Isaac B 5/14/26)
        Option(ctx.source.substring(ctx.source.lastIndexOf("\n", stmt.start).max(0), stmt.start))
          .filter(_.trim.isEmpty).getOrElse(" ")
      }

      super.visitStatement(stmt)(using ctx.through(stmt.start).inserted(formatted))
    } else {
      super.visitStatement(stmt)
    }
  }
}

class ReplaceVisitor(primitive: String, replacement: String) extends RewriteFolder {
  override def visitReporterApp(app: ReporterApp)(implicit ctx: RewriteContext): RewriteContext = {
    if (app.reporter.token.text.equalsIgnoreCase(primitive)) {
      val args: Seq[String] = app.args.foldLeft(Seq[String]()) {
        case (acc, arg) =>
          acc :+ visitExpression(arg)(using RewriteContext(ctx.source, "", arg.start)).text
      }

      val formatted: String = "\\{(\\d)\\}".r.replaceAllIn(replacement, m => args(m.group(1).toInt))

      ctx.through(app.start).through(app.end, Some(formatted))
    } else {
      app.args.foldLeft(ctx.through(app.start)) {
        case (ctx, arg) =>
          visitExpression(arg)(using ctx)
      }.through(app.end)
    }
  }

  override def visitStatement(stmt: Statement)(implicit ctx: RewriteContext): RewriteContext = {
    if (stmt.command.token.text.equalsIgnoreCase(primitive)) {
      val args: Seq[String] = stmt.args.foldLeft(Seq[String]()) {
        case (acc, arg) =>
          acc :+ visitExpression(arg)(using RewriteContext(ctx.source, "", arg.start)).text
      }

      val formatted: String = "\\{(\\d)\\}".r.replaceAllIn(replacement, m => args(m.group(1).toInt))

      ctx.through(stmt.start).through(stmt.end, Some(formatted))
    } else {
      super.visitStatement(stmt)
    }
  }
}
