// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.{ URI, URISyntaxException }
import java.io.{ File, IOException }
import java.nio.file.Paths
import java.awt.{ Component, Container, FileDialog => AWTFileDialog }
import javax.swing.{ Action, JOptionPane }, Action.{ ACCELERATOR_KEY }

import org.nlogo.core.{ I18N, Model }
import org.nlogo.api.{ Exceptions, ModelLoader, ModelReader, ModelType, Version }, ModelReader.{ emptyModelPath, modelSuffix }
import org.nlogo.app.common.{ Actions, ExceptionCatchingAction }, Actions.Ellipsis
import org.nlogo.app.tools.{ ModelsLibraryDialog, NetLogoWebSaver }
import org.nlogo.awt.{ Hierarchy, UserCancelException }
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionDialog, UserAction }, UserAction.MenuAction
import org.nlogo.workspace.{ AbstractWorkspaceScala, OpenModel, SaveModel, SaveModelAs }
import org.nlogo.window.{ BackgroundFileController, Events, FileController, ReconfigureWorkspaceUI },
  Events.{ AboutToQuitEvent, LoadModelEvent, ModelSavedEvent, OpenModelEvent }
import org.nlogo.fileformat.ModelConversion

import scala.util.{ Failure, Try }

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
      manager.offerSave()
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
      manager.offerSave()
      manager.openFromPath(manager.userChooseLoadPath(), ModelType.Normal)
    }
  }

  class SaveAction(manager: FileManager, parent: Component)
    extends ExceptionCatchingAction(I18N.gui.get("menu.file.save"), parent)
    with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      manager.save(false)
    }
  }

  class SaveAsAction(manager: FileManager, parent: Component)
    extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAs") + Ellipsis, parent)
    with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withShift = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      manager.save(true)
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
    group       =   UserAction.FileOpenGroup
    rank        =    3
    accelerator =             UserAction.KeyBindings.keystroke('M', withMenu = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      manager.offerSave()
      val sourceURI = ModelsLibraryDialog.open(frame)
      manager.openFromURI(sourceURI, ModelType.Library)
    }
  }

  class ImportClientAction(manager: FileManager, workspace: AbstractWorkspaceScala, parent: Component)
    extends ExceptionCatchingAction(I18N.gui.get("menu.file.import.hubNetClientInterface") + Ellipsis, parent)
    with MenuAction {
    category    = UserAction.FileCategory
    subcategory = UserAction.FileImportSubcategory

    var exception = Option.empty[IOException]

    def importTask(importPath: String, sectionChoice: Int): Runnable =
      new Runnable() {
        def run(): Unit = {
          try {
            manager.loadModel(Paths.get(importPath).toUri).map(model =>
              workspace.getHubNetManager.foreach(_.importClientInterface(model, sectionChoice == 1)))
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val importPath = org.nlogo.swing.FileDialog.show(
          parent, I18N.gui.get("menu.file.import.hubNetClientInterface"), java.awt.FileDialog.LOAD, null);
      val choice =
          OptionDialog.show(frame,
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

  class SaveAsNetLogoWebAction(manager: FileManager, workspace: AbstractWorkspaceScala, modelSaver: ModelSaver, parent: Component)
    extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAsNetLogoWeb"), parent)
    with MenuAction {
    category = UserAction.FileCategory
    group    = UserAction.FileShareGroup

    // disabled for 3-D since you can't do that in NetLogo Web - RG 9/10/15
    setEnabled(! Version.is3D)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.show(
        parent, I18N.gui.get("menu.file.saveAsNetLogoWeb.dialog"),
        AWTFileDialog.SAVE, suggestedFileName)

      val exportFile = new File(exportPath)
      val saver = NetLogoWebSaver(exportPath)
      saver.save(modelToSave, exportFile.getName)
    }

    @throws(classOf[UserCancelException])
    def suggestedFileName: String = {
      if (workspace.getModelType == ModelType.New) {
        manager.save(false)
        workspace.getModelFileName.stripSuffix(".nlogo") + ".html"
      } else
        workspace.modelNameForDisplay + ".html"
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
      val modelType = workspace.getModelType
      val typeKey =
        if (modelType == ModelType.Normal) "fromSave" else "fromLibrary"
      val options = Array[Object](
        I18N.gui.get("menu.file.nlw.prompt." + typeKey),
        I18N.gui.get("menu.file.nlw.prompt.fromCurrentCopy"),
        I18N.gui.get("common.buttons.cancel"))
      val title   = I18N.gui.get("menu.file.nlw.prompt.title")
      val message = I18N.gui.get("menu.file.nlw.prompt.message." + typeKey)
      val choice = OptionDialog.show(parent, title, message, options)
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
}

import FileManager._

/** This class manages a number of file operations. Much of the code in here used to live in
 *  fileMenu, but it's obviously undesirable to couple the behavior in this class too closely to
 *  its presentation (the menu) */
class FileManager(workspace: AbstractWorkspaceScala,
  modelLoader: ModelLoader,
  modelConverter: ModelConversion,
  dirtyMonitor: DirtyMonitor,
  modelSaver: ModelSaver,
  eventRaiser: AnyRef,
  parent: Container)
    extends OpenModelEvent.Handler
    with LoadModelEvent.Handler {
  private var firstLoad: Boolean = true

  val controller = new FileController(parent, workspace)

  def handle(e: OpenModelEvent): Unit = {
    openFromPath(e.path, ModelType.Library)
  }

  def handle(e: LoadModelEvent): Unit = {
    modelSaver.setCurrentModel(e.model)
  }

  @throws(classOf[UserCancelException])
  def quit(): Unit = {
    offerSave()
    new AboutToQuitEvent().raise(eventRaiser)
    workspace.getExtensionManager.reset()
    System.exit(0)
  }

  // called whenever the workspace is about to be destroyed
  @throws(classOf[UserCancelException])
  def offerSave(): Unit = {
    if (dirtyMonitor.dirty && userWantsToSaveFirst()) {
      save(false)
    }
  }

  /**
   * opens a model from a file path.
   */
  def openFromPath(path: String, modelType: ModelType): Unit = {
    openFromURI(new File(path).toURI, modelType)
  }

  def openFromURI(uri: URI, modelType: ModelType): Unit = {
    loadModel(uri).foreach(m => openFromModel(m, uri, modelType))
  }

  private def runLoad(linkParent: Container, uri: URI, model: Model, modelType: ModelType): Unit = {
    ReconfigureWorkspaceUI(linkParent, uri, modelType, model, workspace)
  }

  private def loadModel(uri: URI): Option[Model] = {
    ModalProgressTask.runForResultOnBackgroundThread(
      Hierarchy.getFrame(parent), I18N.gui.get("dialog.interface.loading.task"),
      (dialog) => new BackgroundFileController(dialog, controller),
      (fileController: BackgroundFileController) =>
        try {
          OpenModel(uri, fileController, modelLoader, modelConverter, Version)
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

  /// saving
  @throws(classOf[UserCancelException])
  private[app] def save(saveAs: Boolean): Unit = {
    val saveThunk =
      if (saveAs)
        SaveModelAs(currentModel, modelLoader, controller, workspace, Version)
      else
        SaveModel(currentModel, modelLoader, controller, workspace, Version)

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
      val loader = new Runnable() {
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
    FileDialog.show(parent, I18N.gui.get("menu.file.open"), AWTFileDialog.LOAD, null)
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst(): Boolean = {
    val options = Array[Object](
      I18N.gui.get("common.buttons.save"),
      I18N.gui.get("common.buttons.discard"),
      I18N.gui.get("common.buttons.cancel"))

    val message = I18N.gui.get("menu.file.save.confirm")

    OptionDialog.show(parent, "NetLogo", message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException()
    }
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
    new SaveAction(this, parent),
    new SaveAsAction(this, parent),
    new QuitAction(this, parent),
    new ModelsLibraryAction(this, parent),
    new SaveAsNetLogoWebAction(this, workspace, modelSaver, parent),
    new ImportClientAction(this, workspace, parent))
}
