// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ Syntax, Token }
import org.nlogo.nvm.Instruction

/**
 * Fills in the source of all of Instructions in the Procedure.
 */
private class SourceTagger(sources: Map[String, String]) extends DefaultAstVisitor {
  var internalSources = Seq[String]()

  private def captureInternalSources(f: () => Unit): Seq[String] = {
    var tmpSources = internalSources
    internalSources = Seq[String]()
    f()
    val resultingSources = internalSources
    internalSources = tmpSources
    resultingSources
  }

  override def visitStatement(stmt:Statement) {
    val argSources = captureInternalSources(() => super.visitStatement(stmt))
    addInstructionPositions(stmt.command, stmt.command.token.start, stmt.command.token.end)
    instructionsOwnSource(stmt.command).foreach { src =>
      val fullSource = applicationSource(src, argSources, stmt.coreInstruction.syntax)
      internalSources = internalSources :+ fullSource
      stmt.command.source = src
      stmt.command.fullSource = fullSource
    }
  }

  override def visitReporterApp(app:ReporterApp) {
    val capturedSources = captureInternalSources(() => super.visitReporterApp(app))
    val ((start, end), prefix, argSources) = app.reporter match {
      case ct: org.nlogo.prim._commandtask  => ((ct.proc.pos, ct.proc.end), "task ", capturedSources)
      case ct: org.nlogo.prim._reportertask => ((app.start, app.end), "task ", Seq())
      case _                                =>
        val positions = Option(app.reporter.token).map(token => (token.start, token.end)).getOrElse((app.start, app.end))
        (positions, "", capturedSources)
    }
    addInstructionPositions(app.reporter, start, end)
    instructionsOwnSource(app.reporter).foreach { src =>
      val fullSource = prefix + applicationSource(src, argSources, app.coreInstruction.syntax)
      internalSources = internalSources :+ fullSource
      app.reporter.source = src
      app.reporter.fullSource = fullSource
    }
  }

  override def visitCommandBlock(blk: CommandBlock): Unit = {
    super.visitCommandBlock(blk)
    internalSources = internalSources.dropRight(1) :+ ("[ " + internalSources.last + " ]")
  }

  override def visitReporterBlock(blk: ReporterBlock): Unit = {
    super.visitReporterBlock(blk)
    internalSources = internalSources.dropRight(1) :+ ("[ " + internalSources.last + " ]")
  }

  private def addInstructionPositions(i: Instruction, start: Int, end: Int): Unit = {
    i.storedSourceStartPosition = start
    i.storedSourceEndPosition = end
  }

  private def instructionsOwnSource(i: Instruction): Option[String] = {
    val filename = i.getFilename
    val start = i.getSourceStartPosition
    val end   = i.getSourceEndPosition
    val validStartAndEnd = (start > 0 || start < end)
    if (validStartAndEnd) {
      Option(filename).map(sources.apply).flatMap { source =>
        if (end < source.length)
          Some(source.substring(start, end))
        else
          None
      }
    } else
      None
  }

  private def applicationSource(src: String, argSources: Seq[String], syntax: Syntax): String = {
    val syntaxComponents =
      if (syntax.isInfix)
        argSources.head +: (src +: argSources.tail)
      else
        src +: argSources
    if (syntax.isVariadic && argSources.length != syntax.dfault)
      syntaxComponents.mkString("(", " ", ")")
    else
      syntaxComponents.mkString(" ")
  }
}
