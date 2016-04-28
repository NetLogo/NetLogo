// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.back

import org.nlogo.nvm.Instruction

import org.nlogo.compile.{ DefaultAstVisitor, ReporterApp, Statement }

/**
 * Fills in the source of all of Instructions in the Procedure.
 * Must be used *after* ArgumentStuffer.
 */
private class SourceTagger(source: String) extends DefaultAstVisitor {

  override def visitStatement(stmt:Statement) {
    tagWithSource(stmt.command)
    super.visitStatement(stmt)
  }

  override def visitReporterApp(app:ReporterApp) {
    tagWithSource(app.reporter)
    super.visitReporterApp(app)
  }

  private def tagWithSource(i: Instruction): Unit = {
    val sourceStart = i.sourceStartPosition
    val sourceEnd = i.sourceEndPosition
    i.source =
      if (sourceStart < 0 || sourceStart > sourceEnd || sourceEnd > source.length) ""
      else source.substring(sourceStart, sourceEnd)
  }
}
