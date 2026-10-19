package org.nlogo.ls

import java.awt.event.{ ActionEvent, ActionListener }
import java.lang.{ Double => JDouble }
import java.util
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JMenuItem

import org.nlogo.api.{ Argument, DefaultClassManager, ExtensionException, ExtensionManager, ImportErrorHandler,
                       LogoException, PrimitiveManager, Version }
import org.nlogo.app.{ App, ToolsMenu }
import org.nlogo.awt.EventQueue
import org.nlogo.core.LogoList
import org.nlogo.nvm.HaltException
import org.nlogo.workspace.AbstractWorkspace

import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala

object LevelSpace {
  var isHeadless = true

  @throws[ExtensionException]
  def castToId(id: Any): Int = id match {
    case number: Number => number.intValue
    case _ => throw new ExtensionException("Expected a model ID but got: " + id)
  }

  def checkSupportNetLogoVersion(): Unit = {
    // Due to changes in the tabs "API" of NetLogo desktop, LevelSpace will only work
    // with NetLogo 6.2.0 and newer.  But we want to release updates to the extensions
    // library that will include users on NetLogo 6.1.1 and 6.1.0, for whom this newer
    // LS will *not* work.  So we'll warn them they need to upgrade NetLogo or downgrade
    // LS, rather than just blowing up.  -Jeremy B June 2021
    // This has now been incremented to 7.0.0, due to API changes required for the GUI
    // redesign. (Isaac B 1/25/25)
    val versionErrorMessage =
      """This version of LevelSpace can only be used with NetLogo version 7.0.0 or newer.
      If you updated LevelSpace through the extensions manager, you can use it to uninstall
      the LevelSpace update and go back to the version that came with your NetLogo
      installation.  You can also upgrade NetLogo to the latest version to use this updated
      LevelSpace by going to the CCL website, https://ccl.northwestern.edu/netlogo."""
    def makeVersion(v: String): String = s"NetLogo${if (Version.is3D) " 3D " else " " }$v"
    val minSupportedVersion = Version.numericValue(makeVersion("7.0.0-beta0"))
    val minErrorVersion     = Version.numericValue(makeVersion("6.4.0"))
    val currentVersion      = Version.numericValue(Version.version)
    if (minErrorVersion < currentVersion && currentVersion < minSupportedVersion) {
      throw new ExtensionException(versionErrorMessage)
    }
  }
}

class LevelSpace extends DefaultClassManager { // This can be accessed by both the JobThread and EDT (when halting)
  LevelSpace.checkSupportNetLogoVersion()

  private var functionID: Option[Long] = None

  private def addSyncFunction(): Unit = {
    functionID = Some(App.app.addSyncFunction(() => syncTheme()))
  }

  final private val models = new ConcurrentHashMap[Integer, ChildModel].asScala
  // counter for keeping track of new models
  private var modelCounter = 0
  var letManager = new LetPrim
  // These need to be cleaned up on unload
  // ideally this would check isHeadless, but that is determined in runOnce, which isn't called
  // when the workspace is in testing mode and is therefore headless (Isaac B 7/25/25)
  private lazy val haltButton: Option[JMenuItem] = if (App.app == null)
    None
  else
    App.app.frame.getJMenuBar.getSubElements.collectFirst{case tm: ToolsMenu => tm.getItem(0)}

  private val haltListener: ActionListener = (_: ActionEvent) => haltChildModels()
  private var modelManager: LSModelManager = new HeadlessBackingModelManager

  @throws[ExtensionException]
  override def load(primitiveManager: PrimitiveManager): Unit = {
    primitiveManager.addPrimitive("let", letManager)
    primitiveManager.addPrimitive("ask", new Ask(this))
    primitiveManager.addPrimitive("of", new Of(this))
    primitiveManager.addPrimitive("report", new Report(this))
    primitiveManager.addPrimitive("with", new With(this))
    primitiveManager.addPrimitive("create-models", new CreateModels[HeadlessChildModel](this, createHeadlessModel))
    primitiveManager.addPrimitive("create-interactive-models", new CreateModels[GUIChildModel](this, createGUIModel))
    primitiveManager.addPrimitive("name-of", new Name(this))
    primitiveManager.addPrimitive("set-name", new SetName(this))
    primitiveManager.addPrimitive("close", new Close(this))
    primitiveManager.addPrimitive("models", new AllModels(this))
    primitiveManager.addPrimitive("model-exists?", new ModelExists(this))
    primitiveManager.addPrimitive("reset", new Reset(this))
    primitiveManager.addPrimitive("path-of", new Path(this))
    primitiveManager.addPrimitive("show", new Show(this))
    primitiveManager.addPrimitive("hide", new Hide(this))
    primitiveManager.addPrimitive("show-all", new ShowAll(this))
    primitiveManager.addPrimitive("hide-all", new HideAll(this))
    primitiveManager.addPrimitive("uses-level-space?", new UsesLS(this))
    primitiveManager.addPrimitive("random-seed", new RandomSeed(this))
    primitiveManager.addPrimitive("assign", new Assign(this))
  }

