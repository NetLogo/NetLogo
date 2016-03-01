// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.nvm.Instruction

/**
 * Fills in the source of all of Instructions in the Procedure.
 * Must be used *after* ArgumentStuffer.
 */
private class SourceTagger(sources: Map[String, String]) extends DefaultAstVisitor {

  override def visitStatement(stmt:Statement) {
    tagWithSource(stmt.command)
    super.visitStatement(stmt)
  }

  override def visitReporterApp(app:ReporterApp) {
    tagWithSource(app.reporter)
    super.visitReporterApp(app)
  }

  private def tagWithSource(i: Instruction): Unit = {
    val filename = i.getFilename
    if (filename != null) {
      val source = sources(i.getFilename)
      val sourceStart = i.getSourceStartPosition
      val sourceEnd = i.getSourceEndPosition
      i.source =
        if (sourceStart < 0 || sourceStart > sourceEnd || sourceEnd > source.length) ""
        else source.substring(sourceStart, sourceEnd)
    }
  }
}
