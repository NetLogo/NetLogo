// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.middle

import org.nlogo.core.{ CompilationEnvironment, Syntax, Token }
import org.nlogo.nvm.Instruction
import org.nlogo.compile.api.{ DefaultAstVisitor, CommandBlock, ReporterApp, ReporterBlock, Statement }

import scala.util.Try

/**
 * Fills in the source of all of Instructions in the Procedure.
 */
private class SourceTagger(existingSources: Map[String, String], compilationEnvironment: CompilationEnvironment) extends DefaultAstVisitor {
  var sources: Map[String, String] = Map(existingSources.toSeq: _*)
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
    instructionsOwnSource(stmt.command, stmt.command.token.start, stmt.command.token.end).foreach { src =>
      val fullSource = applicationSource(src, argSources, stmt.coreInstruction.syntax)
      internalSources = internalSources :+ fullSource
      stmt.command.source = src
      stmt.command.fullSource = fullSource
    }
  }

  override def visitReporterApp(app:ReporterApp) {
    val capturedSources = captureInternalSources(() => super.visitReporterApp(app))
    val ((start, end), prefix, argSources) = app.reporter match {
      case ct: org.nlogo.prim._reporterlambda => ((app.start, app.end), "", Seq())
      case cl: org.nlogo.prim._commandlambda  => ((cl.proc.pos, cl.proc.end), "", Seq())
      case _                                  =>
        val positions = Option(app.reporter.token).map(token => (token.start, token.end)).getOrElse((app.start, app.end))
        (positions, "", capturedSources)
    }
    addInstructionPositions(app.reporter, start, end)
    instructionsOwnSource(app.reporter, start, end).foreach { src =>
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

  private def instructionsOwnSource(i: Instruction, start: Int, end: Int): Option[String] = {
    val filename = i.getFilename
    val validStartAndEnd = (start > 0 || start < end)
    if (validStartAndEnd) {
      if (filename != null && ! sources.isDefinedAt(filename)) {
        Try(compilationEnvironment.getSource(filename)).foreach { newSource =>
          sources = sources + (filename -> newSource)
        }
      }
      Option(filename).flatMap(sources.get).flatMap { source =>
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
