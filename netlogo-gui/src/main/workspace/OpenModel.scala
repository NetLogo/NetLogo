// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI

import org.nlogo.core.Model
import org.nlogo.api.{ ComponentSerialization, ModelFormat, Version }

import scala.util.{ Failure, Success }

object OpenModel {
  class InvalidModelException(message: String) extends Exception(message)

  trait Controller {
    def errorOpeningURI(uri: URI, exception: Exception): Unit
    def invalidModel(uri: URI): Unit
    def invalidModelVersion(uri: URI, version: String): Unit
    def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean
    def shouldOpenModelOfUnknownVersion(version: String): Boolean
    def shouldOpenModelOfLegacyVersion(version: String): Boolean
  }

  def apply[A, B <: ModelFormat[A, B]](uri: URI,
    controller: Controller,
    format: B,
    currentVersion: Version,
    optionalSerializers: Seq[ComponentSerialization[A, B]]): Option[Model] = {
    if (uri == null || ! getFileExtension(uri).exists(_ == format.name)) {
      controller.invalidModel(uri)
      None
    } else {
      format.load(uri, optionalSerializers) match {
        case Failure(exception: Exception) =>
          controller.errorOpeningURI(uri, exception)
          None
        case Failure(throwable) => throw throwable
        case Success(model)     =>
          if (! model.version.startsWith("NetLogo")) {
            controller.invalidModelVersion(uri, model.version)
            None
          } else if (shouldNotContinueOpeningModel(model, controller, currentVersion))
            None
          else
            Some(model)
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
  private def getFileExtension(uri: URI): Option[String] = {
    uri.getPath.split("\\.").lastOption
  }
}
