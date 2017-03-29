// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{ Group, Scene }
import javafx.stage.Stage

import org.nlogo.api._
import org.nlogo.agent.{ World, World2D }
import org.nlogo.core.{ AgentKind, CompilerException, Dialect, I18N, LogoList, Model, Nobody,
  Shape, Token, Widget => CoreWidget }, Shape.{ LinkShape, VectorShape }
import org.nlogo.internalapi.ModelUpdate
import org.nlogo.fileformat, fileformat.{ ModelConversion, ModelConverter, NLogoFormat }
import org.nlogo.nvm.{ CompilerInterface, DefaultCompilerServices, PresentationCompilerInterface, Workspace }
import org.nlogo.util.Pico
import org.nlogo.workspace.{ AbstractWorkspace, AbstractWorkspaceScala, Controllable, CurrentModelOpener, HubNetManagerFactory, WorkspaceFactory }

import org.picocontainer.adapters.AbstractAdapter

class JavaFXApp extends Application {

  private val pico = new Pico()

  var threadPool: java.util.concurrent.ExecutorService = _

  var applicationController: ApplicationController = _

  var workspace: JFXGUIWorkspace = _

  var worldUpdates: java.util.concurrent.BlockingQueue[ModelUpdate] = _

  /**
   * Should be called once at startup to create the application and
   * start it running.  May not be called more than once.  Once
   * this method has called, the singleton instance of this class
   * is stored in <code>app</code>.
   *
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread.
   *
   * @param args Should be empty. (Passing non-empty arguments
   *             is not currently documented.)
   */
  override def init(): Unit = {
    AbstractWorkspace.isApp(true)
    AbstractWorkspace.isApplet(false)

    pico.add("org.nlogo.compile.Compiler")
    if (Version.is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")

    class ModelLoaderComponent extends AbstractAdapter[ModelLoader](classOf[ModelLoader], classOf[ConfigurableModelLoader]) {
      import scala.collection.JavaConverters._

      def getDescriptor(): String = "ModelLoaderComponent"
      def verify(x$1: org.picocontainer.PicoContainer): Unit = {}

      def getComponentInstance(container: org.picocontainer.PicoContainer, into: java.lang.reflect.Type) = {
        val compiler         = container.getComponent(classOf[PresentationCompilerInterface])
        val compilerServices = new DefaultCompilerServices(compiler)

        val loader =
          fileformat.standardLoader(compilerServices)
        val additionalComponents =
          container.getComponents(classOf[ComponentSerialization[Array[String], NLogoFormat]]).asScala
        if (additionalComponents.nonEmpty)
          additionalComponents.foldLeft(loader) {
            case (l, serialization) =>
              l.addSerializer[Array[String], NLogoFormat](serialization)
          }
        else
          loader
      }
    }

    pico.addAdapter(new ModelLoaderComponent())

    class ModelConverterComponent extends AbstractAdapter[ModelConversion](classOf[ModelConversion], classOf[ModelConverter]) {
      import scala.collection.JavaConverters._

      def getDescriptor(): String = "ModelConverterComponent"
      def verify(x$1: org.picocontainer.PicoContainer): Unit = {}

      def getComponentInstance(container: org.picocontainer.PicoContainer, into: java.lang.reflect.Type) = {
        val workspace = container.getComponent(classOf[org.nlogo.api.Workspace])

        val allAutoConvertables =
          fileformat.defaultAutoConvertables ++ container.getComponents(classOf[AutoConvertable]).asScala

        fileformat.converter(workspace.getExtensionManager, workspace.getCompilationEnvironment, workspace, allAutoConvertables)(container.getComponent(classOf[Dialect]))
      }
    }

    pico.addAdapter(new ModelConverterComponent())

    worldUpdates = new java.util.concurrent.LinkedBlockingQueue[ModelUpdate]()
    val world = new World2D()
    workspace = new JFXGUIWorkspace(world, pico.getComponent(classOf[PresentationCompilerInterface]), worldUpdates)
    world.compiler = workspace

    pico.addComponent(workspace)
    pico.addComponent(classOf[ModelSaver])
    pico.addComponent(classOf[JavaFXApp], this)

    threadPool = java.util.concurrent.Executors.newFixedThreadPool(2)
  }

  override def start(primaryStage: Stage): Unit = {
    import javafx.scene.layout.{ HBox, VBox }
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Application.fxml"))
    val vBox = loader.load().asInstanceOf[VBox]
    applicationController = loader.getController.asInstanceOf[ApplicationController]
    applicationController.modelLoader = pico.getComponent(classOf[ModelLoader])
    applicationController.modelConverter = pico.getComponent(classOf[ModelConverter])
    applicationController.executor = threadPool
    applicationController.workspace = workspace
    applicationController.worldUpdates = worldUpdates
    val scene = new Scene(vBox)
    primaryStage.setScene(scene)
    primaryStage.show()
  }

  override def stop(): Unit = {
    workspace.dispose()
    applicationController.dispose()
    threadPool.shutdown()
    if (! threadPool.awaitTermination(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
      threadPool.shutdownNow()
      System.exit(0)
    }
    javafx.application.Platform.exit()
  }
}