  def isMainModel(myEM: ExtensionManager): Boolean = myEM eq App.app.workspace.getExtensionManager

  @throws[ExtensionException]
  def getModel(id: Int): ChildModel =
    models.getOrElse(id, throw new ExtensionException("There is no model with ID " + id))

  def containsModel(id: Int): Boolean = models.contains(id)

  def modelList: Seq[Integer] = Seq(ArraySeq.unsafeWrapArray(models.keys.toArray.sorted)*)

  def numModels: Integer = models.size

  @throws[ExtensionException]
  override def unload(em: ExtensionManager): Unit = {
    if (!LevelSpace.isHeadless) {
      if (isMainModel(em)) {
        App.app.frame.getJMenuBar.remove(modelManager.guiComponent)
        functionID.foreach(App.app.removeSyncFunction)
      }

      haltButton.foreach(_.removeActionListener(haltListener))
    }
    try reset()
    catch {
      case _: HaltException =>
      // we can ignore this
    }
  }

  private def initModel(parentWS: AbstractWorkspace, model: ChildModel): Unit = {
    model.workspace.behaviorSpaceRunNumber(parentWS.behaviorSpaceRunNumber)
    model.workspace.behaviorSpaceExperimentName(parentWS.behaviorSpaceExperimentName)
    model.workspace.mainRNG.setSeed(parentWS.mainRNG.nextInt())
    models.put(modelCounter, model)
    modelCounter += 1
  }

  private def createModel(
    parentWS: AbstractWorkspace,
    path: String,
    modelCreator: (AbstractWorkspace, String) => ChildModel
  ): ChildModel = try {
    val model = modelCreator(parentWS, path)
    initModel(parentWS, model)
    model
  } catch {
    case e: Exception => throw new ExtensionException(e)
  }

  def createHeadlessModel(parentWS: AbstractWorkspace, path: String): ChildModel =
    createModel(parentWS, path, { (pws, p) =>
      new HeadlessChildModel(pws, p, modelCounter)
    })

  def createGUIModel(parentWS: AbstractWorkspace, path: String): ChildModel =
    if (LevelSpace.isHeadless || parentWS.behaviorSpaceRunNumber != 0) {
      createHeadlessModel(parentWS, path)
    } else {
      createModel(parentWS, path, { (pws, p) =>
        val model = new GUIChildModel(this, parentWS, path, modelCounter)
        model.setSpeed(App.app.workspace.updateManager.speed)
        model
      })
    }

  def updateModelMenu(): Unit = {
    if (!LevelSpace.isHeadless) {
      EventQueue.invokeLater { () =>
        modelManager.updateChildModels(models)
      }
    }
  }

  @throws[ExtensionException]
  @throws[HaltException]
  def reset(): Unit = {
    modelCounter = 0
    models.values.foreach(_.kill())
    models.clear()
  }

  @throws[LogoException]
  @throws[ExtensionException]
  def toModelList(arg: Argument): Seq[ChildModel] = {
    arg.get match {
      case x: JDouble => Seq(getModel(x.intValue))
      case l: LogoList => l.map(x => getModel(LevelSpace.castToId(x)))
      case _ => throw new ExtensionException("Expected a number or list")
    }
  }

  @throws[ExtensionException]
  @throws[HaltException]
  def closeModel(model: ChildModel): Unit = {
    model.kill()
    models.remove(model.modelID)
    updateModelMenu()
  }

  @throws[ExtensionException]
  override def importWorld(arg0: util.List[Array[String]], arg1: ExtensionManager, arg2: ImportErrorHandler): Unit = {
    // TODO
  }

  @throws[ExtensionException]
  override def runOnce(em: ExtensionManager): Unit = {
    LevelSpace.isHeadless = !em.workspaceContext.workspaceGUI

    if (!LevelSpace.isHeadless) {
      modelManager = new BackingModelManager
      // We need to actually listen for halt actions because gui child models can be running independently on their own
      // job threads if the user is interacting with them.
      haltButton.foreach(_.addActionListener(haltListener))
    }
    modelManager.updateChildModels(models)
    if (!LevelSpace.isHeadless && isMainModel(em)) {
      val menuBar = App.app.frame.getJMenuBar
      if (menuBar.getComponentIndex(modelManager.guiComponent) == -1) {
        menuBar.add(modelManager.guiComponent)
      }
      addSyncFunction()
    }
  }

  private def haltChildModels(): Unit = models.values.foreach(_.halt())

  def syncTheme(): Unit = {
    modelManager match {
      case guiManager: BackingModelManager =>
        guiManager.syncTheme()

      case _ =>
    }
  }
}
