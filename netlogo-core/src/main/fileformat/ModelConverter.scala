// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.Path

import scala.util.{ Failure, Success, Try }
import scala.util.matching.Regex

import org.nlogo.api.{ AutoConverter, AutoConvertable, FileIO }
import org.nlogo.core.{ CompilationEnvironment, CompilationOperand, Dialect, ExtensionManager, Femto,
                        FrontEndInterface, LibraryManager, LiteralParser, Model, Program, SourceRewriter,
                        StructureDeclarations, StructureResults, VersionUtils },
  FrontEndInterface.ProceduresMap, StructureDeclarations.{ Declaration, Procedure }

import FileFormat.ModelConversion

object ModelConverter {
  def apply(
    extensionManager:       ExtensionManager,
    libManager:             LibraryManager,
    compilationEnvironment: CompilationEnvironment,
    literalParser:          LiteralParser,
    dialect:                Dialect,
    conversionSections:     Seq[AutoConvertable] = Seq()): ModelConversion = {
    val modelConversions = {(m: Model) =>
      AutoConversionList.conversions.collect {
        case (version, conversionSet) if VersionUtils.numericValue(m.version) < VersionUtils.numericValue(version) =>
          conversionSet
      }
    }
    new ModelConverter(extensionManager, libManager, compilationEnvironment, literalParser, dialect, conversionSections, modelConversions)
  }
}

