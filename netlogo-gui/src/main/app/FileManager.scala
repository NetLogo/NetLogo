// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Container, FileDialog => AWTFileDialog, KeyboardFocusManager }
import java.io.{ File, IOException }
import java.net.{ URI, URISyntaxException }
import java.nio.file.Paths

import scala.util.{ Failure, Try }

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ I18N, Model }
import org.nlogo.api.{ AbstractModelLoader, Exceptions, FileIO, ModelReader, ModelType, Version },
  ModelReader.{ emptyModelPath, modelSuffix }
import org.nlogo.app.common.{ Actions, Dialogs, ExceptionCatchingAction }, Actions.Ellipsis
import org.nlogo.app.codetab.TemporaryCodeTab
import org.nlogo.app.tools.{ ModelsLibraryDialog, NetLogoWebSaver }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.fileformat.{ FailedConversionResult, SuccessfulConversion }
import org.nlogo.fileformat.FileFormat.ModelConversion
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionPane, UserAction }, UserAction.MenuAction
import org.nlogo.window.{ Events, FileController, GUIWorkspace, ReconfigureWorkspaceUI },
                          Events.{ AboutToCloseFilesEvent, AboutToQuitEvent, AboutToSaveModelEvent, LoadModelEvent,
                                   LoadErrorEvent, ModelSavedEvent, OpenModelEvent }
import org.nlogo.workspace.{ AbstractWorkspaceScala, OpenModel, OpenModelFromURI, OpenModelFromSource, SaveModel,
                             SaveModelAs }

