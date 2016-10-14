// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.{ URI, URISyntaxException }
import java.io.{ File, IOException }
import java.nio.file.Paths
import java.awt.event.ActionEvent
import java.awt.{ Container, FileDialog => AWTFileDialog }
import javax.swing.{ AbstractAction => SwingAbstractAction, JOptionPane }

import org.nlogo.agent.ImportPatchColors
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionDialog }
import org.nlogo.api.{ Exceptions, FileIO, LocalFile, ModelingCommonsInterface, ModelLoader, ModelReader,
  ModelSection, ModelSectionJ, ModelType, ModelTypeJ, Version },
    ModelReader.{ modelSuffix, emptyModelPath }
import org.nlogo.app.common.{ CodeToHtml, ExportBackgroundAction, ExportInterfaceAction }
import org.nlogo.app.tools.{ ModelsLibraryDialog, NetLogoWebSaver }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.awt.{ Hierarchy => NLogoHierarchy, UserCancelException }
import org.nlogo.window.{ BackgroundFileController, FileController, Events => WindowEvents, GUIWorkspace,
  LinkRoot, PlotWidgetExport, ReconfigureWorkspaceUI },
  WindowEvents.{ AboutToQuitEvent, ExportPlotEvent, LoadModelEvent, ModelSavedEvent, OpenModelEvent }
import org.nlogo.workspace.{ OpenModel, SaveModel, SaveModelAs }
import org.nlogo.plot.Plot
import org.nlogo.swing.{ Implicits, Menu => SwingMenu }, Implicits.thunk2runnable
import org.nlogo.fileformat.{ ModelConversion, NLogoFormat, NLogoModelSettings, NLogoHubNetFormat, NLogoPreviewCommandsFormat }

import scala.util.Try
import scala.concurrent.Future

