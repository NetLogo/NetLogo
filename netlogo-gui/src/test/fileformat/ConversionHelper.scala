// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.NetLogoLegacyDialect

import org.nlogo.core.{ Dialect, DummyCompilationEnvironment, DummyExtensionManager,
  Femto, LiteralParser, Model, SourceRewriter }

import scala.util.Try

trait ConversionHelper {
  val compilationEnvironment = FooCompilationEnvironment
  val extensionManager = VidExtensionManager

  val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def converter(conversions: Model => Seq[ConversionSet] = (_ => Seq())) = {
    def literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
    new ModelConverter(VidExtensionManager, FooCompilationEnvironment, literalParser, NetLogoLegacyDialect, defaultAutoConvertables, conversions)
  }

  def tryConvert(model: Model, conversions: ConversionSet*): ConversionResult =
    converter(_ => conversions)(model)

  def convert(model: Model, conversions: ConversionSet*): Model =
    tryConvert(model, conversions: _*).model

  def conversion(
    name: String = "test conversion",
    codeTabConversions:   Seq[SourceRewriter => String] = Seq(),
    otherCodeConversions: Seq[SourceRewriter => String] = Seq(),
    targets:              Seq[String] = Seq(),
    conversionDialect:    Dialect => Dialect = identity): ConversionSet =
      ConversionSet(conversionName = name, codeTabConversions = codeTabConversions, otherCodeConversions = otherCodeConversions, targets = targets, conversionDialect = conversionDialect)
}

object VidExtensionManager extends DummyExtensionManager {
  import org.nlogo.core.{ Syntax, Primitive, PrimitiveCommand, PrimitiveReporter}

  override def anyExtensionsLoaded = true
  override def importExtension(path: String, errors: org.nlogo.core.ErrorSource): Unit = { }
  override def replaceIdentifier(name: String): Primitive = {
    name match {
      case "VID:SAVE-RECORDING" =>
        new PrimitiveCommand { override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType)) }
      case "VID:RECORDER-STATUS" =>
        new PrimitiveReporter { override def getSyntax = Syntax.reporterSyntax(ret = Syntax.StringType) }
      case vid if vid.startsWith("VID") =>
        new PrimitiveCommand { override def getSyntax = Syntax.commandSyntax() }
      case _ => null
    }
  }
}

object FooCompilationEnvironment extends DummyCompilationEnvironment {
  import java.nio.file.Files
  override def resolvePath(filename: String): String = {
    val file = Files.createTempFile("foo", ".nls")
    Files.write(file, "to bar bk 1 end".getBytes)
    file.toString
  }
}