class ModelConverter(
  extensionManager:      ExtensionManager,
  libManager:            LibraryManager,
  compilationEnv:        CompilationEnvironment,
  literalParser:         LiteralParser,
  baseDialect:           Dialect,
  components:            Seq[AutoConvertable],
  applicableConversions: Model => Seq[ConversionSet] = { _ => Seq() })
  extends ModelConversion {


  def apply(model: Model, modelPath: Path): ConversionResult = {
    def compilationOperand(source: String, program: Program, procedures: ProceduresMap): CompilationOperand = {
      CompilationOperand(
        sources                = Map("" -> source) ++ components.flatMap(_.conversionSource(model, literalParser)).toMap,
        extensionManager       = extensionManager,
        libraryManager         = libManager,
        compilationEnvironment =
          new CompilationEnvironment {
            def getSource(filename: String) = compilationEnv.getSource(filename)
            def profilingEnabled = compilationEnv.profilingEnabled
            def resolvePath(path: String): String =
              FileIO.resolvePath(path, modelPath).map(_.normalize.toString).getOrElse(path)
          },
        oldProcedures          = procedures,
        containingProgram      = program,
        subprogram             = false)
    }

    val tokenizer =
      Femto.scalaSingleton[org.nlogo.core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

    val frontEnd = Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")

    def rewriterOp(operand: CompilationOperand): SourceRewriter = {
      Femto.get[SourceRewriter]("org.nlogo.parse.AstRewriter", tokenizer, operand)
    }

    val aggregateConversionDialect = applicableConversions(model).map(_.conversionDialect)
      .foldLeft(baseDialect) { case (d, convDialect) => convDialect(d) }

    def rewriter(source: String, program: Program): SourceRewriter = {
      val operand = compilationOperand(source, program, FrontEndInterface.NoProcedures)
      val operandWithAuxSources = operand.copy(sources = operand.sources)
      rewriterOp(operandWithAuxSources)
    }

    def targetToRegexString(t: String): String = {
      if (t.length > 0 && t.head.isLetter && t.last.isLetter)
        "^[^;]*\\b" + Regex.quote(t) + "\\b"
      else
        Regex.quote(t)
    }

    def containsAnyTargets(targets: Seq[String])(source: String): Boolean = {
      val anyTarget =
        new Regex("(?mi)" + targets.map(targetToRegexString).mkString("|"))
      anyTarget.findFirstIn(source).isDefined
    }

    def requiresConversion(model: Model, targets: Seq[String], convertable: AutoConvertable): Boolean = {
      convertable.requiresAutoConversion(model, containsAnyTargets(targets))
    }

    def runConversion(conversionSet: ConversionSet, model: Model): ConversionResult = {
      import conversionSet._
      if (targets.nonEmpty && (containsAnyTargets(targets)(model.code) || components.exists(requiresConversion(model, targets, _))))
        applyConversion(conversionSet, model)
      else
        SuccessfulConversion(model, model)
    }

    def applyConversion(conversionSet: ConversionSet, model: Model): ConversionResult = {
      import conversionSet._

      lazy val convertedCodeTab: ConversionResult =
        Try {
          codeTabConversions.foldLeft(SuccessfulConversion(model, model)) {
            case (SuccessfulConversion(original, converted), conversion) =>
              val newProgram =
                Program.fromDialect(aggregateConversionDialect).copy(interfaceGlobals = model.interfaceGlobals)
              val newCode = conversion(rewriter(converted.code, newProgram))
              SuccessfulConversion(original, model.copy(code = newCode))
          }
        }.recover {
          case e: Exception => ErroredConversion(model, ConversionError(e, "code tab", conversionName))
        }.get

      def newStructure(code: String): Try[StructureResults] = {
        val newCompilation = compilationOperand(code,
          Program.fromDialect(aggregateConversionDialect).copy(interfaceGlobals = model.interfaceGlobals),
          FrontEndInterface.NoProcedures)

        Try {
          val (_, results) = frontEnd.frontEnd(newCompilation)
          results
        }
      }

      def modelWithConvertedComponents(conversionRes: ConversionResult): ConversionResult =
        newStructure(conversionRes.model.code) match {
          case Success(convertedStructure) =>
            val converter =
              new SnippetConverter( otherCodeConversions, containsAnyTargets(targets), rewriterOp
                                  , convertedStructure, extensionManager, libManager, compilationEnv)

            components.foldLeft(conversionRes) {
              case (res, component) =>
                res.mergeResult(
                  component.autoConvert(res.model, converter) match {
                    case Left((convertedModel, newExceptions)) =>
                      val error =
                        ConversionError(newExceptions, component.componentDescription, conversionName)
                      conversionRes.addError(error).updateModel(convertedModel)
                    case Right(convertedModel) => conversionRes.updateModel(convertedModel)
                  })
            }
          case Failure(e: Exception) => conversionRes.addError(ConversionError(e, "code tab", conversionName))
          case Failure(t) => throw t
        }

      modelWithConvertedComponents(convertedCodeTab)
    }

    val decls: Seq[Declaration] = frontEnd.findDeclarations(model.code, modelPath.getFileName.toString)

    val reordered: String = {
      if (decls.nonEmpty) {
        val source: String = model.code

        val sorted: Seq[Declaration] = decls.sortBy(_.start.start)

        val firstRange: (Declaration, String) = (sorted(0), source.substring(0, sorted(0).end.end))

        val ranges: Seq[(Declaration, String)] = sorted.sliding(2).foldLeft(Seq(firstRange)) {
          case (acc, Seq(one, two)) =>
            acc :+ (two, source.substring(one.end.end, two.end.end))

          case (acc, _) =>
            acc
        }

        val (valid, (invalid, procs)) = ranges.span(!_._1.isInstanceOf[Procedure]) match {
          case (valid, other) =>
            (valid, other.partition(!_._1.isInstanceOf[Procedure]))
        }

        (valid :++ invalid :++ procs).map(_._2).mkString + source.substring(sorted.last.end.end)
      } else {
        model.code
      }
    }

    val newModel: Model = model.copy(code = reordered)

    applicableConversions(newModel).foldLeft[ConversionResult](SuccessfulConversion(newModel, newModel)) {
      case (cr, conversion) =>
        cr.mergeResult(runConversion(conversion, cr.model))
    }
  }

  class SnippetConverter(otherCodeConversions: Seq[SourceRewriter => String],
    containsAnyTargets: String => Boolean,
    rewriter:           CompilationOperand => SourceRewriter,
    results:            StructureResults,
    extensionManager:   ExtensionManager,
    libManager:         LibraryManager,
    compilationEnv:     CompilationEnvironment)
    extends AutoConverter {

    def convertProcedure(procedure: String): String =
      convertWrappedSource(compilationOp)(procedure)

    def convertStatement(statement: String): String =
      convertUnwrappedSource(compilationOp, cmd, uncmd)(statement)

    def convertReporterProcedure(reporterProc: String): String =
      convertWrappedSource(compilationOp)(reporterProc)

    def convertReporterExpression(expression: String): String =
      convertUnwrappedSource(compilationOp, rep, unrep)(expression)

    def appliesToSource(source: String): Boolean =
      containsAnyTargets(source)

    private def rep(s: String): String = "to-report +++++ report " + s + " \nend"
    private def cmd(s: String): String = "to ------ " + s + " \nend"

    private def uncmd(s: String): String = s.stripPrefix("to ------").stripSuffix("\nend").trim
    private def unrep(s: String): String = s.stripPrefix("to-report +++++ report").stripSuffix("\nend").trim

    private def compilationOp(source: String): CompilationOperand =
      CompilationOperand(
        sources                = Map("" -> source),
        extensionManager       = extensionManager,
        libraryManager         = libManager,
        compilationEnvironment = compilationEnv,
        oldProcedures          = results.procedures,
        containingProgram      = results.program,
        subprogram             = true)

    private def convertWrappedSource(compilationOp: String => CompilationOperand)(source: String) = {
      otherCodeConversions.foldLeft(source) {
        case (src, conversion) => conversion(rewriter(compilationOp(src)))
      }
    }

    private def convertUnwrappedSource(compilationOp: String => CompilationOperand, wrap: String => String, unwrap: String => String)(
      source: String): String = {
        unwrap(convertWrappedSource(compilationOp)(wrap(source)))
    }
  }
}
