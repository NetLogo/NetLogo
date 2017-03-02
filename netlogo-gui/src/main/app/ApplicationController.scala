// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Executor

import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.control.{ Alert, ButtonType, MenuBar => JFXMenuBar , MenuItem, TabPane }
import javafx.scene.layout.AnchorPane
import javafx.stage.{ FileChooser, Window }

import org.nlogo.api.{ ModelLoader, Version }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion }
import org.nlogo.workspace.OpenModel

import scala.util.Try

class ApplicationController {
  var executor: Executor = _

  var modelLoader: ModelLoader = _
  var modelConverter: ModelConversion = _

  @FXML
  var openFile: MenuItem = _

  @FXML
  var menuBar: JFXMenuBar = _

  @FXML
  var interfaceArea: AnchorPane = _

  @FXML
  def initialize(): Unit = {
    openFile.setOnAction(new EventHandler[ActionEvent] {
      override def handle(a: ActionEvent): Unit = {
        val fileChooser = new FileChooser()
        fileChooser.setTitle("Select a NetLogo model")
        fileChooser.setInitialDirectory(new java.io.File(new java.io.File(System.getProperty("user.dir")).getParentFile, "models/Sample Models/Biology"))
        val selectedFile = Option(fileChooser.showOpenDialog(menuBar.getScene.getWindow))
        val openModelUI = new OpenModelUI(executor, menuBar.getScene.getWindow)
        selectedFile.foreach { file =>
          openModelUI(file.toURI, modelLoader, modelConverter).foreach { m =>
            interfaceArea.getChildren.add(ModelInterfaceBuilder.build(m))
          }
        }
      }
    })
  }
}

class ReadModel(loader: ModelLoader, uri: URI) extends javafx.concurrent.Task[Try[Model]] {
  override protected def call(): Try[Model] =
    loader.readModel(uri)
}

class OpenModelUI(executor: Executor, window: Window)
  extends OpenModel[URI]
  with OpenModel.Controller {

    class CancelException extends RuntimeException("canceled!")

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

  def readModel(loader: ModelLoader, uri: URI): Try[Model] = {
    val task = new ReadModel(loader, uri)
    executor.execute(task)
    task.get // this is blocking :P, come back and fix this!
  }

  def apply(
    uri:            URI,
    loader:         ModelLoader,
    modelConverter: ModelConversion): Option[Model] = {
      try {
        runOpenModelProcess(uri, uri, this, loader, modelConverter, Version)
      } catch {
        case c: CancelException => None
      }
  }
}
