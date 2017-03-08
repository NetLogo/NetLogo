// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import java.net.URI
import java.util.concurrent.Executor

import java.util.concurrent.BlockingQueue

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.canvas.Canvas
import javafx.scene.control.{ Alert, Button, ButtonType, MenuBar => JFXMenuBar , MenuItem, TabPane }
import javafx.scene.layout.{ AnchorPane, Pane }
import javafx.stage.{ FileChooser, Window }

import org.nlogo.javafx.{ CompileAll, GraphicsInterface, JavaFXExecutionContext, ModelInterfaceBuilder, OpenModelUI }
import org.nlogo.api.ModelLoader
import org.nlogo.agent.World
import org.nlogo.internalapi.ModelRunner
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.ModelConversion
import org.nlogo.workspace.{ AbstractWorkspaceScala, ConfigureWorld }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class ApplicationController extends ModelRunner {
  var executor: Executor = _
  var workspace: AbstractWorkspaceScala = _

  var modelLoader: ModelLoader = _
  var modelConverter: ModelConversion = _

  var worldUpdates: BlockingQueue[World] = _

  @FXML
  var openFile: MenuItem = _

  @FXML
  var menuBar: JFXMenuBar = _

  @FXML
  var interfaceArea: AnchorPane = _

  var widgetsByTag = Map.empty[String, Button]

  var interfacePane: Pane = _

  val timer = new java.util.Timer()

  def tagError(tag: String, error: Exception): Unit = {
    // empty implementation (for now!)
  }

  @FXML
  def initialize(): Unit = {
    openFile.setOnAction(new EventHandler[ActionEvent] {
      override def handle(a: ActionEvent): Unit = {
        val fileChooser = new FileChooser()
        fileChooser.setTitle("Select a NetLogo model")
        fileChooser.setInitialDirectory(new java.io.File(new java.io.File(System.getProperty("user.dir")).getParentFile, "models/Sample Models/Biology"))
        val selectedFile = Option(fileChooser.showOpenDialog(menuBar.getScene.getWindow))
        val executionContext = ExecutionContext.fromExecutor(executor, e => System.err.println("exception in background thread: " + e.getMessage))
        val openModelUI = new OpenModelUI(executionContext, menuBar.getScene.getWindow)
        selectedFile.foreach { file =>
          val openedModel = openModelUI(file.toURI, modelLoader, modelConverter)
            .map { m =>
              CompileAll(m, workspace)
            }(executionContext)
          openedModel.foreach {
            compiledModel =>
              ConfigureWorld(workspace, compiledModel)
          }(executionContext)
          openedModel.foreach {
            compiledModel =>
              val (interfaceWidgetsPane, widgetsMap) = ModelInterfaceBuilder.build(compiledModel, ApplicationController.this)
              interfacePane = interfaceWidgetsPane
              widgetsByTag = widgetsMap
              //TODO: add turtle and link shapes to workspace
              interfaceArea.getChildren.add(interfaceWidgetsPane)
          }(JavaFXExecutionContext)
        }
      }
    })
    /* start scheduling canvas updates */
    timer.schedule(scheduleRefresh, 1000)
  }

  def scheduleRefresh =
    new java.util.TimerTask {
      override def run(): Unit = {
        Platform.runLater(new Runnable() {
          override def run(): Unit = {
            refreshCanvas()
          }
        })
      }
    }

  import scala.collection.JavaConverters._

  def refreshCanvas(): Unit = {
    Option(worldUpdates.poll()).foreach { world =>
      interfacePane.getChildren().asScala.foreach {
        case c: Canvas =>
          val graphicsInterface = new GraphicsInterface(c.getGraphicsContext2D)
          val renderer = new org.nlogo.render.Renderer(workspace.world)
          val settings = new org.nlogo.api.ViewSettings {
            def fontSize: Int = 12
            // TODO: Why is this separate from world.patchSize?
            def patchSize: Double = world.patchSize
            def viewWidth: Double = c.getWidth
            def viewHeight: Double = c.getHeight
            def perspective: org.nlogo.api.Perspective = world.observer.perspective
            def viewOffsetX: Double = world.observer.followOffsetX
            def viewOffsetY: Double = world.observer.followOffsetY
            def drawSpotlight: Boolean = true
            def renderPerspective: Boolean = true
            def isHeadless: Boolean = false
          }
          renderer.paint(graphicsInterface, settings)
        case _ =>
      }
    }
    timer.schedule(scheduleRefresh, 1000)
  }

  def dispose(): Unit = {
    timer.cancel()
  }

}
