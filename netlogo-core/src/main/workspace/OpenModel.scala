// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI
import java.nio.file.Paths

import org.nlogo.core.Model
import org.nlogo.api.{ AbstractModelLoader, Version }
import org.nlogo.fileformat.FailedConversionResult
import org.nlogo.fileformat.FileFormat.ModelConversion

import scala.util.{ Failure, Success, Try }

object OpenModel {
  trait Controller {
    def errorOpeningURI(uri: URI, exception: Exception): Unit
    // this callback returns either None (indicating cancellation) or the Model that should be opened
    def errorAutoconvertingModel(result: FailedConversionResult): Option[Model]
    def invalidModel(uri: URI): Unit
    def invalidModelVersion(uri: URI, version: String): Unit
    def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean
    def shouldOpenModelOfUnknownVersion(version: String): Boolean
    def shouldOpenModelOfLegacyVersion(version: String): Boolean
  }
}

import OpenModel.Controller

trait OpenModel[OpenParameter] {
  class InvalidModelException(message: String) extends Exception(message)

  def readModel(loader: AbstractModelLoader, param: OpenParameter): Try[Model]

  def runOpenModelProcess(
    openParam: OpenParameter,
    uri: URI,
    controller: Controller,
    loader: AbstractModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
      val isValidURI = Option(uri)
        .flatMap(AbstractModelLoader.getURIExtension)
        .map(ext => true)
        .getOrElse(false)
      if (! isValidURI) {
        controller.invalidModel(uri)
        None
      } else {
        readModel(loader, openParam) match {
          case Success(model) =>
            if (! model.version.startsWith("NetLogo")) {
              controller.invalidModelVersion(uri, model.version)
              None
            } else if (shouldCancelOpeningModel(model, controller, currentVersion))
              None
            else
              modelConverter(model, Paths.get(uri)) match {
                case res: FailedConversionResult => controller.errorAutoconvertingModel(res)
                case res                         => Some(res.model)
              }
          case Failure(exception: Exception) =>
            controller.errorOpeningURI(uri, exception)
            None
          case Failure(ex) => throw ex
        }
      }
  }

  private def shouldCancelOpeningModel(model: Model, controller: Controller, currentVersion: Version): Boolean = {
    val modelArity = if (Version.is3D(model.version)) 3 else 2
    val modelArityDiffers = Version.is3D(model.version) != currentVersion.is3D
    if (modelArityDiffers)
      ! controller.shouldOpenModelOfDifferingArity(modelArity, model.version)
    else if (! currentVersion.knownVersion(model.version))
      ! controller.shouldOpenModelOfUnknownVersion(model.version)
    else if (! currentVersion.compatibleVersion(model.version))
      ! controller.shouldOpenModelOfLegacyVersion(model.version)
    else
      false
  }
}

object OpenModelFromURI extends OpenModel[URI] {
  def readModel(loader: AbstractModelLoader, uri: URI): Try[Model] =
    loader.readModel(uri)

  def apply(
    uri:            URI,
    controller:     Controller,
    loader:         AbstractModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
    runOpenModelProcess(uri, uri, controller, loader, modelConverter, currentVersion)
  }
}

object OpenModelFromSource extends OpenModel[(URI, String)] {
  def readModel(loader: AbstractModelLoader, uriAndSource: (URI, String)): Try[Model] =
    loader.readModel(uriAndSource._2, uriAndSource._1.getPath.split("\\.").last)

  def apply(
    uri:            URI,
    source:         String,
    controller:     Controller,
    loader:         AbstractModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
    runOpenModelProcess((uri, source), uri, controller, loader, modelConverter, currentVersion)
  }
}
