// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI

import org.nlogo.core.Model
import org.nlogo.api.{ ComponentSerialization, ModelFormat, ModelLoader, Version }
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion }

import scala.util.{ Failure, Success }

object OpenModel {
  class InvalidModelException(message: String) extends Exception(message)

  trait Controller {
    def errorOpeningURI(uri: URI, exception: Exception): Unit
    def errorAutoconvertingModel(result: FailedConversionResult): Boolean
    def invalidModel(uri: URI): Unit
    def invalidModelVersion(uri: URI, version: String): Unit
    def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean
    def shouldOpenModelOfUnknownVersion(version: String): Boolean
    def shouldOpenModelOfLegacyVersion(version: String): Boolean
  }

  def apply(uri: URI,
    controller: Controller,
    loader: ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
      val isValidURI = Option(uri)
        .flatMap(ModelLoader.getURIExtension)
        .map(ext => loader.formats.map(_.name).contains(ext))
        .getOrElse(false)
      if (! isValidURI) {
        controller.invalidModel(uri)
        None
      } else {
        loader.readModel(uri) match {
          case Success(model) =>
            if (! model.version.startsWith("NetLogo")) {
              controller.invalidModelVersion(uri, model.version)
              None
            } else if (shouldNotContinueOpeningModel(model, controller, currentVersion))
              None
            else
              modelConverter(model) match {
                case res: FailedConversionResult =>
                  if (controller.errorAutoconvertingModel(res)) Some(model)
                  else None
                case res => Some(res.model)
              }
          case Failure(exception: Exception) =>
            controller.errorOpeningURI(uri, exception)
            None
          case Failure(ex) => throw ex
        }
      }
  }

  private def shouldNotContinueOpeningModel(model: Model, controller: Controller, currentVersion: Version): Boolean = {
    val modelArity = if (Version.is3D(model.version)) 3 else 2
    val modelArityDiffers = Version.is3D(model.version) != currentVersion.is3D
    ((modelArityDiffers && ! controller.shouldOpenModelOfDifferingArity(modelArity, model.version)) ||
      (! currentVersion.knownVersion(model.version) && ! controller.shouldOpenModelOfUnknownVersion(model.version)) ||
      (! currentVersion.compatibleVersion(model.version) && ! controller.shouldOpenModelOfLegacyVersion(model.version)))
  }
}