object FileManager {
  class NewAction(manager: FileManager, parent: Container)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.new"), parent)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileOpenGroup
    rank        = 1
    accelerator = UserAction.KeyBindings.keystroke('N', withMenu = true)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      manager.aboutToCloseFiles()
      manager.newModel()
    }
  }

  class OpenAction(manager: FileManager, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.open") + Ellipsis, parent)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileOpenGroup
    rank        = 2
    accelerator = UserAction.KeyBindings.keystroke('O', withMenu = true)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      manager.aboutToCloseFiles()
      manager.openFromPath(manager.userChooseLoadPath(), ModelType.Normal)
    }
  }

  class QuitAction(manager: FileManager, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.quit"), parent)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = "Quit"
    accelerator = UserAction.KeyBindings.keystroke('Q', withMenu = true)

    override def action(): Unit = {
      try {
        manager.quit()
      } catch {
        case ex: UserCancelException => Exceptions.ignore(ex)
      }
    }
  }

  class ModelsLibraryAction(manager: FileManager, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.modelsLibrary"), parent)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileOpenGroup
    rank        = 3
    accelerator = UserAction.KeyBindings.keystroke('M', withMenu = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      manager.aboutToCloseFiles()
      ModelsLibraryDialog.open(frame,
      { sourceURI => manager.openFromURI(sourceURI, ModelType.Library) })
    }
  }

  class ImportClientAction(manager: FileManager, workspace: AbstractWorkspaceScala, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.import.hubNetClientInterface") + Ellipsis, parent)
  with MenuAction {
    category    = UserAction.FileCategory
    subcategory = UserAction.FileImportSubcategory

    var exception = Option.empty[IOException]

    def importTask(importPath: String, sectionChoice: Int): Runnable =
      new Runnable {
        def run(): Unit = {
          try {
            val uri = Paths.get(importPath).toUri
            manager.loadModel(manager.openModelURI(uri)).map(model =>
              workspace.getHubNetManager.foreach(_.importClientInterface(model, sectionChoice == 1)))
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val importPath = org.nlogo.swing.FileDialog.showFiles(
          parent, I18N.gui.get("menu.file.import.hubNetClientInterface"), java.awt.FileDialog.LOAD, null);
      val choice =
          new OptionPane(frame, I18N.gui.get("menu.file.import.hubNetClientInterface.message"),
                         I18N.gui.get("menu.file.import.hubNetClientInterface.prompt"),
                         Seq(I18N.gui.get("menu.file.import.hubNetClientInterface.fromInterface"),
                             I18N.gui.get("menu.file.import.hubNetClientInterface.fromClient")),
                         OptionPane.Icons.Question).getSelectedIndex

      if (choice != 2) {
        ModalProgressTask.onUIThread(
          frame,
          I18N.gui.get("dialog.interface.import.task"),
          importTask(importPath, choice))
        exception.foreach(throw _)
      }
    }
  }

  class SaveAsNetLogoWebAction(manager: FileManager, workspace: AbstractWorkspaceScala, modelSaver: ModelSaver, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAsNetLogoWeb"), parent)
  with MenuAction {
    category = UserAction.FileCategory
    group    = UserAction.FileShareGroup

    // disabled for 3-D since you can't do that in NetLogo Web - RG 9/10/15
    setEnabled(!Version.is3D)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.showFiles(
        parent, I18N.gui.get("menu.file.saveAsNetLogoWeb.dialog"),
        AWTFileDialog.SAVE, suggestedFileName)

      val exportFile = new File(exportPath)
      val saver = NetLogoWebSaver(exportPath)

      val modelString = modelToSave
      val includes = collectIncludes(modelString)

      if (includes.nonEmpty &&
        new OptionPane(parent, I18N.gui.get("common.messages.warning"),
                       I18N.gui.get("menu.file.nlw.prompt.includesWarning"), OptionPane.Options.OkCancel,
                       OptionPane.Icons.Warning).getSelectedIndex != 0)
        throw new UserCancelException()

      saver.save(modelString, exportFile.getName, includes)
    }

    @throws(classOf[UserCancelException])
    def suggestedFileName: String = {
      if (workspace.getModelType == ModelType.New) {
        manager.saveModel(false)
        val index = workspace.getModelFileName.lastIndexOf(".nlogo")
        if (index == -1)
          workspace.getModelFileName
        else
          workspace.getModelFileName.substring(0, index) + ".html"
      } else
        workspace.modelNameForDisplay + ".html"
    }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    def modelToSave: String = {
      if (doesNotMatchWorkingCopy && userWantsLastSaveExported())
        modelSaver.modelAsString(modelSaver.priorModel, "nlogox")
      else
        modelSaver.modelAsString(modelSaver.currentModel, "nlogox")
    }

    @throws(classOf[UserCancelException])
    private def userWantsLastSaveExported(): Boolean = {
      val typeKey =
        if (workspace.getModelType == ModelType.Normal) "fromSave" else "fromLibrary"
      val choice = new OptionPane(parent, I18N.gui.get("menu.file.nlw.prompt.title"),
                                  I18N.gui.get("menu.file.nlw.prompt.message." + typeKey),
                                  Seq(I18N.gui.get("menu.file.nlw.prompt." + typeKey),
                                      I18N.gui.get("menu.file.nlw.prompt.fromCurrentCopy")),
                                  OptionPane.Icons.Question).getSelectedIndex
      if (choice == 0)
        true
      else if (choice == 1)
        false
      else
        throw new UserCancelException()
    }

    // We compare last saved to current save here because dirtyMonitor doesn't
    // report if UI values (sliders, etc.) have been changed - RG 9/10/15
    private def doesNotMatchWorkingCopy: Boolean = {
      modelSaver.priorModel != modelSaver.currentModel
    }

    @throws(classOf[IOException])
    private def collectIncludes(str: String): Seq[(String, String)] = {
      val includes = workspace.compiler.findIncludes(workspace.getModelPath, str, workspace.getCompilationEnvironment)

      if (includes.isEmpty)
        return Nil

      includes.get.flatMap({ case (name, path) =>
        val file = scala.io.Source.fromFile(path)
        val source = file.mkString

        file.close()

        (name, source) +: collectIncludes(source)
      }).toList
    }
  }

  class ConvertNlsAction(
    tab:                TemporaryCodeTab,
    modelSaver:         ModelSaver,
    convertIncludeFile: ConvertIncludeFile,
    workspace:          AbstractWorkspaceScala,
    controller:         FileController)
  extends ExceptionCatchingAction(I18N.gui.get("menu.edit.convertToNetLogoSix"), tab)
  with MenuAction{
    category = UserAction.EditCategory
    group    = "ConversionGroup"

    override def action(): Unit = {
      for {
        name <- tab.filename.toOption
        path <- FileIO.resolvePath(name, Paths.get(workspace.getModelPath))
      } {
        convertIncludeFile.apply(path, modelSaver.currentModel, tab.innerSource) match {
          case SuccessfulConversion(originalModel, m) => tab.innerSource = m.code
          case failure: FailedConversionResult =>
            controller.showAutoconversionError(failure, "nls").foreach { m =>
              tab.innerSource = m.code
            }
        }
      }
    }
  }

  class ManageResourcesAction(manager: FileManager, workspace: AbstractWorkspaceScala, parent: Component)
    extends ExceptionCatchingAction(I18N.gui.get("menu.file.manageResources"), parent) with MenuAction {

    category = UserAction.FileCategory
    group = UserAction.FileResourcesGroup
    rank = 1

    override def action(): Unit = {
      new ResourceManagerDialog(workspace.asInstanceOf[GUIWorkspace].getFrame, workspace).setVisible(true)
    }
  }

}

import FileManager._

/** This class manages a number of file operations. Much of the code in here used to live in
 *  fileMenu, but it's obviously undesirable to couple the behavior in this class too closely to
 *  its presentation (the menu) */
class FileManager(workspace: AbstractWorkspaceScala,
  modelLoader: AbstractModelLoader,
  modelConverter: ModelConversion,
  dirtyMonitor: DirtyMonitor,
  modelSaver: ModelSaver,
  eventRaiser: AnyRef,
  parent: Container,
  tabManager: TabManager)
    extends OpenModelEvent.Handler
    with LoadModelEvent.Handler {
  private var firstLoad: Boolean = true

  val controller = new FileController(parent, workspace)

  def handle(e: OpenModelEvent): Unit = {
    openFromPath(e.path, ModelType.Library, e.shouldAutoInstallLibs)
  }

  def handle(e: LoadModelEvent): Unit = {
    modelSaver.setCurrentModel(e.model)
  }

  private[app] def aboutToCloseFiles(): Unit = {
    if (dirtyMonitor.modelDirty) {
      if (Dialogs.userWantsToSaveFirst(I18N.gui.get("file.save.offer.thisModel"), parent)) {
        saveModel(false)
      } else {
        dirtyMonitor.discardNewAutoSaves()
      }
    }

    new AboutToCloseFilesEvent().raise(eventRaiser)
  }

  @throws(classOf[UserCancelException])
  def quit(): Unit = {
    aboutToCloseFiles()
    ModelConfig.pruneAutoSaves()
    new AboutToQuitEvent().raise(eventRaiser)
    workspace.getExtensionManager.reset()
    Analytics.appExit()
    System.exit(0)
  }

  /**
   * opens a model from a file path.
   */
  def openFromPath(path: String, modelType: ModelType, shouldAutoInstallLibs: Boolean = false): Unit = {
    openFromURI(new File(path).toURI, modelType, shouldAutoInstallLibs)
  }

  def openFromURI(uri: URI, modelType: ModelType, shouldAutoInstallLibs: Boolean = false): Unit = {
    var autosaveFound = false

    val newUri = ModelConfig.findAutoSave(Paths.get(uri).toString) match {
      case Some(path) =>
        autosaveFound = true

        if (new OptionPane(parent, I18N.gui.get("file.autosave.recover"), I18N.gui.get("file.autosave.recover.message"),
                           OptionPane.Options.YesNo, OptionPane.Icons.Info).getSelectedIndex == 0) {
          path.toUri
        } else {
          uri
        }

      case _ => uri
    }

    loadModel(openModelURI(newUri)).foreach { m =>
      openFromModel(m, uri, modelType, shouldAutoInstallLibs)

      if (modelType == ModelType.Normal && autosaveFound) {
        saveModel(false)
      } else {
        dirtyMonitor.discardNewAutoSaves()
      }
    }
  }

  private def openModelURI(uri: URI): (OpenModel.Controller) => Option[Model] =
    ((fileController: OpenModel.Controller) => OpenModelFromURI(uri, fileController, modelLoader, modelConverter, Version))

  def openFromSource(uri: URI, modelSource: String, modelType: ModelType): Unit = {
    loadModel((fileController: OpenModel.Controller) =>
      OpenModelFromSource(uri, modelSource, fileController, modelLoader, modelConverter, Version))
        .foreach(m => openFromModel(m, uri, modelType))
  }

  /**
   * Opens a model from the Model object.
   * This must be called from the Swing event dispatch thread.
   */
  def openModel(uri: URI, model: Model, modelType: ModelType): Unit = {
    openFromModel(model, uri, modelType)
  }

  private def runLoad( linkParent: Container, uri: URI, model: Model, modelType: ModelType
                     , shouldAutoInstallLibs: Boolean): Unit = {
    val convertWidgetSizes = {
      if (Version.numericValue(model.version) > Version.numericValue("NetLogo 6.4.0")) {
        false
      } else {
        new OptionPane(parent, I18N.gui.get("menu.tools.convertWidgetSizes"),
                      I18N.gui.get("file.open.warn.convertWidgetSizes"),
                      Seq(I18N.gui.get("menu.tools.convertWidgetSizes.resizeAndAdjust"),
                          I18N.gui.get("file.open.skip")), OptionPane.Icons.Info)
          .getSelectedIndex == 0
      }
    }

    ReconfigureWorkspaceUI(linkParent, uri, modelType, model, workspace, shouldAutoInstallLibs, convertWidgetSizes)
  }

  private def loadModel(openModel: (OpenModel.Controller) => Option[Model]): Option[Model] = {
    val result = ModalProgressTask.runForResultOnBackgroundThread(
      Hierarchy.getFrame(parent), I18N.gui.get("dialog.interface.loading.task"), () => {},
      Unit => openModel(controller))
    if (result.isEmpty) {
      new LoadErrorEvent().raise(eventRaiser)
    }
    result
  }

  @throws(classOf[UserCancelException])
  @throws(classOf[IOException])
  def newModel(): Unit = {
    try {
      openFromModel(modelLoader.emptyModel(modelSuffix), getClass.getResource(emptyModelPath).toURI, ModelType.New)
    } catch  {
      case ex: URISyntaxException =>
        println("Unable to locate empty model: " + emptyModelPath)
    }
  }

  @throws(classOf[UserCancelException])
  private[app] def saveModel(saveAs: Boolean): Unit = {
    val isNew = workspace.getModelPath == null

    val saveThunk = {
      val newFormat =
        workspace.getModelFileName != null && (workspace.getModelFileName.endsWith(".nlogox") ||
                                               workspace.getModelFileName.endsWith(".nlogox3d"))
      val saveModel = if (saveAs || !newFormat) SaveModelAs else SaveModel
      saveModel(currentModel, modelLoader, controller, workspace, Version)
    }

    // if there's no thunk, the user canceled the save
    saveThunk.foreach { thunk =>
      val saver = new Saver(thunk, isNew)
      val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()
      val tempParent = if (focusOwner == null) parent else focusOwner

      ModalProgressTask.onUIThread(Hierarchy.getFrame(tempParent),
        I18N.gui.get("dialog.interface.saving.task"), saver)

      if (! saver.result.isDefined)
        throw new UserCancelException()

      saver.result match {
        case Some(Failure(e: Throwable)) =>
          new OptionPane(parent, I18N.gui.get("common.netlogo"), I18N.gui.getN("menu.file.save.error", e.getMessage),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        case _ =>
          ModelConfig.setLastModified(workspace.getModelPath)
      }
    }
  }

  private def openFromModel( model: Model, uri: URI, modelType: ModelType
                           , shouldAutoInstallLibs: Boolean = false): Unit = {
    if (firstLoad) {
      firstLoad = false
      runLoad(parent, uri, model, modelType, shouldAutoInstallLibs)
    } else {
      val loader = new Runnable {
        override def run(): Unit = {
          runLoad(parent, uri, model, modelType, shouldAutoInstallLibs)
        }
      }

      ModalProgressTask.onUIThread(Hierarchy.getFrame(parent), I18N.gui.get("dialog.interface.loading.task"), loader)
    }
  }

  @throws(classOf[UserCancelException])
  private def userChooseLoadPath(): String = {
    FileDialog.showFiles(parent, I18N.gui.get("menu.file.open"), AWTFileDialog.LOAD, null)
  }

  private class Saver(val thunk: () => Try[URI], isNew: Boolean) extends Runnable {
    var result = Option.empty[Try[URI]]

    def run(): Unit = {
      new AboutToSaveModelEvent().raise(eventRaiser)
      val r = thunk()
      r.foreach { uri =>
        val path = Paths.get(uri).toString
        modelSaver.setCurrentModel(modelSaver.currentModel.copy(version = Version.version))
        new ModelSavedEvent(path, isNew).raise(eventRaiser)
      }
      result = Some(r)
    }
  }

  def currentModel: Model = modelSaver.currentModel

  def actions: Seq[MenuAction] = Seq(
    new NewAction(this, parent),
    new OpenAction(this, parent),
    new QuitAction(this, parent),
    new ModelsLibraryAction(this, parent),
    new SaveAsNetLogoWebAction(this, workspace, modelSaver, parent),
    new ImportClientAction(this, workspace, parent),
    new ManageResourcesAction(this, workspace, parent))

  def saveModelActions(parent: Component) = {
    def saveAction(saveAs: Boolean) =
      new ExceptionCatchingAction(
        if (saveAs) I18N.gui.get("menu.file.saveAs") + Ellipsis else I18N.gui.get("menu.file.save"),
        parent)
      with MenuAction {
        category = UserAction.FileCategory
        group = UserAction.FileSaveGroup
        accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withShift = saveAs)
        rank = 0

        @throws(classOf[UserCancelException])
        override def action(): Unit = {
          tabManager.getSelectedTab match {
            case tempTab: TemporaryCodeTab => tempTab.save(saveAs)
            case _ => saveModel(saveAs)
          }

          tabManager.saveOpenTabs()
        }
      }

    Seq(saveAction(false), saveAction(true))
  }

  def convertTabAction(t: TemporaryCodeTab): MenuAction = {
    val version =
      if (Version.is3D) "NetLogo 3D 5.3.1"
      else              "NetLogo 5.3.1"
    val convertIncludeFile = new ConvertIncludeFile(modelConverter, version)
    new ConvertNlsAction(t, modelSaver, convertIncludeFile, workspace, controller)
  }
}
