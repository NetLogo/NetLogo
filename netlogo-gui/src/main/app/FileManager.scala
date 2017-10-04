// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Container, FileDialog => AWTFileDialog }
import java.io.{ File, IOException }
import java.net.{ URI, URISyntaxException }
import java.nio.file.Paths
import javax.swing.{ Action, JOptionPane }

import scala.util.{ Failure, Try }

import org.nlogo.core.{ I18N, Model }
import org.nlogo.api.{ Exceptions, FileIO, ModelLoader, ModelReader, ModelType, Version },
  ModelReader.{ emptyModelPath, modelSuffix }
import org.nlogo.app.common.{ Actions, Dialogs, ExceptionCatchingAction }, Actions.Ellipsis
import org.nlogo.app.codetab.TemporaryCodeTab
import org.nlogo.app.tools.{ ModelsLibraryDialog, NetLogoWebSaver }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.fileformat.{ FailedConversionResult, ModelConversion, SuccessfulConversion }
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionDialog, UserAction }, UserAction.MenuAction
import org.nlogo.window.{ BackgroundFileController, Events, FileController, ReconfigureWorkspaceUI },
  Events.{AboutToCloseFilesEvent, AboutToQuitEvent, LoadModelEvent, ModelSavedEvent, OpenModelEvent }
import org.nlogo.workspace.{ AbstractWorkspace, ModelTracker, OpenModel, OpenModelFromURI,
  OpenModelFromSource, SaveModel, SaveModelAs }

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

  class ImportClientAction(manager: FileManager, workspace: AbstractWorkspace, parent: Component)
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
            manager.loadModel(uri, manager.openModelURI(uri)).map(model =>
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
          OptionDialog.showMessage(frame,
                  I18N.gui.get("menu.file.import.hubNetClientInterface.message"),
                  I18N.gui.get("menu.file.import.hubNetClientInterface.prompt"),
                  Array[Object](
    I18N.gui.get("menu.file.import.hubNetClientInterface.fromInterface"),
    I18N.gui.get("menu.file.import.hubNetClientInterface.fromClient"),
    I18N.gui.get("common.buttons.cancel")))

      if (choice != 2) {
        ModalProgressTask.onUIThread(
          frame,
          I18N.gui.get("dialog.interface.import.task"),
          importTask(importPath, choice))
        exception.foreach(throw _)
      }
    }
  }

  class SaveAsNetLogoWebAction(manager: FileManager, modelTracker: ModelTracker, modelSaver: ModelSaver, parent: Component)
  extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAsNetLogoWeb"), parent)
  with MenuAction {
    category = UserAction.FileCategory
    group    = UserAction.FileShareGroup

    // disabled for 3-D since you can't do that in NetLogo Web - RG 9/10/15
    setEnabled(! Version.is3D)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.showFiles(
        parent, I18N.gui.get("menu.file.saveAsNetLogoWeb.dialog"),
        AWTFileDialog.SAVE, suggestedFileName)

      val exportFile = new File(exportPath)
      val saver = NetLogoWebSaver(exportPath)
      saver.save(modelToSave, exportFile.getName)
    }

    @throws(classOf[UserCancelException])
    def suggestedFileName: String = {
      if (modelTracker.getModelType == ModelType.New) {
        manager.saveModel(false)
        modelTracker.getModelFileName.stripSuffix(".nlogo") + ".html"
      } else
        modelTracker.modelNameForDisplay + ".html"
    }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    def modelToSave: String = {
      if (doesNotMatchWorkingCopy && userWantsLastSaveExported())
        modelSaver.modelAsString(modelSaver.priorModel, "nlogo")
      else
        modelSaver.modelAsString(modelSaver.currentModel, "nlogo")
    }

    @throws(classOf[UserCancelException])
    private def userWantsLastSaveExported(): Boolean = {
      val modelType = modelTracker.getModelType
      val typeKey =
        if (modelType == ModelType.Normal) "fromSave" else "fromLibrary"
      val options = Array[Object](
        I18N.gui.get("menu.file.nlw.prompt." + typeKey),
        I18N.gui.get("menu.file.nlw.prompt.fromCurrentCopy"),
        I18N.gui.get("common.buttons.cancel"))
      val title   = I18N.gui.get("menu.file.nlw.prompt.title")
      val message = I18N.gui.get("menu.file.nlw.prompt.message." + typeKey)
      val choice = OptionDialog.showMessage(parent, title, message, options)
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
  }

  class ConvertNlsAction(
    tab:            TemporaryCodeTab,
    modelSaver:     ModelSaver,
    modelConverter: ModelConversion,
    workspace:      AbstractWorkspace,
    controller:     FileController)
  extends ExceptionCatchingAction(I18N.gui.get("menu.edit.convertToNetLogoSix"), tab)
  with MenuAction{
    category = UserAction.EditCategory
    group    = "ConversionGroup"

    override def action(): Unit = {
      tab.filename.right.toOption
        .flatMap(name => FileIO.resolvePath(name, Paths.get(workspace.getModelPath)))
        .foreach { path =>
        val version =
          if (Version.is3D) "NetLogo 3D 5.3.1"
          else              "NetLogo 5.3.1"
        val tempModel = modelSaver.currentModel.copy(code = tab.innerSource, version = version)
        modelConverter(tempModel, path) match {
          case SuccessfulConversion(originalModel, m) => tab.innerSource = m.code
          case failure: FailedConversionResult =>
            controller.showAutoconversionError(failure, "nls").foreach { m =>
              tab.innerSource = m.code
            }
        }
      }
    }
  }

}

import FileManager._

