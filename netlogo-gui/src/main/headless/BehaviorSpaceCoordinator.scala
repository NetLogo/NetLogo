// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Paths

import org.nlogo.core.{ Femto, LiteralParser, Model }
import org.nlogo.api.{ LabProtocol, Version, Workspace }
import org.nlogo.nvm.LabInterface.Settings
import org.nlogo.workspace.OpenModelFromURI
import org.nlogo.fileformat.{ FileFormat, NLogoLabFormat }
import scala.util.{ Failure, Success }

import scala.io.Source

object BehaviorSpaceCoordinator {
  private val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  private lazy val labFormat: NLogoLabFormat =
    new NLogoLabFormat(literalParser)

  private def bsSection = labFormat.componentName

  private def modelProtocols(m: Model): Option[Seq[LabProtocol]] =
    m.optionalSectionValue[Seq[LabProtocol]](bsSection)

  def selectProtocol(settings: Settings, workspace: Workspace): Option[LabProtocol] = {
    val model = modelAtPath(settings.modelPath, workspace)

    val modelWithExtraProtocols =
      settings.externalXMLFile.map { file =>
        val loadableXML = Source.fromFile(file).mkString
        val additionalProtos = labFormat.load(loadableXML.linesIterator.toArray, None)
        model.withOptionalSection(bsSection, additionalProtos, Seq[LabProtocol]())
      }.getOrElse(model)

    val namedProtocol: Option[LabProtocol] =
      for {
        name   <- settings.protocolName
        protos <- modelProtocols(modelWithExtraProtocols)
        proto  <- protos.find(_.name == name)
      } yield proto

    lazy val firstSetupFileProtocol: Option[LabProtocol] =
      for {
        hasExternalFile <- settings.externalXMLFile
        protos          <- modelProtocols(modelWithExtraProtocols)
      } yield protos.head

    namedProtocol orElse firstSetupFileProtocol
  }

  private def modelAtPath(path: String, workspace: Workspace): Model = {
    val allAutoConvertables = FileFormat.defaultAutoConvertables :+
      Femto.scalaSingleton[org.nlogo.api.AutoConvertable]("org.nlogo.sdm.SDMAutoConvertable")
    val converter =
      FileFormat.converter(
        workspace.getExtensionManager
      , workspace.getLibraryManager
      , workspace.getCompilationEnvironment
      , literalParser
      , allAutoConvertables)
    val loader = FileFormat.standardAnyLoader(true, literalParser)
    val modelConverter = converter(workspace.world.program.dialect)

    OpenModelFromURI(Paths.get(path).toUri, HeadlessFileController, loader, modelConverter, Version)
    loader.readModel(Paths.get(path).toUri) match {
      case Success(m) => m
      case Failure(e) => throw new Exception("Unable to open model at: " + path + ". " + e.getMessage)
    }
  }

  def protocolsFromModel(modelPath: String, workspace: Workspace): Seq[LabProtocol] = {
    modelProtocols(modelAtPath(modelPath, workspace)).getOrElse(Seq[LabProtocol]())
  }

  def externalProtocols(path: String): Option[Seq[LabProtocol]] = {
    val fileSource = Source.fromFile(path).mkString
    labFormat.load(fileSource.linesIterator.toArray, None)
  }
}
