// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.util.Map

import org.nlogo.agent.ImportPatchColors
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionDialog }
import org.nlogo.api.{ Exceptions, FileIO, LocalFile, ModelLoader, ModelReader,
  ModelSection, ModelSectionJ, ModelType, ModelTypeJ, Version },
    ModelReader.{ modelSuffix, emptyModelPath }
import org.nlogo.core.{ I18N, Model }
import org.nlogo.awt.{ Hierarchy => NLogoHierarchy, UserCancelException }
import org.nlogo.window.{ FileController, PlotWidgetExportType, Events => WindowEvents, ReconfigureWorkspaceUI },
  WindowEvents.{ AboutToQuitEvent, ExportOutputEvent, ExportPlotEvent, ModelSavedEvent, OpenModelEvent }
import org.nlogo.workspace.OpenModel
import org.nlogo.swing.{ Menu => SwingMenu }
import org.nlogo.fileformat.{ NLogoFormat, NLogoModelSettings, NLogoHubNetFormat, NLogoPreviewCommandsFormat }

import java.net.{ URI, URISyntaxException }
import java.io.{ File, IOException }
import java.nio.file.Paths
import java.awt.event.ActionEvent
import java.awt.{ Container, FileDialog => AWTFileDialog }
import javax.swing.{ AbstractAction => SwingAbstractAction, JOptionPane }

/*
 * note that multiple instances of this class may exist
 * as there are now multiple frames that each have their own menu bar
 * and menus ev 8/25/05
 */

