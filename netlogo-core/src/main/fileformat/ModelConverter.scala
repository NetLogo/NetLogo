// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, Dialect, ExtensionManager, Femto,
  FrontEndInterface, Model, Program, SourceRewriter, StructureResults, Widget },
  FrontEndInterface.ProceduresMap

import org.nlogo.api.{ AutoConverter, AutoConvertable }

import scala.util.Try
import scala.util.matching.Regex

object ModelConverter {
  def apply(model:       Model,
    codeTabConversions:  Seq[SourceRewriter => String],
    allCodeConversions:  Seq[SourceRewriter => String],
    targets:             Seq[String],
    extensionManager:    ExtensionManager,
    compilationEnv:      CompilationEnvironment,
    components:          Seq[AutoConvertable],
    dialect:             Dialect): Try[Model] = {

    def compilationOperand(source: String, program: Program, procedures: ProceduresMap): CompilationOperand =
      CompilationOperand(
        sources                = Map("" -> source),
        extensionManager       = extensionManager,
        compilationEnvironment = compilationEnv,
        oldProcedures          = procedures,
        containingProgram      = program.copy(dialect = dialect),
        subprogram             = false)

    val tokenizer = Femto.scalaSingleton[org.nlogo.core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

    def rewriterOp(operand: CompilationOperand): SourceRewriter = {
      Femto.get[SourceRewriter]("org.nlogo.parse.AstRewriter", tokenizer, operand)
    }

    def rewriter(source: String, program: Program): SourceRewriter = {
      val operand = compilationOperand(source, program, FrontEndInterface.NoProcedures)
      rewriterOp(operand)
    }

    def containsAnyTargets(source: String): Boolean = {
      val anyTarget =
        new Regex("(?i)" + targets.map(Regex.quote).map("\\b" + _ + "\\b").mkString("|"))
      anyTarget.findFirstIn(source).isDefined
    }

    def requiresConversion(model: Model, convertable: AutoConvertable): Boolean = {
      convertable.requiresAutoConversion(model, containsAnyTargets _)
    }

    Try {
      if (targets.nonEmpty &&
        (containsAnyTargets(model.code) || components.exists(requiresConversion(model, _)))) {

          val code = codeTabConversions.foldLeft(model.code) {
            case (src, conversion) =>
              conversion(rewriter(src, Program.fromDialect(dialect).copy(interfaceGlobals = model.interfaceGlobals)))
          }

          val newCompilation = compilationOperand(code,
            Program.fromDialect(dialect).copy(interfaceGlobals = model.interfaceGlobals),
            FrontEndInterface.NoProcedures)

          val fe = Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")
          val (_, results) = fe.frontEnd(newCompilation)


          val converter = new ModelConverter(allCodeConversions, containsAnyTargets _, rewriterOp _, results, extensionManager, compilationEnv)

          // need to run optionalConversions even when no other code is changed...
          val convertedModel = model.copy(code = code)
          components.foldLeft(convertedModel) {
            case (m, component) => component.autoConvert(m, converter)
          }
        }
        else
          model
    }
  }

  class ModelConverter(allCodeConversions: Seq[SourceRewriter => String],
    containsAnyTargets: String => Boolean,
    rewriter: CompilationOperand => SourceRewriter,
    results: StructureResults,
    extensionManager: ExtensionManager,
    compilationEnv: CompilationEnvironment)
    extends AutoConverter {

    def convertProcedure(procedure: String): String =
      convertWrappedSource(compilationOp)(procedure)

    def convertStatement(statement: String): String =
      convertUnwrappedSource(compilationOp, cmd _, uncmd _)(statement)

    def convertReporterProcedure(reporterProc: String): String =
      convertWrappedSource(compilationOp)(reporterProc)

    def convertReporterExpression(expression: String): String =
      convertUnwrappedSource(compilationOp, rep _, unrep _)(expression)

    def appliesToSource(source: String): Boolean =
      containsAnyTargets(source)

    private def rep(s: String): String = "to-report +++++ report " + s + " end"
    private def cmd(s: String): String = "to ------ " + s + " end"

    private def uncmd(s: String): String = s.stripPrefix("to ------").stripSuffix("end").trim
    private def unrep(s: String): String = s.stripPrefix("to-report +++++ report").stripSuffix("end").trim

    private def compilationOp(source: String): CompilationOperand =
      CompilationOperand(
        sources                = Map("" -> source),
        extensionManager       = extensionManager,
        compilationEnvironment = compilationEnv,
        oldProcedures          = results.procedures,
        containingProgram      = results.program,
        subprogram             = true)

    private def convertWrappedSource(compilationOp: String => CompilationOperand)(source: String) = {
      allCodeConversions.foldLeft(source) {
        case (src, conversion) => conversion(rewriter(compilationOp(src)))
      }
    }

    private def convertUnwrappedSource(compilationOp: String => CompilationOperand, wrap: String => String, unwrap: String => String)(
      source: String): String = {
        unwrap(convertWrappedSource(compilationOp)(wrap(source)))
    }
  }
}
