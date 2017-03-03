// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI
import java.nio.file.Paths

import org.nlogo.core.Model
import org.nlogo.api.{ ModelLoader, Version }
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion }

import scala.util.{ Failure, Success, Try }

import scala.concurrent.{ ExecutionContext, Future }

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

  class CancelException extends RuntimeException
}

import OpenModel._

trait OpenModel[OpenParameter] {
  class InvalidModelException(message: String) extends Exception(message)

  def readModel(loader: ModelLoader, param: OpenParameter): Try[Model]

  def controllerExecutionContext: ExecutionContext

  def backgroundExecutionContext: ExecutionContext =
    NetLogoExecutionContext.backgroundExecutionContext

  def runOpenModelProcess(
    openParam: OpenParameter,
    uri: URI,
    controller: Controller,
    loader: ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Future[Model] = {
      implicit val ec = controllerExecutionContext
      val isValidURI = Future {
        Option(uri)
          .flatMap(ModelLoader.getURIExtension)
          .map(ext => loader.formats.map(_.name).contains(ext))
          .getOrElse(false)
      }(backgroundExecutionContext)

      def reportFailure(f: Controller => Unit): Future[Model] =
        Future {
          f(controller)
          throw new CancelException()
        }

      def invalidModel = reportFailure(_.invalidModel(uri))

      def errorReadingModel(exception: Exception) =
        reportFailure(_.errorOpeningURI(uri, exception))

      def invalidModelVersion(model: Model) =
        reportFailure(_.invalidModelVersion(uri, model.version))

      def modelFuture =
        Future { readModel(loader, openParam) }(backgroundExecutionContext)

      def checkForCancellation(model: Model) =
        Future {
          if (shouldCancelOpeningModel(model, controller, currentVersion))
            throw new CancelException()
          else
            model
        }

      def convertModel(model: Model): Future[Model] =
        Future { modelConverter(model, Paths.get(uri)) }(backgroundExecutionContext)
          .flatMap { r =>
            r match {
              case res: FailedConversionResult =>
                Future { controller.errorAutoconvertingModel(res).getOrElse(throw new CancelException()) }
              case res                         => Future(res.model)
            }
          }(backgroundExecutionContext)

      isValidURI.flatMap { isValid =>
        if (! isValid) invalidModel
        else
          modelFuture.flatMap {
            case Success(model: Model) if model.version.startsWith("NetLogo") =>
              checkForCancellation(model).flatMap(convertModel)(backgroundExecutionContext)
            case Success(model: Model)  => invalidModelVersion(model)
            case Failure(ex: Exception) => errorReadingModel(ex)
            case Failure(ex)            => throw ex
          }(backgroundExecutionContext)
      }(backgroundExecutionContext)
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

class OpenModelFromURI(val controllerExecutionContext: ExecutionContext)
  extends OpenModel[URI] {
  def readModel(loader: ModelLoader, uri: URI): Try[Model] =
    loader.readModel(uri)

  def apply(
    uri:            URI,
    controller:     Controller,
    loader:         ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Future[Model] = {
    runOpenModelProcess(uri, uri, controller, loader, modelConverter, currentVersion)
  }
}

class OpenModelFromSource(val controllerExecutionContext: ExecutionContext)
  extends OpenModel[(URI, String)] {
  def readModel(loader: ModelLoader, uriAndSource: (URI, String)): Try[Model] =
    loader.readModel(uriAndSource._2, uriAndSource._1.getPath.split("\\.").last)

  def apply(
    uri:            URI,
    source:         String,
    controller:     Controller,
    loader:         ModelLoader,
    modelConverter: ModelConversion,
    currentVersion: Version): Future[Model] = {
    runOpenModelProcess((uri, source), uri, controller, loader, modelConverter, currentVersion)
  }
}
