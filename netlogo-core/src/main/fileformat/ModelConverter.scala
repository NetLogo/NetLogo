// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ CompilationOperand, Femto, FrontEndInterface, Model, Program },
  FrontEndInterface.{ ProceduresMap, SourceRewriter }

import org.nlogo.core.{ Button, Monitor, Plot, Slider, Widget }
import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager }

import scala.util.Try
import scala.util.matching.Regex

object ModelConverter {
  def apply(model:      Model,
    compilationOperand: (String, Program, ProceduresMap) => CompilationOperand,
    codeTabConversions: Seq[SourceRewriter => String],
    widgetConversions:  Seq[SourceRewriter => String],
    targets:            Seq[String]): Try[Model] = {

    val tokenizer = Femto.scalaSingleton[org.nlogo.core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

    def rewriterOp(operand: CompilationOperand): SourceRewriter = {
      Femto.get[SourceRewriter]("org.nlogo.parse.AstRewriter", tokenizer, operand)
    }

    def rewriter(source: String, program: Program): SourceRewriter = {
      val operand =
        compilationOperand(source, program, FrontEndInterface.NoProcedures)
      rewriterOp(operand)
    }

    def containsAnyTargets(source: String): Boolean = {
      val anyTarget =
        new Regex("(?i)" + targets.map(Regex.quote).map("\\b" + _ + "\\b").mkString("|"))
      anyTarget.findFirstIn(source).isDefined
    }

    def rep(s: String): String = "to-report +++++ " + s + " end"
    def cmd(s: String): String = "to ------ " + s + " end"

    def uncmd(s: String): String = s.stripPrefix("to ------").stripSuffix("end").trim
    def unrep(s: String): String = s.stripPrefix("to-report +++++").stripSuffix("end").trim

    def widgetContainsTarget(w: Widget): Boolean =
      w match {
        case b: Button =>
          b.source.map(s => containsAnyTargets(cmd(s))) getOrElse false
        case s: Slider =>
          containsAnyTargets(rep(s.min)) || containsAnyTargets(rep(s.max)) || containsAnyTargets(rep(s.step))
        case m: Monitor =>
          m.source.map(s => containsAnyTargets(rep(s))) getOrElse false
        case p: Plot =>
          containsAnyTargets(cmd(p.setupCode)) || containsAnyTargets(cmd(p.updateCode)) ||
            p.pens.exists(pen => containsAnyTargets(cmd(pen.setupCode)) || containsAnyTargets(cmd(pen.updateCode)))
        case _ => false
      }

    def convertWidget(w: Widget, compilationOp: String => CompilationOperand): Widget = {
      def convertWidgetSource(wrap: String => String, unwrap: String => String)(source: String): String = {
        val converted = widgetConversions.foldLeft(wrap(source)) {
          case (src, conversion) => conversion(rewriterOp(compilationOp(src)))
        }
        unwrap(converted)
      }

      Try {
        w match {
          case cWidget@(_: Button | _: Plot) =>
            cWidget.convertSource(convertWidgetSource(cmd _, uncmd _))
          case mWidget@(_: Monitor | _: Slider) =>
            mWidget.convertSource(convertWidgetSource(rep _, unrep _))
          case _ => w
        }
      } getOrElse w
    }

    Try {
      if (targets.nonEmpty &&
        (containsAnyTargets(model.code) || model.widgets.exists(widgetContainsTarget))) {

        val code = codeTabConversions.foldLeft(model.code) {
          case (src, conversion) => conversion(rewriter(src, Program(interfaceGlobals = model.interfaceGlobals)))
        }

        val newCompilation = compilationOperand(
          code,
          Program(interfaceGlobals = model.interfaceGlobals),
          FrontEndInterface.NoProcedures)

        val fe = Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")
        val (_, results) = fe.frontEnd(newCompilation)

        def compilationOp(source: String): CompilationOperand =
          compilationOperand(source,
            results.program.copy(interfaceGlobals = model.interfaceGlobals),
            results.procedures)

        val widgets = model.widgets.map(w => convertWidget(w, compilationOp))
        model.copy(code = code, widgets = widgets)
      } else
        model
    }
  }
}
