// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import java.net.URI
import java.util.concurrent.Executor

import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue }

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.canvas.Canvas
import javafx.scene.control.{ Alert, Button, ButtonType, MenuBar => JFXMenuBar , MenuItem, TabPane }
import javafx.scene.layout.{ AnchorPane, Pane }
import javafx.stage.{ FileChooser, Window }

import org.nlogo.javafx.{ ButtonControl, CompileAll, InterfaceArea, GraphicsInterface,
  JavaFXExecutionContext, ModelInterfaceBuilder, OpenModelUI , UpdateFilterThread }
import org.nlogo.api.ModelLoader
import org.nlogo.agent.World
import org.nlogo.internalapi.{ CompiledModel, ModelUpdate, SchedulerWorkspace, WorldUpdate }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.ModelConversion
import org.nlogo.workspace.{ AbstractWorkspaceScala, ConfigureWorld }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class ApplicationController {
  var executor: Executor = _
  var workspace: AbstractWorkspaceScala with SchedulerWorkspace = _

  var modelLoader: ModelLoader = _
  var modelConverter: ModelConversion = _

  def worldUpdates: BlockingQueue[ModelUpdate] = null

  // used to communicate between the job thread and the polling thread
  var filterThread: UpdateFilterThread = _

  def worldUpdates_=(updates: BlockingQueue[ModelUpdate]) = {
    if (filterThread != null) {
      filterThread.die()
    }
    filterThread = new UpdateFilterThread(updates, 40, () => processUpdates())
    filterThread.start()
  }

  @FXML
  var openFile: MenuItem = _

  @FXML
  var menuBar: JFXMenuBar = _

  @FXML
  var interfaceTabArea: AnchorPane = _

  var interfaceArea: InterfaceArea = _
  var compiledModel: CompiledModel = _

  var lastWorldTimestamp: Long = 0

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
          openedModel.map {
            compiledModel =>
              if (ApplicationController.this.compiledModel != null) {
                compiledModel.runnableModel.modelUnloaded()
              }
              ConfigureWorld(workspace, compiledModel)
              compiledModel
          }(executionContext).foreach {
            compiledModel =>
              ApplicationController.this.compiledModel = compiledModel
              val (interfaceWidgetsPane, widgetsMap) =
                ModelInterfaceBuilder.build(compiledModel)
              if (interfaceArea != null) {
                interfaceTabArea.getChildren.remove(interfaceArea)
              }
              interfaceArea = interfaceWidgetsPane
              interfaceTabArea.getChildren.add(interfaceWidgetsPane)
              AnchorPane.setTopAnchor(interfaceArea, 0.0)
              AnchorPane.setBottomAnchor(interfaceArea, 0.0)
              AnchorPane.setLeftAnchor(interfaceArea, 0.0)
              AnchorPane.setRightAnchor(interfaceArea, 0.0)
          }(JavaFXExecutionContext)
        }
      }
    })
  }


  class FakeViewSettings(canvas: Canvas, world: World) extends org.nlogo.api.ViewSettings {
    def fontSize: Int = 12
    // TODO: Why is this separate from world.patchSize?
    def patchSize: Double = world.patchSize
    def viewWidth: Double = canvas.getWidth
    def viewHeight: Double = canvas.getHeight
    def perspective: org.nlogo.api.Perspective = world.observer.perspective
    def viewOffsetX: Double = world.observer.followOffsetX
    def viewOffsetY: Double = world.observer.followOffsetY
    def drawSpotlight: Boolean = false
    def renderPerspective: Boolean = false
    def isHeadless: Boolean = false
  }

  def processUpdates(): Unit = {
    filterThread.filteredUpdates.poll() match {
      case WorldUpdate(world: World, _) =>
        interfaceArea.getViewCanvas.foreach { c =>
          val graphicsInterface = new GraphicsInterface(c.getGraphicsContext2D)
          val renderer = new org.nlogo.render.Renderer(world)
          val settings = new FakeViewSettings(c, world)
          renderer.paint(graphicsInterface, settings)
        }
        interfaceArea.ticks.setValue(world.ticks)
      case update if update != null =>
        Option(compiledModel).foreach { model =>
          model.runnableModel.notifyUpdate(update)
        }
      case _ =>
    }
    if (filterThread.filteredUpdates.peek != null) {
      Platform.runLater(new Runnable() {
        override def run(): Unit = {
          processUpdates()
        }
      })
    }
  }

  def dispose(): Unit = {
    filterThread.die()
  }
}
