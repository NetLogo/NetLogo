// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import java.net.URI
import java.util.concurrent.Executor

import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.control.{ Alert, ButtonType, MenuBar => JFXMenuBar , MenuItem, TabPane }
import javafx.scene.layout.AnchorPane
import javafx.stage.{ FileChooser, Window }

import org.nlogo.javafx.{ JavaFXExecutionContext, OpenModelUI }
import org.nlogo.api.ModelLoader
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.ModelConversion

import scala.concurrent.Future
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
          openModelUI(file.toURI, modelLoader, modelConverter).foreach(m =>
            interfaceArea.getChildren.add(ModelInterfaceBuilder.build(m))
          )(JavaFXExecutionContext)
        }
      }
    })
  }
}
