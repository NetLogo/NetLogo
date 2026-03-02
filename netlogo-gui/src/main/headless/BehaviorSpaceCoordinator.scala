// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Paths

import org.nlogo.core.{ Femto, LiteralParser, Model }
import org.nlogo.api.LabProtocol
import org.nlogo.nvm.LabInterface.Settings
import org.nlogo.fileformat.FileFormat

import scala.io.Source
import scala.util.{ Failure, Success, Try }

object BehaviorSpaceCoordinator {
  private val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
  private val loader = FileFormat.standardAnyLoader(true, literalParser)

  private val bsSection = "org.nlogo.modelsection.behaviorspace"

  private def modelProtocols(m: Model): Option[Seq[LabProtocol]] =
    m.optionalSectionValue[Seq[LabProtocol]](bsSection)

  def selectProtocol(settings: Settings): Try[LabProtocol] = {
    val model = modelAtPath(settings.modelPath)

    val modelWithExtraProtocols =
      settings.externalXMLFile.map { file =>
        val source = Source.fromFile(file)
        val additionalProtos = loader.readExperiments(source.mkString, false, modelProtocols(model).fold(Set())(_.map(_.name).toSet)).toOption.map(_._1)
        source.close()
        model.withOptionalSection(bsSection, additionalProtos, Seq[LabProtocol]())
      }.getOrElse(model)

    settings.protocolName.map { name =>
      modelProtocols(modelWithExtraProtocols).flatMap(_.find(_.name == name))
        .fold(Failure(new Exception(s"The specified model does not contain the experiment \"$name\".")))(Success(_))
    }.orElse {
      settings.externalXMLFile.map { _ =>
        modelProtocols(modelWithExtraProtocols).flatMap(_.headOption)
          .fold(Failure(new Exception("Failed to parse specified setup file.")))(Success(_))
      }
    }.getOrElse(Failure(new IllegalArgumentException("You must specify either an experiment name or a setup file.")))
  }

  private def modelAtPath(path: String): Model = {
    loader.readModel(Paths.get(path).toUri) match {
      case Success(m) => m
      case Failure(e) => throw new Exception("Unable to open model at: " + path + ". " + e.getMessage)
    }
  }

  def protocolsFromModel(modelPath: String): Seq[LabProtocol] =
    modelProtocols(modelAtPath(modelPath)).getOrElse(Seq[LabProtocol]())

  def externalProtocols(path: String): Option[Seq[LabProtocol]] = {
    val source = Source.fromFile(path)
    val protocols = loader.readExperiments(source.mkString, false, Set())
    source.close()
    protocols.toOption.map(_._1)
  }
}