class FileMenu(app: App,
  modelSaver: ModelSaver,
  modelLoader: ModelLoader,
  modelConverter: ModelConversion)
  extends SwingMenu(I18N.gui.get("menu.file"))
    with OpenModelEvent.Handler
    with LoadModelEvent.Handler {

  val ellipsis = '\u2026'
  implicit val i18nPrefix = I18N.Prefix("menu.file")
  def workspace: GUIWorkspace = app.workspace
  def tabs: Tabs = app.tabs
  def modelingCommons: ModelingCommonsInterface = app.modelingCommons
  def frame: AppFrame = app.frame

  private var savedVersion: String = Version.version

  private var firstLoad: Boolean = true

  val dirtyMonitor = new DirtyMonitor(frame, modelSaver, modelLoader, workspace)
  frame.addLinkComponent(dirtyMonitor)

  val controller = new FileController(this, workspace)

  setMnemonic('F')
  addMenuItem('N', new NewAction)
  addMenuItem('O', new OpenAction)
  addMenuItem('M', new ModelsLibraryAction)
  add(new RecentFilesMenu(frame, this))
  addSeparator()
  addMenuItem('S', new SaveAction)
  addMenuItem('S', true, new SaveAsAction)
  addMenuItem(new SaveModelingCommonsAction)
  addSeparator()
  addMenuItem(new SaveAsNetLogoWebAction)
  addSeparator()
  addMenuItem(I18N.gui("print"), 'P', tabs.printAction)
  addSeparator()
  val exportMenu = new SwingMenu(I18N.gui("export"))
  exportMenu.addMenuItem(new ExportWorldAction)
  exportMenu.addMenuItem(new ExportPlotAction)
  exportMenu.addMenuItem(new ExportAllPlotsAction)
  exportMenu.addMenuItem(new ExportGraphicsAction)
  exportMenu.addMenuItem(new ExportInterfaceAction(this, workspace))
  exportMenu.addMenuItem(new ExportOutputAction)
  exportMenu.addMenuItem(new ExportCodeAction)
  add(exportMenu)
  addSeparator()
  val importMenu = new SwingMenu(I18N.gui("import"))
  importMenu.addMenuItem(new ImportWorldAction)
  importMenu.addMenuItem(new ImportPatchColorsAction)
  importMenu.addMenuItem(new ImportPatchColorsRGBAction)
  if (!Version.is3D) {
    importMenu.addMenuItem(new ImportDrawingAction)
  }
  importMenu.addMenuItem(new ImportClientAction)

  add(importMenu)

  if (!System.getProperty("os.name").startsWith("Mac")) {
    addSeparator()
    addMenuItem('Q', new QuitAction)
  }

  ///
  abstract class FileMenuAction(name: String) extends SwingAbstractAction(name) {

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    def action(): Unit

    def actionPerformed(e: ActionEvent): Unit = {
      try {
        action()
      } catch {
        case ex: UserCancelException => Exceptions.ignore(ex)
        case ex: IOException => JOptionPane.showMessageDialog(
          FileMenu.this, ex.getMessage,
          I18N.gui.get("common.messages.error"), JOptionPane.ERROR_MESSAGE)
      }
    }
  }

  private class NewAction extends FileMenuAction(I18N.gui("new")) {
    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      offerSave()
      newModel()
    }
  }

  private class OpenAction extends FileMenuAction(I18N.gui("open") + ellipsis) {
    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      offerSave()
      openFromPath(userChooseLoadPath(), ModelType.Normal)
    }
  }

  private class ModelsLibraryAction extends FileMenuAction(I18N.gui("modelsLibrary")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      offerSave()
      val sourceURI =
        ModelsLibraryDialog.open(NLogoHierarchy.getFrame(FileMenu.this))
      openFromURI(sourceURI, ModelType.Library)
    }
  }

  private class SaveModelingCommonsAction extends FileMenuAction(I18N.gui("uploadMc")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      modelingCommons.saveToModelingCommons()
    }
  }

  private class SaveAction extends FileMenuAction(I18N.gui("save")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      save(false)
    }
  }

  private class SaveAsAction extends FileMenuAction(I18N.gui("saveAs") + ellipsis) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      save(true)
    }
  }

  private class SaveAsNetLogoWebAction extends FileMenuAction(I18N.gui("saveAsNetLogoWeb")) {
    // disabled for 3-D since you can't do that in NetLogo Web - RG 9/10/15
    setEnabled(! Version.is3D)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.show(
        FileMenu.this,
        I18N.gui("saveAsNetLogoWeb.dialog"),
        AWTFileDialog.SAVE, suggestedFileName)

      val exportFile = new File(exportPath)
      val saver = NetLogoWebSaver(exportPath)
      saver.save(modelToSave, exportFile.getName)
    }

    @throws(classOf[UserCancelException])
    def suggestedFileName: String = {
      if (workspace.getModelType == ModelType.New) {
        save(false)
        workspace.getModelFileName.stripSuffix(".nlogo") + ".html"
      } else
        guessFileName.stripSuffix(".nlogo") + ".html"
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
        I18N.gui("nlw.prompt." + typeKey),
        I18N.gui("nlw.prompt.fromCurrentCopy"),
        I18N.gui.get("common.buttons.cancel"))
      val title   = I18N.gui("nlw.prompt.title")
      val message = I18N.gui("nlw.prompt.message." + typeKey)
      val choice = OptionDialog.show(FileMenu.this, title, message, options)
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

  abstract class ExportAction(taskName: String, suggestedFileName: String, performExport: String => Unit = {(s) => })
    extends FileMenuAction(I18N.gui("export." + taskName) + ellipsis) {

    def exportTask(path: String): Runnable = new Runnable() {
      override def run(): Unit = {
        try {
          performExport(path)
        }
        catch {
          case ex: IOException => exception = Some(ex)
        }
      }
    }

    var exception = Option.empty[IOException]

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.show(
        FileMenu.this,
        I18N.gui(s"export.$taskName"),
        AWTFileDialog.SAVE, workspace.guessExportName(suggestedFileName))
      exception = None

      ModalProgressTask.onUIThread(
          NLogoHierarchy.getFrame(FileMenu.this),
          I18N.gui.get("dialog.interface.export.task"), exportTask(exportPath))

      exception.foreach(throw _)
    }
  }

  private class ExportWorldAction extends ExportAction("world", "world.csv", workspace.exportWorld _)

  private class ExportGraphicsAction
    extends ExportBackgroundAction[String](this, "view", workspace.guessExportName("view.png")) {
      def beforeModalDialog(): String = promptForFilePath()

      def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
        workspace.exportViewFromUIThread(filename, "png", closeDialog)
      }
  }


  private class ExportOutputAction extends ExportAction("output", "output.txt", { exportPath =>
    workspace.exportOutput(exportPath)
  })

  private class ExportPlotAction
    extends ExportBackgroundAction[(String, Plot)](this, "plot", workspace.guessExportName("plot.csv")) {

    def beforeModalDialog(): (String, Plot) = {
      val plot = workspace.plotExportControls.choosePlot(frame)
        .getOrElse(throw new UserCancelException())
      val filepath = promptForFilePath()
      (filepath, plot)
    }

    def inModalDialog(filenameAndPlot: (String, Plot), closeDialog: () => Unit): Unit = {
      new ExportPlotEvent(PlotWidgetExport.ExportSinglePlot(filenameAndPlot._2), filenameAndPlot._1, closeDialog)
        .raise(FileMenu.this)
    }
  }

  private class ExportAllPlotsAction
    extends ExportBackgroundAction[String](this, "allPlots", workspace.guessExportName("plots.csv")) {
    def beforeModalDialog(): String = {
      if (workspace.plotExportControls.plotNames.isEmpty) {
        workspace.plotExportControls.sorryNoPlots(frame)
        throw new UserCancelException()
      } else
        promptForFilePath()
    }

    def inModalDialog(filename: String, closeDialog: () => Unit): Unit = {
      new ExportPlotEvent(PlotWidgetExport.ExportAllPlots, filename, closeDialog)
        .raise(FileMenu.this)
    }
  }

  private class ExportCodeAction extends ExportAction("code", "code.html", { exportPath =>
    FileIO.writeFile(exportPath,
      new CodeToHtml(workspace.compiler).convert(app.tabs.codeTab.getText))
  })

  abstract class ImportAction(taskName: String, performImport: String => Unit = { s => })
    extends FileMenuAction(I18N.gui(s"import.$taskName") + ellipsis) {
    var exception = Option.empty[IOException]

    def importTask(path: String): Runnable = new Runnable() {
      override def run(): Unit = {
        try {
          performImport(path)
        }
        catch {
          case ex: IOException => exception = Some(ex)
        }
      }
    }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      exception = None
      val importPath = FileDialog.show(
          FileMenu.this, I18N.gui(s"import.$taskName"), AWTFileDialog.LOAD, null)

      ModalProgressTask.onUIThread(NLogoHierarchy.getFrame(FileMenu.this),
        I18N.gui.get("dialog.interface.import.task"),
        importTask(importPath))
      exception.foreach(throw _)
    }
  }

  private class ImportWorldAction extends ImportAction("world", { importPath =>
    workspace.importWorld(importPath)
    workspace.view.dirty()
    workspace.view.repaint()
  })

  private class ImportPatchColorsAction extends ImportAction("patchColors", { importPath =>
    // We can't wait for the thread to complete, or we end
    // up locking up the app since the Model Dialog and the
    // job wedge against one another. -- CLB 07/19/05
    ImportPatchColors.importPatchColors(
      new LocalFile(importPath),
      workspace.world, true)
    workspace.view.dirty()
    workspace.view.repaint()
  })

  private class ImportPatchColorsRGBAction extends ImportAction("patchColorsRGB", { importPath =>
    // We can't wait for the thread to complete, or we end
    // up locking up the app since the Model Dialog and the
    // job wedge against one another. -- CLB 07/19/05
    ImportPatchColors.importPatchColors(
      new LocalFile(importPath), workspace.world, false)
    workspace.view.dirty()
    workspace.view.repaint()
  })

  private class ImportDrawingAction extends ImportAction("drawing", { importPath =>
    workspace.importDrawing(importPath);
    workspace.view.dirty();
    workspace.view.repaint();
  })

  private class ImportClientAction extends FileMenuAction(I18N.gui("import.hubNetClientInterface") + ellipsis) {
    var exception = Option.empty[IOException]

    def importTask(importPath: String, sectionChoice: Int): Runnable =
      new Runnable() {
        def run(): Unit = {
          try {
            loadModel(Paths.get(importPath).toUri).map(model =>
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
          FileMenu.this, I18N.gui("import.hubNetClientInterface"), java.awt.FileDialog.LOAD, null);
      val choice =
          OptionDialog.show(workspace.getFrame,
                  I18N.gui("import.hubNetClientInterface.message"),
                  I18N.gui("import.hubNetClientInterface.prompt"),
                  Array[Object](
    I18N.gui("import.hubNetClientInterface.fromInterface"),
    I18N.gui("import.hubNetClientInterface.fromClient"),
    I18N.gui.get("common.buttons.cancel")))

      if (choice != 2) {
        ModalProgressTask.onUIThread(
          NLogoHierarchy.getFrame(FileMenu.this),
          I18N.gui.get("dialog.interface.import.task"),
          importTask(importPath, choice))
        exception.foreach(throw _)
      }
    }
  }

  private class QuitAction extends FileMenuAction(I18N.gui("quit")) {
    override def action(): Unit = {
      try {
        quit()
      } catch {
        case ex: UserCancelException => Exceptions.ignore(ex)
      }
    }
  }

  ///

  @throws(classOf[UserCancelException])
  def quit(): Unit = {
    offerSave()
    new AboutToQuitEvent().raise(this)
    workspace.getExtensionManager.reset()
    System.exit(0)
  }

  ///

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogo" otherwise.
   */
  private def guessFileName: String =
    workspace.modelNameForDisplay + "." + modelSuffix

  /// model, how shall I load thee?  let me count the ways

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

  /**
   * opens a model from a file path.
   */
  def openFromPath(path: String, modelType: ModelType): Unit = {
    openFromURI(new File(path).toURI, modelType)
  }

  def openFromURI(uri: URI, modelType: ModelType): Unit = {
    loadModel(uri).foreach(m => openFromModel(m, uri, modelType))
  }

  private def loadModel(uri: URI): Option[Model] = {
    ModalProgressTask.runForResultOnBackgroundThread(
      NLogoHierarchy.getFrame(this), I18N.gui.get("dialog.interface.loading.task"), (dialog) => new BackgroundFileController(dialog, controller),
      (fileController: BackgroundFileController) =>
        try {
          OpenModel(uri, fileController, modelLoader, modelConverter, Version)
        } catch {
          case e: Exception => println("Exception in FileMenu.loadModel: " + e)
          None
        })
  }

  private def openFromModel(model: Model, uri: URI, modelType: ModelType): Unit = {
    if (firstLoad) {
      firstLoad = false
      runLoad(this, uri, model, modelType)
    } else {
      val loader = new Runnable() {
        override def run(): Unit = {
          runLoad(FileMenu.this, uri, model, modelType)
        }
      }
      ModalProgressTask.onUIThread(
          NLogoHierarchy.getFrame(this), I18N.gui.get("dialog.interface.loading.task"), loader)
      tabs.requestFocus()
    }
    savedVersion = model.version // maybe the whole model should be stored?
  }

  private def runLoad(linkParent: Container, uri: URI, model: Model, modelType: ModelType): Unit = {
    ReconfigureWorkspaceUI(linkParent, uri, modelType, model, workspace)
  }

  def handle(e: OpenModelEvent): Unit = {
    openFromPath(e.path, ModelType.Library)
  }

  def handle(e: LoadModelEvent): Unit = {
    modelSaver.setCurrentModel(e.model)
  }

  def currentModel: Model = modelSaver.currentModel

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

      ModalProgressTask.onUIThread(NLogoHierarchy.getFrame(this),
        I18N.gui.get("dialog.interface.saving.task"), saver)

      if (! saver.result.isDefined)
        throw new UserCancelException()

      saver.result.foreach(_.failed.foreach { e =>
        JOptionPane.showMessageDialog(this,
          I18N.gui.getN("menu.file.save.error", e.getMessage),
          "NetLogo", JOptionPane.ERROR_MESSAGE)
      })

      tabs.saveExternalFiles()
    }
  }

  private class Saver(val thunk: () => Try[URI]) extends Runnable {
    var result = Option.empty[Try[URI]]

    def run(): Unit = {
      val r = thunk()
      r.foreach { uri =>
        val path = Paths.get(uri).toString
        new ModelSavedEvent(path).raise(FileMenu.this)
      }
      result = Some(r)
    }
  }

  /// and now, a whole bunch of dialog boxes

  // this is called whenever a workspace is about to be destroyed
  @throws(classOf[UserCancelException])
  def offerSave(): Unit = {
    if (dirtyMonitor.dirty && userWantsToSaveFirst()) {
      save(false)
    }
  }

  @throws(classOf[UserCancelException])
  private def userChooseLoadPath(): String = {
    FileDialog.show(this, I18N.gui("open"), AWTFileDialog.LOAD, null)
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst(): Boolean = {
    val options = Array[Object](
      I18N.gui.get("common.buttons.save"),
      I18N.gui.get("common.buttons.discard"),
      I18N.gui.get("common.buttons.cancel"))

    val message = I18N.gui("save.confirm")

    OptionDialog.show(this, "NetLogo", message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException()
    }
  }

  @throws(classOf[UserCancelException])
  private def checkWithUserBeforeSavingModelFromOldVersion(): Unit = {
    if (! Version.compatibleVersion(savedVersion)) {
      val options = Array[Object](
        I18N.gui.get("common.buttons.save"),
        I18N.gui.get("common.buttons.cancel"))
      val message = I18N.gui.getN("file.save.warn.savingInNewerVersion", savedVersion, Version.version)
      if (OptionDialog.show(this, "NetLogo", message, options) != 0) {
        throw new UserCancelException()
      }
      savedVersion = Version.version
    }
  }
}