class FileMenu(app: App, modelSaver: ModelSaver, modelLoader: ModelLoader)
  extends SwingMenu(I18N.gui.get("menu.file")) with OpenModelEvent.Handler {

  private var savedVersion: String = Version.version

  private var firstLoad: Boolean = true

  setMnemonic('F')
  addMenuItem('N', new NewAction())
  addMenuItem('O', new OpenAction())
  addMenuItem('M', new ModelsLibraryAction())
  add(new RecentFilesMenu(app, this))
  addSeparator()
  addMenuItem('S', new SaveAction())
  addMenuItem('S', true, new SaveAsAction())
  addMenuItem(new SaveModelingCommonsAction())
  addSeparator()
  addMenuItem(new SaveAsNetLogoWebAction())
  addSeparator()
  addMenuItem(I18N.gui.get("menu.file.print"), 'P', app.tabs.printAction)
  addSeparator()
  val exportMenu =
    new SwingMenu(I18N.gui.get("menu.file.export"))
  exportMenu.addMenuItem(new ExportWorldAction())
  exportMenu.addMenuItem(new ExportPlotAction())
  exportMenu.addMenuItem(new ExportAllPlotsAction())
  exportMenu.addMenuItem(new ExportGraphicsAction())
  exportMenu.addMenuItem(new ExportInterfaceAction())
  exportMenu.addMenuItem(new ExportOutputAction())
  add(exportMenu)
  addSeparator()
  val importMenu =
    new org.nlogo.swing.Menu(I18N.gui.get("menu.file.import"))
  importMenu.addMenuItem(new ImportWorldAction())
  importMenu.addMenuItem(new ImportPatchColorsAction())
  importMenu.addMenuItem(new ImportPatchColorsRGBAction())
  if (! Version.is3D) {
    importMenu.addMenuItem(new ImportDrawingAction())
  }
  importMenu.addMenuItem(new ImportClientAction())

  add(importMenu)

  if (! System.getProperty("os.name").startsWith("Mac")) {
    addSeparator()
    addMenuItem('Q', new QuitAction())
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

  private class NewAction extends FileMenuAction(I18N.gui.get("menu.file.new")) {
    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      offerSave()
      newModel()
    }
  }

  private class OpenAction extends FileMenuAction(I18N.gui.get("menu.file.open")) {
    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      offerSave()
      openFromPath(userChooseLoadPath(), ModelType.Normal)
    }
  }

  private class ModelsLibraryAction extends FileMenuAction(I18N.gui.get("menu.file.modelsLibrary")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      offerSave()
      val sourceURI =
        ModelsLibraryDialog.open(NLogoHierarchy.getFrame(FileMenu.this))
      openFromURI(sourceURI, ModelType.Library)
    }
  }

  private class SaveModelingCommonsAction extends FileMenuAction(I18N.gui.get("menu.file.uploadMc")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      //Very verbosely named method, this is called in doSave before calling modelSaver.doSave
      //so we call it here before saving to Modeling Commons as well
      checkWithUserBeforeSavingModelFromOldVersion()
      app.modelingCommons.saveToModelingCommons()
    }
  }

  private class SaveAction extends FileMenuAction(I18N.gui.get("menu.file.save")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      save()
    }
  }

  private class SaveAsAction extends FileMenuAction(I18N.gui.get("menu.file.saveAs")) {
    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      saveAs()
    }
  }

  private class SaveAsNetLogoWebAction extends FileMenuAction(I18N.gui.get("menu.file.saveAsNetLogoWeb")) {
    // disabled for 3-D since you can't do that in NetLogo Web - RG 9/10/15
    setEnabled(! Version.is3D)

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.show(
        FileMenu.this,
        I18N.gui.get("menu.file.saveAsNetLogoWeb.dialog"),
        AWTFileDialog.SAVE, suggestedFileName)

      val exportFile = new File(exportPath)
      val saver = NetLogoWebSaver(exportPath)
      saver.save(modelToSave, exportFile.getName)
    }

    @throws(classOf[UserCancelException])
    def suggestedFileName: String = {
      if (app.workspace.getModelType == ModelType.New)
        suggestedSaveFileName("") + ".html"
      else
        guessFileName.stripSuffix(".nlogo") + ".html"
    }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    def modelToSave: String = {
      val lastSaved =
        FileIO.file2String(app.workspace.getModelPath)
      if (doesNotMatchWorkingCopy(lastSaved) && userWantsLastSaveExported())
        lastSaved
      else
        modelSaver.save
    }

    @throws(classOf[UserCancelException])
    private def userWantsLastSaveExported(): Boolean = {
      val modelType = app.workspace.getModelType;
      val typeKey =
        if (modelType == ModelType.Normal) "fromSave" else "fromLibrary"
      val options = Array[Object](
        I18N.gui.get("menu.file.nlw.prompt." + typeKey),
        I18N.gui.get("menu.file.nlw.prompt.fromCurrentCopy"),
        I18N.gui.get("common.buttons.cancel"))
      val title   = I18N.gui.get("menu.file.nlw.prompt.title")
      val message = I18N.gui.get("menu.file.nlw.prompt.message." + typeKey)
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
    private def doesNotMatchWorkingCopy(lastSaved: String): Boolean = {
      lastSaved != modelSaver.save
    }
  }

  abstract class ExportAction(taskName: String, suggestedFileName: String)
    extends FileMenuAction(I18N.gui.get("menu.file.export." + taskName)) {

    def exportTask(path: String): Runnable

    var exception = Option.empty[IOException]

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val exportPath = FileDialog.show(
        FileMenu.this,
        I18N.gui.get(s"menu.file.export.$taskName.dialog"),
        AWTFileDialog.SAVE, app.workspace.guessExportName(suggestedFileName))
      exception = None

      ModalProgressTask.apply(
          NLogoHierarchy.getFrame(FileMenu.this),
          I18N.gui.get("dialog.interface.export.task"), exportTask(exportPath))

      exception.foreach(throw _)
    }
  }

  private class ExportWorldAction extends ExportAction("world", "world.csv") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        try {
          app.workspace.exportWorld(exportPath)
        } catch {
          case ex: IOException => exception = Some(ex)
        }
      }
    }
  }

  private class ExportGraphicsAction extends ExportAction("view", "view.png") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        try {
          app.workspace.exportView(exportPath, "png");
        } catch {
          case ex: IOException => exception = Some(ex)
        }
      }
    }
  }

  private class ExportInterfaceAction extends ExportAction("interface", "interface.png") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        try {
          app.workspace.exportInterface(exportPath)
        } catch  {
          case ex: IOException => exception = Some(ex)
        }
      }
    }
  }

  private class ExportOutputAction extends ExportAction("output", "output.txt") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        new ExportOutputEvent(exportPath).raise(FileMenu.this)
      }
    }
  }

  private class ExportPlotAction extends ExportAction("plot", "plot.csv") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        new ExportPlotEvent(PlotWidgetExportType.PROMPT, null, exportPath)
          .raise(FileMenu.this)
      }
    }
  }

  private class ExportAllPlotsAction extends ExportAction("allPlots", "plots.csv") {
    def exportTask(exportPath: String) = new Runnable() {
      override def run(): Unit = {
        new ExportPlotEvent(PlotWidgetExportType.ALL, null, exportPath)
          .raise(FileMenu.this)
      }
    }
  }

  abstract class ImportAction(taskName: String)
    extends FileMenuAction(I18N.gui.get(s"menu.file.import.$taskName")) {
    var exception = Option.empty[IOException]

    def importTask(importPath: String): Runnable

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      exception = None
      val importPath = FileDialog.show(
          FileMenu.this, I18N.gui.get(s"menu.file.import.$taskName.dialog"), AWTFileDialog.LOAD, null)

      ModalProgressTask(NLogoHierarchy.getFrame(FileMenu.this),
        I18N.gui.get("dialog.interface.import.task"),
        importTask(importPath))
      exception.foreach(throw _)
    }
  }

  private class ImportWorldAction extends ImportAction("world") {
    def importTask(importPath: String) = {
      new Runnable() {
        override def run(): Unit = {
          try {
            app.workspace.importWorld(importPath)
            app.workspace.view.dirty()
            app.workspace.view.repaint()
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }
    }
  }

  private class ImportPatchColorsAction extends ImportAction("patchColors") {
    def importTask(importPath: String) = {
      new Runnable() {
        def run(): Unit = {
          try {
            // We can't wait for the thread to complete, or we end
            // up locking up the app since the Model Dialog and the
            // job wedge against one another. -- CLB 07/19/05
            ImportPatchColors.importPatchColors(
              new LocalFile(importPath),
              app.workspace.world, true)
            app.workspace.view.dirty()
            app.workspace.view.repaint()
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }
    }
  }

  private class ImportPatchColorsRGBAction extends ImportAction("patchColorsRGB") {
    def importTask(importPath: String) = {
      new Runnable() {
        override def run(): Unit = {
          try {
            // We can't wait for the thread to complete, or we end
            // up locking up the app since the Model Dialog and the
            // job wedge against one another. -- CLB 07/19/05
            ImportPatchColors.importPatchColors(
              new LocalFile(importPath), app.workspace.world, false)
            app.workspace.view.dirty()
            app.workspace.view.repaint()
          } catch  {
            case ex: IOException => exception = Some(ex)
          }
        }
      }
    }
  }

  private class ImportDrawingAction extends ImportAction("drawing") {
    def importTask(importPath: String) = {
      new Runnable() {
        override def run(): Unit = {
          try {
            app.workspace.importDrawing(importPath);
            app.workspace.view.dirty();
            app.workspace.view.repaint();
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }
    }
  }

  private class ImportClientAction extends FileMenuAction(I18N.gui.get("menu.file.import.hubNetClientInterface")) {
    var exception = Option.empty[IOException]

    def importTask(importPath: String, sectionChoice: Int): Runnable =
      new Runnable() {
        def run(): Unit = {
          try {
            loadModel(Paths.get(importPath).toUri).map(model =>
              app.workspace.getHubNetManager.foreach(_.importClientInterface(model, sectionChoice == 1)))
          } catch {
            case ex: IOException => exception = Some(ex)
          }
        }
      }

    @throws(classOf[UserCancelException])
    @throws(classOf[IOException])
    override def action(): Unit = {
      val importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, I18N.gui.get("menu.file.import.hubNetClientInterface.dialog"), java.awt.FileDialog.LOAD, null);
      val choice =
          OptionDialog.show(app.workspace.getFrame,
                  I18N.gui.get("menu.file.import.hubNetClientInterface.message"),
                  I18N.gui.get("menu.file.import.hubNetClientInterface.prompt"),
                  Array[Object](
    I18N.gui.get("menu.file.import.hubNetClientInterface.fromInterface"),
    I18N.gui.get("menu.file.import.hubNetClientInterface.fromClient"),
    I18N.gui.get("common.buttons.cancel")))

      if (choice != 2) {
        ModalProgressTask.apply(
          NLogoHierarchy.getFrame(FileMenu.this),
          I18N.gui.get("dialog.interface.import.task"),
          importTask(importPath, choice))
        exception.foreach(throw _)
      }
    }
  }

  private class QuitAction extends FileMenuAction(I18N.gui.get("menu.file.quit")) {
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
    app.workspace.getExtensionManager.reset()
    System.exit(0)
  }

  ///

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogo" otherwise.
   */
  private def guessFileName: String = {
    val fileName = app.workspace.getModelFileName
    if (fileName == null)
      "Untitled." + modelSuffix
    else
      fileName
  }

  /// model, how shall I load thee?  let me count the ways

  @throws(classOf[UserCancelException])
  @throws(classOf[IOException])
  def newModel(): Unit = {
    try {
      openFromURI(getClass.getResource(emptyModelPath).toURI, ModelType.New);
    } catch  {
      case ex: URISyntaxException =>
        println("Invalid model path: " + emptyModelPath)
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
    println("loading from URI " + uri.toString)
    val controller = new FileController(this)
    OpenModel(uri, controller, modelLoader, Version)
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
      ModalProgressTask(
          NLogoHierarchy.getFrame(this), I18N.gui.get("dialog.interface.loading.task"), loader)
      app.tabs.requestFocus()
    }
    savedVersion = model.version // maybe the whole model should be stored?
  }

  private def runLoad(linkParent: Container, uri: URI, model: Model, modelType: ModelType): Unit = {
    ReconfigureWorkspaceUI(linkParent, uri, modelType, model, app.workspace)
  }

  def handle(e: OpenModelEvent): Unit = {
    openFromPath(e.path, ModelType.Library)
  }

  /// saving
  @throws(classOf[UserCancelException])
  private def save(): Unit = {
    if (app.workspace.forceSaveAs)
      saveAs()
    else
      doSave(app.workspace.getModelPath)
  }

  @throws(classOf[UserCancelException])
  private def saveAs(): Unit = {
    doSave(userChooseSavePath())
  }

  private class Saver(val path: String) extends Runnable {

    private var result = true
    private var exception = Option.empty[IOException]

    def getResult: Boolean = result

    def getException: Option[IOException] = exception

    def run(): Unit = {
      try {
        FileIO.writeFile(path, modelSaver.save)
        new ModelSavedEvent(path).raise(FileMenu.this)
      } catch {
        case ex: IOException =>
          result = false
          exception = Some(ex)
          // we don't want to call JOptionPane.showMessageDialog() here
          // because Java on Macs tends to barf when multiple modal dialogs
          // appear on top of each other, so we just hang onto the exception
          // until the modal progress task is done... - ST 11/3/04
      }
    }
  }

  @throws(classOf[UserCancelException])
  private def doSave(path: String): Unit = {
    checkWithUserBeforeSavingModelFromOldVersion()
    val saver = new Saver(path)

    ModalProgressTask(NLogoHierarchy.getFrame(this),
      I18N.gui.get("dialog.interface.saving.task"), saver)
    saver.getException.foreach { e =>
      JOptionPane.showMessageDialog(this,
        I18N.gui.getN("menu.file.save.error", e.getMessage),
        "NetLogo", JOptionPane.ERROR_MESSAGE)
    }

    if (!saver.getResult)
      throw new UserCancelException()

    app.tabs.saveExternalFiles()
  }

  /// and now, a whole bunch of dialog boxes

  // this is called whenever a workspace is about to be destroyed
  @throws(classOf[UserCancelException])
  def offerSave(): Unit = {
    // check if we have an open movie
    if (app.workspace.movieEncoder != null) {
      val options = Array[Object](
        I18N.gui.get("common.buttons.ok"),
        I18N.gui.get("common.buttons.cancel"))
      val message =
        I18N.gui.get("file.close.warn.movieInProgress")
      if (OptionDialog.show(this, "NetLogo", message, options) == 1) {
        throw new UserCancelException()
      }
      app.workspace.movieEncoder.cancel();
      app.workspace.movieEncoder = null;
    }

    if (app.dirtyMonitor.dirty && userWantsToSaveFirst()) {
      save()
    }
  }

  @throws(classOf[UserCancelException])
  private def suggestedSaveFileName(suffix: String): String = {
    // first, force the user to save.
    save()

    // we use workspace.getModelFileName() here, because it really should
    // never any longer be null, now that we've forced the user to save.
    // it's important that it not be, in fact, since the applet relies
    // on the model having been saved to some file.
    var suggestedFileName = app.workspace.getModelFileName;

    // don't add the suffix on twice
    if (suggestedFileName.endsWith(s".$modelSuffix"))
      suggestedFileName
    else
      suggestedFileName + suffix
  }

  @throws(classOf[UserCancelException])
  private def userChooseLoadPath(): String = {
    FileDialog.show(this, I18N.gui.get("menu.file.open.dialog"),
        AWTFileDialog.LOAD, null)
  }

  @throws(classOf[UserCancelException])
  private def userChooseSavePath(): String = {
    val newFileName = guessFileName

    // we only default to saving in the model dir for normal and
    // models. for library and new models, we use the current
    // FileDialog dir.
    if (app.workspace.getModelType == ModelType.Normal) {
      FileDialog.setDirectory(app.workspace.getModelDir)
    }

    var path = FileDialog.show(
      this, I18N.gui.get("menu.file.saveAs.dialog"), AWTFileDialog.SAVE,
      newFileName)
    if(! path.endsWith("." + modelSuffix)) {
      path += "." + modelSuffix
    }
    path
  }

  @throws(classOf[UserCancelException])
  private def userWantsToSaveFirst(): Boolean = {
    val options = Array[Object](
      I18N.gui.get("common.buttons.save"),
      I18N.gui.get("common.buttons.discard"),
      I18N.gui.get("common.buttons.cancel"))

    val message = I18N.gui.get("menu.file.save.confirm")

    OptionDialog.show(this, "NetLogo", message, options) match {
      case 0 => true
      case 1 =>false
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