/** This class manages a number of file operations. Much of the code in here used to live in
 *  fileMenu, but it's obviously undesirable to couple the behavior in this class too closely to
 *  its presentation (the menu) */
class FileManager(workspace: AbstractWorkspace,
  modelTracker: ModelTracker,
  modelLoader: ModelLoader,
  modelConverter: ModelConversion,
  dirtyMonitor: DirtyMonitor,
  modelSaver: ModelSaver,
  eventRaiser: AnyRef,
  parent: Container)
    extends OpenModelEvent.Handler
    with LoadModelEvent.Handler {
  private var firstLoad: Boolean = true

  val controller = new FileController(parent, modelTracker)

  def handle(e: OpenModelEvent): Unit = {
    openFromPath(e.path, ModelType.Library)
  }

  def handle(e: LoadModelEvent): Unit = {
    modelSaver.setCurrentModel(e.model)
  }

  private[app] def aboutToCloseFiles(): Unit = {
    if (dirtyMonitor.modelDirty &&
        Dialogs.userWantsToSaveFirst(I18N.gui.get("file.save.offer.thisModel"), parent))
      saveModel(false)
    new AboutToCloseFilesEvent().raise(eventRaiser)
  }

  @throws(classOf[UserCancelException])
  def quit(): Unit = {
    aboutToCloseFiles()
    new AboutToQuitEvent().raise(eventRaiser)
    workspace.getExtensionManager.reset()
    System.exit(0)
  }

  /**
   * opens a model from a file path.
   */
  def openFromPath(path: String, modelType: ModelType): Unit = {
    openFromURI(new File(path).toURI, modelType)
  }

  def openFromURI(uri: URI, modelType: ModelType): Unit = {
    loadModel(uri, openModelURI(uri)).foreach(m => openFromModel(m, uri, modelType))
  }

  private def openModelURI(uri: URI): (OpenModel.Controller) => Option[Model] =
    ((fileController: OpenModel.Controller) => OpenModelFromURI(uri, fileController, modelLoader, modelConverter, Version))

  def openFromSource(uri: URI, modelSource: String, modelType: ModelType): Unit = {
    loadModel(uri,
      (fileController: OpenModel.Controller) =>
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

  private def runLoad(linkParent: Container, uri: URI, model: Model, modelType: ModelType): Unit = {
    ReconfigureWorkspaceUI(linkParent, uri, modelType, model, workspace.compilerServices)
  }

  private def loadModel(uri: URI, openModel: (OpenModel.Controller) => Option[Model]): Option[Model] = {
    ModalProgressTask.runForResultOnBackgroundThread(
      Hierarchy.getFrame(parent), I18N.gui.get("dialog.interface.loading.task"),
      (dialog) => new BackgroundFileController(dialog, controller),
      (fileController: BackgroundFileController) =>
        try {
          openModel(fileController)
        } catch {
          case e: Exception => println("Exception in FileMenu.loadModel: " + e)
          None
        })
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
    val saveThunk = {
      val saveModel = if (saveAs) SaveModelAs else SaveModel
      saveModel(currentModel, modelLoader, controller, modelTracker, Version)
    }

    // if there's no thunk, the user canceled the save
    saveThunk.foreach { thunk =>
      val saver = new Saver(thunk)

      ModalProgressTask.onUIThread(Hierarchy.getFrame(parent),
        I18N.gui.get("dialog.interface.saving.task"), saver)

      if (! saver.result.isDefined)
        throw new UserCancelException()

      saver.result match {
        case Some(Failure(e: Throwable)) =>
          JOptionPane.showMessageDialog(parent,
            I18N.gui.getN("menu.file.save.error", e.getMessage),
            "NetLogo", JOptionPane.ERROR_MESSAGE)
        case _ =>
      }
    }
  }

  private def openFromModel(model: Model, uri: URI, modelType: ModelType): Unit = {
    if (firstLoad) {
      firstLoad = false
      runLoad(parent, uri, model, modelType)
    } else {
      val loader = new Runnable {
        override def run(): Unit = {
          runLoad(parent, uri, model, modelType)
        }
      }
      ModalProgressTask.onUIThread(
        Hierarchy.getFrame(parent), I18N.gui.get("dialog.interface.loading.task"), loader)
    }
  }

  @throws(classOf[UserCancelException])
  private def userChooseLoadPath(): String = {
    FileDialog.showFiles(parent, I18N.gui.get("menu.file.open"), AWTFileDialog.LOAD, null)
  }

  private class Saver(val thunk: () => Try[URI]) extends Runnable {
    var result = Option.empty[Try[URI]]

    def run(): Unit = {
      val r = thunk()
      r.foreach { uri =>
        val path = Paths.get(uri).toString
        modelSaver.setCurrentModel(modelSaver.currentModel.copy(version = Version.version))
        new ModelSavedEvent(path).raise(eventRaiser)
      }
      result = Some(r)
    }
  }

  def currentModel: Model = modelSaver.currentModel

  def actions: Seq[Action] = Seq(
    new NewAction(this, parent),
    new OpenAction(this, parent),
    new QuitAction(this, parent),
    new ModelsLibraryAction(this, parent),
    new SaveAsNetLogoWebAction(this, modelTracker, modelSaver, parent),
    new ImportClientAction(this, workspace, parent))

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
        override def action(): Unit = saveModel(saveAs)
      }

    Seq(saveAction(false), saveAction(true))
  }

  def convertTabAction(t: TemporaryCodeTab): Action = {
    new ConvertNlsAction(t, modelSaver, modelConverter, workspace, controller)
  }
}
