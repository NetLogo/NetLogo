// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import
  java.net.URI

import
  java.nio.file.{ Files, Path, Paths }

import
  javax.xml.transform.{ OutputKeys, TransformerFactory, stream },
    stream.{ StreamResult, StreamSource }

import
  org.nlogo.{ api, core, fileformat, nvm, workspace, xmllib },
    core.{ Femto, LiteralParser, Model },
    api.{ FileIO, LabProtocol, Version, Workspace },
    nvm.LabInterface.Settings,
    workspace.OpenModelFromURI,
    xmllib.{ ScalaXmlElement, ScalaXmlElementFactory }

import
  scala.util.{ Failure, Success }

import
  scala.io.Source

import
  scala.xml.XML

object BehaviorSpaceCoordinator {
  val ConversionStylesheetResource = "/system/behaviorspace-to-nlogox.xslt"

  private val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  private lazy val labFormat: fileformat.NLogoLabFormat =
    new fileformat.NLogoLabFormat(literalParser)

  private lazy val labXFormat: fileformat.NLogoXLabFormat =
    new fileformat.NLogoXLabFormat(ScalaXmlElementFactory)

  private def bsSection = labXFormat.componentName

  private def modelProtocols(m: Model): Option[Seq[LabProtocol]] =
    m.optionalSectionValue[Seq[LabProtocol]](bsSection)

  def selectProtocol(settings: Settings, workspace: Workspace): Option[(LabProtocol, Model, Option[Path])] = {
    val model = modelAtLocation(settings.modelLocation, settings.version, workspace)

    def loadFile(uri: URI): Option[Seq[LabProtocol]] =
      labXFormat.load(new ScalaXmlElement(XML.load(uri.toURL)), None)

    val additionalProtosAndNewLoadPath: Option[(Seq[LabProtocol], Option[Path])] =
      settings.externalXMLFile.flatMap(loadFile).map(protos => (protos, None)) orElse
        settings.externalXMLFile.flatMap { externalFile =>
          val tmpPath = Files.createTempFile("convertedSetupFile", ".xml")
          convertToNewFormat(Paths.get(externalFile), tmpPath)
          loadFile(tmpPath.toUri).map(protos => (protos, Some(tmpPath)))
        }

    val modelWithExtraProtocols =
      additionalProtosAndNewLoadPath
        .map(_._1)
        .map(additionalProtos => model.withOptionalSection(bsSection, Some(additionalProtos), Seq[LabProtocol]()))
        .getOrElse(model)

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
        proto           <- protos.headOption
      } yield proto

    for {
      selectedProto <- (namedProtocol orElse firstSetupFileProtocol)
      createdPath   = additionalProtosAndNewLoadPath.flatMap(_._2)
    } yield (selectedProto, model, createdPath)
  }

  private def modelAtLocation(location: URI, version: Version, workspace: Workspace): Model = {
    val allAutoConvertables = fileformat.defaultAutoConvertables :+
      Femto.scalaSingleton[org.nlogo.api.AutoConvertable]("org.nlogo.sdm.SDMAutoConvertable")
    val converter =
      fileformat.converter(workspace.getExtensionManager, workspace.getCompilationEnvironment, literalParser, allAutoConvertables) _
    val loader = fileformat.standardLoader(literalParser)
    val modelConverter = converter(workspace.world.program.dialect)

    OpenModelFromURI(location, HeadlessFileController, loader, modelConverter, version)
    loader.readModel(location) match {
      case Success(m) => m
      case Failure(e) => throw new Exception("Unable to open model at: " + location + ". " + e.getMessage)
    }
  }

  def protocolsFromModel(modelLocation: URI, version: Version, workspace: Workspace): Seq[LabProtocol] = {
    modelProtocols(modelAtLocation(modelLocation, version, workspace)).getOrElse(Seq[LabProtocol]())
  }

  def externalProtocols(path: String): Option[Seq[LabProtocol]] = {
    val fileSource = Source.fromFile(path).mkString
    labFormat.load(fileSource.lines.toArray, None)
  }

  def convertToNewFormat(oldPath: Path, newPath: Path): Unit = {
    Files.write(newPath, Files.readAllBytes(oldPath))
    val stylesource = new StreamSource(FileIO.getResourceAsString(ConversionStylesheetResource))
    stylesource.setSystemId(getClass.getResource(ConversionStylesheetResource).toString)
    val inputSource = new StreamSource(oldPath.toFile)
    val outputResult = new StreamResult(newPath.toFile)
    val transformer = TransformerFactory.newInstance.newTransformer(stylesource)
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    transformer.transform(inputSource, outputResult)
  }
}
