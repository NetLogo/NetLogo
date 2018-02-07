// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI
import java.nio.file.Paths

import org.nlogo.core.{ Model, View, Widget }
import org.nlogo.api.{ ModelLoader, Version, RichWorldDimensions },
  RichWorldDimensions._
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion }

import scala.util.{ Failure, Success, Try }

object OpenModel {
  sealed trait VersionResponse {
    def apply(m: Model, currentVersion: String): Option[Model]
  }
  case object CancelOpening extends VersionResponse {
    def apply(m: Model, currentVersion: String): Option[Model] = None
  }
  case object OpenInCurrentVersion extends VersionResponse {
    def apply(m: Model, currentVersion: String): Option[Model] = {
      val viewConversion =
        if (Version.is3D(currentVersion)) {(v: View) => v.copy(dimensions = v.dimensions.to3D)}
        else                              {(v: View) => v.copy(dimensions = v.dimensions.to2D)}
      Some(m.copy(version = currentVersion, widgets = m.widgets.map(convertView(viewConversion))))
    }
    private def convertView(viewConvert: View => View)(w: Widget): Widget = {
      w match {
        case v: View => viewConvert(v)
        case other => other
      }
    }
  }
  case object OpenAsSaved extends VersionResponse {
    def apply(m: Model, currentVersion: String): Option[Model] = Some(m)
  }

  trait Controller {
    def errorOpeningURI(uri: URI, exception: Exception): Unit
    // this callback returns either None (indicating cancellation) or the Model that should be opened
    def errorAutoconvertingModel(result: FailedConversionResult): Option[Model]
    def invalidModel(uri: URI): Unit
    def invalidModelVersion(uri: URI, version: String): Unit
    def shouldOpenModelOfDifferingArity(arity: Int, version: String): VersionResponse
    def shouldOpenModelOfUnknownVersion(currentVersion: String, openVersion: String): Boolean
    def shouldOpenModelOfLegacyVersion(currentVersion: String, openVersion: String): Boolean
  }
}

import OpenModel._

trait OpenModel[OpenParameter] {
  class InvalidModelException(message: String) extends Exception(message)

  def readModel(loader: ModelLoader, param: OpenParameter): Try[Model]

  def runOpenModelProcess(
    openParam: OpenParameter,
    uri: URI,
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
        readModel(loader, openParam) match {
          case Success(model) =>
            if (! model.version.startsWith("NetLogo")) {
              controller.invalidModelVersion(uri, model.version)
              None
            }
            shouldCancelFilter(model, controller, currentVersion).flatMap { model =>
              modelConverter(model, Paths.get(uri)) match {
                case res: FailedConversionResult => controller.errorAutoconvertingModel(res)
                case res                         => Some(res.model)
              }
            }
          case Failure(exception: Exception) =>
            controller.errorOpeningURI(uri, exception)
            None
          case Failure(ex) => throw ex
        }
      }
  }

  private def shouldCancelFilter(model: Model, controller: Controller, currentVersion: Version): Option[Model] = {
    val modelArity = if (Version.is3D(model.version)) 3 else 2
    val modelArityDiffers = Version.is3D(model.version) != currentVersion.is3D
    lazy val versionResponse =
      if (modelArityDiffers)
        Some(controller.shouldOpenModelOfDifferingArity(modelArity, model.version))
      else
        None

    if (versionResponse.isEmpty && ! currentVersion.knownVersion(model.version) &&
      ! controller.shouldOpenModelOfUnknownVersion(currentVersion.version, model.version))
      None
    else if (versionResponse.isEmpty && ! currentVersion.compatibleVersion(model.version) &&
      ! controller.shouldOpenModelOfLegacyVersion(currentVersion.version, model.version))
      None
    else
      if (modelArityDiffers) versionResponse.flatMap(_.apply(model, currentVersion.version))
      else Some(model)
  }
}

object OpenModelFromURI extends OpenModel[URI] {
  def readModel(loader: ModelLoader, uri: URI): Try[Model] =
    loader.readModel(uri)

  def apply(
    uri:            URI,
    controller:     Controller,
    loader:         ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
    runOpenModelProcess(uri, uri, controller, loader, modelConverter, currentVersion)
  }
}

object OpenModelFromSource extends OpenModel[(URI, String)] {
  def readModel(loader: ModelLoader, uriAndSource: (URI, String)): Try[Model] =
    loader.readModel(uriAndSource._2, uriAndSource._1.getPath.split("\\.").last)

  def apply(
    uri:            URI,
    source:         String,
    controller:     Controller,
    loader:         ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Option[Model] = {
    runOpenModelProcess((uri, source), uri, controller, loader, modelConverter, currentVersion)
  }
}
