// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Executor

import javafx.scene.control.{ Alert, ButtonType }
import javafx.stage.Window

import org.nlogo.api.{ ModelLoader, Version }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion }
import org.nlogo.workspace.OpenModel

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class OpenModelUI(executor: Executor, window: Window)
  extends OpenModel[URI]
  with OpenModel.Controller {

    class CancelException extends RuntimeException("canceled!")

  def controllerExecutionContext: ExecutionContext = JavaFXExecutionContext

  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    val alert = new Alert(Alert.AlertType.ERROR, I18N.gui.getN("file.open.error.unableToOpen"), ButtonType.CLOSE)
    alert.showAndWait()
  }
  // this callback returns either None (indicating cancellation) or the Model that should be opened
  def errorAutoconvertingModel(result: FailedConversionResult): Option[Model] = {
    val alert = new Alert(Alert.AlertType.ERROR, "auto-conversion failed on this model, resave in most recent NetLogo version and retry", ButtonType.CLOSE)
    alert.showAndWait()
    None
  }
  def invalidModel(uri: URI): Unit = notifyUserNotValidFile(uri)
  def invalidModelVersion(uri: URI, version: String): Unit = notifyUserNotValidFile(uri)
  def notifyUserNotValidFile(uri: URI): Unit = {
    val errorText = Try(Paths.get(uri))
      .toOption
      .map(path => I18N.gui.getN("file.open.error.invalidmodel.withPath", path.toString))
      .getOrElse(I18N.gui.get("file.open.error.invalidmodel"))
    val alert = new Alert(Alert.AlertType.ERROR, errorText, ButtonType.CLOSE)
    alert.showAndWait()
    throw new CancelException()
  }

  def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean = {
    val alert = new Alert(Alert.AlertType.ERROR, "Only NetLogo 2D models can be opened", ButtonType.CLOSE)
    alert.showAndWait()
    false
  }
  def shouldOpenModelOfUnknownVersion(version: String): Boolean = {
    val alert = new Alert(Alert.AlertType.ERROR, "Only models saved in most recent NetLogo version can be opened", ButtonType.CLOSE)
    alert.showAndWait()
    false
  }
  def shouldOpenModelOfLegacyVersion(version: String): Boolean = {
    val alert = new Alert(Alert.AlertType.ERROR, "Only models saved in most recent NetLogo version can be opened", ButtonType.CLOSE)
    alert.showAndWait()
    false
  }

  def readModel(loader: ModelLoader, uri: URI): Try[Model] =
    loader.readModel(uri)

  def apply(
    uri:            URI,
    loader:         ModelLoader,
    modelConverter: ModelConversion): Future[Model] = {
      runOpenModelProcess(uri, uri, this, loader, modelConverter, Version)
  }
}
