// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import java.net.URI
import java.util.concurrent.Executor

import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue }

import javafx.fxml.FXML
import javafx.event.ActionEvent
import javafx.scene.canvas.Canvas
import javafx.scene.control.{ Alert, Button, ButtonType, MenuBar => JFXMenuBar , MenuItem, TabPane }
import javafx.scene.layout.{ AnchorPane, Pane, VBox }
import javafx.stage.{ FileChooser, Window }

import org.nlogo.javafx.{ ButtonControl, CompileAll, InterfaceArea, GraphicsInterface,
  JavaFXExecutionContext, ModelInterfaceBuilder, OpenModelUI , UpdateFilterThread, Utils },
    Utils.{ changeListener, handler }
import org.nlogo.api.ModelLoader
import org.nlogo.agent.World
import org.nlogo.internalapi.{ CompiledModel, ModelUpdate, RequestUIAction,
  SchedulerWorkspace, ShowMessage, UIRequest, WorldUpdate, WritableGUIWorkspace }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.fileformat.ModelConversion
import org.nlogo.workspace.{ AbstractWorkspaceScala, ConfigureWorld }

import scala.collection.mutable.{ Map => MutableMap }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class ApplicationController {
  var executor: Executor = _
  var workspace: AbstractWorkspaceScala with SchedulerWorkspace with WritableGUIWorkspace = _

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
  var closeItem: MenuItem = _

  @FXML
  var haltItem: MenuItem = _

  @FXML
  var enterKioskMode: MenuItem = _

  @FXML
  var menuBar: JFXMenuBar = _

  @FXML
  var pane: VBox = _

  @FXML
  var interfaceTabArea: AnchorPane = _

  private var _interfaceArea: InterfaceArea = _

  val speedListener = changeListener { (newValue: Number) =>
    workspace.scheduledJobThread.setRunInterval(newValue.longValue)
  }

  def interfaceArea_=(i: InterfaceArea): Unit = {
    if (_interfaceArea != null) {
      interfaceTabArea.getChildren.remove(_interfaceArea)
      _interfaceArea.speedControl.foreverInterval.removeListener(speedListener)
    }
    _interfaceArea = i
    interfaceTabArea.getChildren.add(_interfaceArea)
    _interfaceArea.registerMouseEventSink(workspace)
    _interfaceArea.speedControl.foreverInterval
    workspace.scheduledJobThread.setRunInterval(_interfaceArea.speedControl.foreverInterval.get.toLong)
    _interfaceArea.speedControl.foreverInterval.addListener(speedListener)
  }

  def interfaceArea: InterfaceArea = _interfaceArea

  var compiledModel: CompiledModel = _

  var lastWorldTimestamp: Long = 0

  def tagError(tag: String, error: Exception): Unit = {
    // empty implementation (for now!)
  }

  @FXML
  def initialize(): Unit = {
    openFile.setOnAction(handler(openFile _))
    haltItem.setOnAction(handler(halt _))
    enterKioskMode.setOnAction(handler(activateKioskMode _))
  }

  private def openFile(a: ActionEvent): Unit = {
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Select a NetLogo model")
    fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")))
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
              compiledModel.modelUnloaded()
            }
            ConfigureWorld(workspace, compiledModel)
            compiledModel
        }(executionContext).foreach {
          compiledModel =>
            ApplicationController.this.compiledModel = compiledModel
            val interfaceWidgetsPane =
              ModelInterfaceBuilder.build(compiledModel)
            interfaceArea = interfaceWidgetsPane
            uiHooks.map(_._2._1).foreach(_.apply(interfaceArea))
            AnchorPane.setTopAnchor(interfaceArea, 0.0)
            AnchorPane.setBottomAnchor(interfaceArea, 0.0)
            AnchorPane.setLeftAnchor(interfaceArea, 0.0)
            AnchorPane.setRightAnchor(interfaceArea, 0.0)
        }(JavaFXExecutionContext)
    }
  }

  val uiHooks: MutableMap[String, (InterfaceArea => Unit, () => Unit)] =
    MutableMap.empty[String, (InterfaceArea => Unit, () => Unit)]

  def addHook(name: String, hookIn: InterfaceArea => Unit, hookOut: () => Unit): Unit = {
    uiHooks += ((name, (hookIn, hookOut)))
    if (interfaceArea != null)
      Utils.runLater(() => hookIn(interfaceArea))
  }

  def removeHook(name: String): Unit = {
    val hookOut = uiHooks.get(name).map(_._2).getOrElse({() => })
    Utils.runLater(() => hookOut())
    uiHooks -= name
  }

  private def halt(a: ActionEvent): Unit = {
    workspace.scheduledJobThread.halt()
  }

  private def activateKioskMode(a: ActionEvent): Unit = {
    // don't activate kiosk mode unless there's a model loaded, why would anyone
    // want a kiosk of NetLogo with no model?
    if (interfaceArea != null) {
      interfaceArea.activateKioskMode()
      pane.getChildren.remove(menuBar)
      menuBar.setVisible(false)
    }
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
          val settings = new FakeViewSettings(c.canvas, world)
          renderer.paint(graphicsInterface, settings)
        }
        interfaceArea.ticks.setValue(world.ticks)
      case RequestUIAction(UIRequest(display, continue)) =>
        display match {
          case ShowMessage(message) =>
            val alert = new Alert(Alert.AlertType.INFORMATION, message,
              ButtonType.OK,
              new ButtonType(I18N.gui.get("common.buttons.halt")))
            alert.setTitle("User Message")
            alert.setHeaderText("")
            val res = alert.showAndWait()
            if (! res.isPresent || (res.get == ButtonType.OK)) {
              val jobThread = workspace.scheduledJobThread
              val t = jobThread.createJob(continue)
              jobThread.queueTask(t)
            }
        }
      case update if update != null =>
        Option(compiledModel).foreach { model =>
          model.interfaceControl.notifyUpdate(update)
        }
      case _ =>
    }
    if (workspace != null && interfaceArea != null) {
      workspace.setMaxFPS(interfaceArea.speedControl.updatesPerSecond.getValue)
    }
    if (filterThread.filteredUpdates.peek != null) {
      Utils.runLater(processUpdates _)
    }
  }

  def dispose(): Unit = {
    filterThread.die()
  }
}
