// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.{ Files, Paths }

import org.nlogo.api.NetLogoLegacyDialect

import org.nlogo.core.{ Dialect, DummyCompilationEnvironment, DummyExtensionManager,
  DummyLibraryManager, Femto, LiteralParser, Model, SourceRewriter }

trait ConversionHelper {

  val compilationEnvironment = FooCompilationEnvironment
  val extensionManager       = VidExtensionManager
  val libManager             = new DummyLibraryManager
  val canTestConversions     = NetLogoLegacyDialect.isAvailable
  val tempDir                = Files.createTempDirectory("ConversionTest")
  val modelPath              = Files.createTempFile(tempDir, "ConversionTest", ".nlogox") // this is only used for includes file testing

  val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def converter(conversions: Model => Seq[ConversionSet] = (_ => Seq())) =
    new ModelConverter( VidExtensionManager, libManager, FooCompilationEnvironment
                      , literalParser, NetLogoLegacyDialect, FileFormat.defaultAutoConvertables, conversions)

  def plotConverter =
    new PlotConverter( VidExtensionManager, libManager, FooCompilationEnvironment
                     , literalParser, NetLogoLegacyDialect, FileFormat.defaultAutoConvertables)

  def tryConvert(model: Model, conversions: ConversionSet*): ConversionResult =
    converter(_ => conversions)(model, modelPath)

  def convert(model: Model, conversions: ConversionSet*): Model =
    tryConvert(model, conversions*).model

  def writeNls(name: String, contents: String): Unit = {
    Files.write(tempDir.resolve(name), contents.getBytes)
  }

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
  override def getSource(filename: String): String = {
    val path = Paths.get(filename)
    if (Files.exists(path))
      new String(Files.readAllBytes(path))
    else super.getSource(filename)
  }
}
