// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.IOException

import org.nlogo.api.{ I18N, ModelReader, ModelSection, ModelType }
import org.nlogo.api.ModelReader.{ modelSuffix, emptyModelPath }
import org.nlogo.awt.UserCancelException
import org.nlogo.window.InvalidVersionException

import scala.annotation.{strictfp, switch}

/*
 * note that multiple instances of this class may exist
 * as there are now multiple frames that each have their own menu bar
 * and menus ev 8/25/05
 */

@strictfp class FileMenu(private val app: App, private val modelSaver: ModelSaver)
    extends org.nlogo.swing.Menu(I18N.gui.get("menu.file"))
    with org.nlogo.window.Events.OpenModelEventHandler {
  implicit val i18nName = I18N.Prefix("menu.file")

  setMnemonic('F')
  addMenuItem('N', new NewAction)
  addMenuItem('O', new OpenAction)
  addMenuItem('M', new ModelsLibraryAction)
  add(new RecentFilesMenu(app, this))
  addSeparator()
  addMenuItem('S', new SaveAction)
  addMenuItem('S', true, new SaveAsAction)
  addMenuItem(new SaveModelingCommonsAction)
  addSeparator()
  addMenuItem(I18N.gui("print"), 'P', app.tabs.printAction)
  addSeparator()
  val exportMenu = new org.nlogo.swing.Menu(I18N.gui("export"))
  exportMenu.addMenuItem(new ExportWorldAction)
  exportMenu.addMenuItem(new ExportPlotAction)
  exportMenu.addMenuItem(new ExportAllPlotsAction)
  exportMenu.addMenuItem(new ExportGraphicsAction)
  exportMenu.addMenuItem(new ExportInterfaceAction)
  exportMenu.addMenuItem(new ExportOutputAction)
  exportMenu.addMenuItem(new ExportCodeAction)
  add(exportMenu)
  addSeparator()
  val importMenu = new org.nlogo.swing.Menu(I18N.gui("import"))
  importMenu.addMenuItem(new ImportWorldAction)
  importMenu.addMenuItem(new ImportPatchColorsAction)
  importMenu.addMenuItem(new ImportPatchColorsRGBAction)
  if (!org.nlogo.api.Version.is3D) importMenu.addMenuItem(new ImportDrawingAction)
  importMenu.addMenuItem(new ImportClientAction)
  add(importMenu)
  if (!System.getProperty("os.name").startsWith("Mac")) {
    addSeparator()
    addMenuItem('Q', new QuitAction())
  }
  // initialize here, unless there's a big problem early on in the
  // initial load process it'll get initialize properly below
  // maybe this fixes Nigel Gilbert's bug. maybe. ev 1/30/07
  private var savedVersion = org.nlogo.api.Version.version

///

  private abstract class FileMenuAction(name: String) extends javax.swing.AbstractAction(name) {
    def action(): Unit

    def actionPerformed(e: java.awt.event.ActionEvent) {
      try {
        action()
      } catch {
        case ex:UserCancelException => org.nlogo.util.Exceptions.ignore(ex)
        case ex:IOException =>
          javax.swing.JOptionPane.showMessageDialog(
            FileMenu.this, ex.getMessage, I18N.gui.get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE)
      }
    }
  }

  private abstract class ImportMenuAction(name: String) extends javax.swing.AbstractAction(name) {
    def action(): Unit

    def actionPerformed(e: java.awt.event.ActionEvent) {
      try {
        action()
      } catch {
        case ex:UserCancelException => org.nlogo.util.Exceptions.ignore(ex)
        case ex:IOException =>
          javax.swing.JOptionPane.showMessageDialog(
            FileMenu.this, ex.getMessage, I18N.gui.get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE)
      }
    }
  }

  private class NewAction extends FileMenuAction(I18N.gui("new")) {
    def action() {
      offerSave()
      newModel()
    }
  }

  private class OpenAction extends FileMenuAction(I18N.gui("open")) {
    def action() {
      offerSave()
      openFromPath(userChooseLoadPath(), ModelType.Normal)
    }
  }

  private class ModelsLibraryAction extends FileMenuAction(I18N.gui("modelsLibrary")) {
    def action() {
      offerSave()
      val source = ModelsLibraryDialog.open(org.nlogo.awt.Hierarchy.getFrame(FileMenu.this))
      val modelPath = ModelsLibraryDialog.getModelPath
      openFromSource(source, modelPath, "Loading...", ModelType.Library)
    }
  }

  private class SaveModelingCommonsAction extends FileMenuAction("Upload To Modeling Commons") {
    def action() {
      //Very verbosely named method, this is called in doSave before calling modelSaver.doSave
      //so we call it here before saving to Modeling Commons as well
      checkWithUserBeforeSavingModelFromOldVersion()
      app.modelingCommons.saveToModelingCommons()
    }
  }

  private class SaveAction extends FileMenuAction(I18N.gui("save")) {
    def action() {
      save()
    }
  }

  private class SaveAsAction extends FileMenuAction(I18N.gui("saveAs")) {
    def action() {
      saveAs()
    }
  }

  private class ExportWorldAction extends FileMenuAction(I18N.gui("export.world")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export World", java.awt.FileDialog.SAVE, app.workspace.guessExportName("world.csv"))
      TaskWarning.maybeWarn(org.nlogo.awt.Hierarchy.getFrame(FileMenu.this), app.workspace.world)
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask
        (org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
         "Exporting...",
         new Runnable {
           def run() {
             try app.workspace.exportWorld(exportPath)
             catch {
               case ex:IOException => exception = Some(ex)
           }}})
      exception foreach (throw _)
    }
  }

  private class ExportGraphicsAction extends FileMenuAction(I18N.gui("export.view")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export View", java.awt.FileDialog.SAVE, app.workspace.guessExportName("view.png"))
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            try app.workspace.exportView(exportPath, "png")
            catch {
              case ex:IOException => exception = Some(ex)
          }}})
      exception foreach (throw _)
    }
  }

  private class ExportInterfaceAction extends FileMenuAction(I18N.gui("export.interface")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export Interface", java.awt.FileDialog.SAVE, app.workspace.guessExportName("interface.png"))
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            try app.workspace.exportInterface(exportPath)
            catch {
              case ex: IOException => exception = Some(ex)
            }}})
      exception foreach (throw _)
    }
  }

  private class ExportOutputAction extends FileMenuAction(I18N.gui("export.output")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export Output", java.awt.FileDialog.SAVE, app.workspace.guessExportName("output.txt"))
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            new org.nlogo.window.Events.ExportOutputEvent(exportPath)
              .raise(FileMenu.this)
          }})
    }
  }

  private class ExportPlotAction extends FileMenuAction(I18N.gui("export.plot")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export Plot", java.awt.FileDialog.SAVE, app.workspace.guessExportName("plot.csv"))
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            new org.nlogo.window.Events.ExportPlotEvent(org.nlogo.window.PlotWidgetExportType.PROMPT, null, exportPath)
              .raise(FileMenu.this)
          }})
    }
  }

  private class ExportAllPlotsAction extends FileMenuAction(I18N.gui("export.allPlots")) {
   def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export All Plots", java.awt.FileDialog.SAVE, app.workspace.guessExportName("plots.csv"))
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            new org.nlogo.window.Events.ExportPlotEvent(org.nlogo.window.PlotWidgetExportType.ALL, null, exportPath)
              .raise(FileMenu.this)
          }})
    }
  }

  private class ExportCodeAction extends FileMenuAction(I18N.gui("export.code")) {
    def action() {
      val exportPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Export Code", java.awt.FileDialog.SAVE, app.workspace.guessExportName("code.html"));
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable {
          def run() {
            new org.nlogo.window.Events.ExportCodeEvent(exportPath)
              .raise(FileMenu.this);
        }});
    }
  }

  private class ImportWorldAction extends ImportMenuAction(I18N.gui("import.world")) {
    def action() {
      val importPath = org.nlogo.swing.FileDialog.show(FileMenu.this, "Import World", java.awt.FileDialog.LOAD, null)
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing...",
        new Runnable {
          def run() {
            try {
              app.workspace.importWorld(importPath)
              app.workspace.view.dirty()
              app.workspace.view.repaint()
            }
            catch {
              case ex:IOException => exception = Some(ex)
           }}})
      exception foreach (throw _)
    }
  }

  private class ImportPatchColorsAction extends ImportMenuAction(I18N.gui("import.patchColors")) {
    def action() {
      val importPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Import Patch Colors", java.awt.FileDialog.LOAD, null)
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Patch Colors...",
        new Runnable {
          def run() {
            try {
              // We can't wait for the thread to complete, or we end
              // up locking up the app since the Model Dialog and the
              // job wedge against one another. -- CLB 07/19/05
              org.nlogo.agent.ImportPatchColors.importPatchColors(
                new org.nlogo.api.LocalFile(importPath),
                app.workspace.world, true)
              app.workspace.view.dirty()
              app.workspace.view.repaint()
            }
            catch {
              case ex:IOException => exception = Some(ex)
          }}})
      exception foreach (throw _)
    }
  }

  private class ImportPatchColorsRGBAction extends ImportMenuAction(I18N.gui("import.patchColorsRGB")) {
    def action() {
      val importPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Import Patch Colors RGB", java.awt.FileDialog.LOAD, null)
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Patch Colors...",
        new Runnable {
          def run() {
            try {
              // We can't wait for the thread to complete, or we end
              // up locking up the app since the Model Dialog and the
              // job wedge against one another. -- CLB 07/19/05
              org.nlogo.agent.ImportPatchColors.importPatchColors(
                new org.nlogo.api.LocalFile(importPath),
                app.workspace.world, false)
              app.workspace.view.dirty()
              app.workspace.view.repaint()
            }
            catch {
              case ex:IOException => exception = Some(ex)
          }}})
      exception foreach (throw _)
    }
  }

  private class ImportDrawingAction extends ImportMenuAction(I18N.gui("import.drawing")) {
    def action() {
      val importPath = org.nlogo.swing.FileDialog.show(FileMenu.this, "Import Drawing", java.awt.FileDialog.LOAD, null)
      var exception = None: Option[IOException]
      org.nlogo.swing.ModalProgressTask(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Drawing...",
        new Runnable {
          def run() {
            try {
              app.workspace.importDrawing(importPath)
              app.workspace.view.dirty()
              app.workspace.view.repaint()
            }
            catch {
              case ex:IOException => exception = Some(ex)
          }}})
      exception foreach (throw _)
    }
  }

  private class ImportClientAction extends ImportMenuAction(I18N.gui("import.hubNetClientInterface")) {
    def action() {
      val importPath = org.nlogo.swing.FileDialog.show(
        FileMenu.this, "Import HubNet Client Interface", java.awt.FileDialog.LOAD, null)
      var exception = None: Option[IOException]
      val choice =
        org.nlogo.swing.OptionDialog.show(
          app.workspace.getFrame,
          "Import HubNet Client",
          "Which section would you like to import from?",
          Array("Interface Tab", "HubNet client", I18N.gui.get("common.buttons.cancel")))

      if (choice != 2) {
        org.nlogo.swing.ModalProgressTask(
          org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
          "Importing Drawing...",
          new Runnable {
            def run() {
              try app.workspace.getHubNetManager.importClientInterface(importPath, choice == 1)
              catch {
                case ex:IOException => exception = Some(ex)
            }}})
        exception foreach (throw _)
      }
    }
  }

  private class QuitAction extends FileMenuAction(I18N.gui("quit")) {
    def action() {
      try quit()
      catch {
        case ex:UserCancelException => org.nlogo.util.Exceptions.ignore(ex)
      }
    }
  }

  ///

  def quit() {
    offerSave()
    new org.nlogo.window.Events.AboutToQuitEvent().raise(this)
    app.workspace.getExtensionManager.reset()
    System.exit(0)
  }

  ///

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogo" otherwise.
   */
  private def guessFileName = {
    val fileName = app.workspace.getModelFileName
    if (fileName == null) "Untitled." + modelSuffix else fileName
  }

  /// model, how shall I load thee?  let me count the ways

  @throws[UserCancelException] @throws[IOException]
  def newModel() {
    openFromURL(emptyModelPath)
  }

  /**
   * opens a model from a URL. Currently, this is only used to create a new
   * model (load the new model template).
   */
  private def openFromURL(model: String) {
    val source = org.nlogo.util.Utils.url2String(model)
    if (model == emptyModelPath) {
      openFromSource(source, null, "Clearing...", ModelType.New)
    } else {
      // models loaded from URLs are treated as library models, since
      // they are read-only. This is currently never used, so I'm
      // not even sure it's what we would really want...
      openFromSource(source, null, "Loading...", ModelType.Library)
    }
  }

  def handle(e: org.nlogo.window.Events.OpenModelEvent) {
    try {
      openFromPath(e.path, ModelType.Library)
    } catch {
      case ex:IOException => throw new IllegalStateException(ex)
    }
  }

  /**
   * opens a model from a file path.
   */
  @throws[IOException]
  def openFromPath(path: String, modelType: ModelType) {
    try {
      val source = org.nlogo.api.FileIO.file2String(path)
      if (source == null) throw new IllegalStateException(s"couldn't open:'$path'")
      openFromSource(source, path, "Loading...", modelType)
    } catch {
      case ex:UserCancelException => org.nlogo.util.Exceptions.ignore(ex)
    }
  }

  /**
   * opens a NetLogo model from the previously loaded source. This
   * is complicated and I'm not totally sure I understand it, but it really
   * should be documented...
   *
   * @param source    the model source. May not be null.
   * @param path      the full pathname of the model, including the file. For
   *                  example: "/home/mmh/models/My_Model.nlogo". This may be null, if,
   *                  for example, this is a new model, or the origin is unknown.
   * @param message   the message to display in the "loading" modal dialog.
   * @param modelType the type of this model. Must be one of the types
   *                  defined in org.nlogo.workspace.Workspace.
   */
  @throws[UserCancelException]
  def openFromSource(source: String, path: String, message: String, modelType: ModelType) {
    // map elements are { source, info, resources, version }
    val map: scala.collection.immutable.Map[ModelSection, scala.collection.Seq[String]] = ModelReader.parseModel(source)
    if (map == null || map(ModelSection.Version).isEmpty) notifyUserNotValidFile()
    val version = org.nlogo.api.ModelReader.parseVersion(map)
    if (version == null || !version.startsWith("NetLogo")) notifyUserNotValidFile()
    if (org.nlogo.api.Version.is3D && !org.nlogo.api.Version.is3D(version)) checkWithUserBeforeOpening2DModelin3D()
    if (!org.nlogo.api.Version.is3D && org.nlogo.api.Version.is3D(version)) {
      checkWithUserBeforeOpening3DModelin2D(version)
    } else if (!org.nlogo.api.Version.knownVersion(version)) {
        checkWithUserBeforeOpeningModelFromFutureVersion(version)
    }
    openFromMap(map, path, message, modelType)
    savedVersion = version
  }

  private var firstLoad = true

  private def openFromMap(map: scala.collection.immutable.Map[ModelSection, scala.collection.Seq[String]],
                           path: String, message: String, modelType: ModelType) {
    try {
      if (firstLoad) {
        firstLoad = false
        // app frame isn't even showing yet, so no need for ModalProgressTask
        org.nlogo.window.ModelLoader.load(this, path, modelType, map)
      } else {
        val loader = new Runnable {
          def run() {
            try {
              org.nlogo.window.ModelLoader.load(FileMenu.this, path, modelType, map)
            } catch {
              // we've already checked the version
              // so I don't expect this ever to happen
              // but in case it does...
              case ex:InvalidVersionException => new IllegalStateException(ex)
            }
          }
        }
        org.nlogo.swing.ModalProgressTask(org.nlogo.awt.Hierarchy.getFrame(this), message, loader)
        app.tabs.requestFocus()
      }
    } catch {
      // we've already checked the version
      case ex:InvalidVersionException => throw new IllegalStateException(ex);
    }
  }

  /// saving

  private def save() {
    if (app.workspace.forceSaveAs) {
      saveAs()
    } else {
      doSave(app.workspace.getModelPath)
    }
  }

  private def saveAs() {
    doSave(userChooseSavePath())
  }

  private class Saver(private val path: String) extends Runnable {
    private var _result = true
    private var _exception = None: Option[IOException]

    def result = _result
    def exception = _exception

    def run() {
      try {
        org.nlogo.api.FileIO.writeFile(path, modelSaver.save)
        new org.nlogo.window.Events.ModelSavedEvent(path)
            .raise(FileMenu.this)
      } catch {
        case ex:IOException =>
          _result = false
          _exception = Some(ex)
          // we don't want to call JOptionPane.showMessageDialog() here
          // because Java on Macs tends to barf when multiple modal dialogs
          // appear on top of each other, so we just hang onto the exception
          // until the modal progress task is done... - ST 11/3/04
      }
    }
  }

  private def doSave(path: String) {
    checkWithUserBeforeSavingModelFromOldVersion()
    val saver = new Saver(path)
    org.nlogo.swing.ModalProgressTask(org.nlogo.awt.Hierarchy.getFrame(this), "Saving...", saver)
    saver.exception foreach (ex => javax.swing.JOptionPane.showMessageDialog(
      this, s"Save failed.  Error: ${ex.getMessage}", "NetLogo", javax.swing.JOptionPane.ERROR_MESSAGE))
    if (!saver.result) throw new UserCancelException()
    app.tabs.saveExternalFiles()
  }

  /// and now, a whole bunch of dialog boxes

  // this is called whenever a workspace is about to be destroyed
  @throws[UserCancelException]
  def offerSave() {
    // check if we have an open movie
    if (app.workspace.movieEncoder != null) {
      val options = Array[AnyRef](I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.cancel"))
      val message = "There is a movie in progress. " +
        "Are you sure you want to exit this model? " +
        "You will lose the contents of your movie."
      if (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options) == 1) throw new UserCancelException
      app.workspace.movieEncoder.cancel()
      app.workspace.movieEncoder = null
    }

    if (app.dirtyMonitor.dirty && userWantsToSaveFirst()) save()
  }

  @throws[UserCancelException]
  def userChooseLoadPath() = org.nlogo.swing.FileDialog.show(this, "Open", java.awt.FileDialog.LOAD, null)

  @throws[UserCancelException]
  def userChooseSavePath() = {
    val newFileName = guessFileName
    var newDirectoryName = null: String
    if (app.workspace.getModelType == ModelType.Normal) {
      // we only default to saving in the model dir for normal and
      // models. for library and new models, we use the current
      // FileDialog dir.
      newDirectoryName = app.workspace.getModelDir
    }
    org.nlogo.swing.FileDialog.setDirectory(newDirectoryName)
    val path = org.nlogo.swing.FileDialog.show(this, "Save As", java.awt.FileDialog.SAVE, newFileName)
    if(!path.endsWith(s".$modelSuffix")) s"$path.$modelSuffix" else path
  }

  private def userWantsToSaveFirst() = {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.save"), "Discard", I18N.gui.get("common.buttons.cancel"))
    val message = "Do you want to save the changes you made to this model?"
    (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options): @switch) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }

  private def checkWithUserBeforeSavingModelFromOldVersion() {
    if (!org.nlogo.api.Version.compatibleVersion(savedVersion)) {
      val options = Array[AnyRef](I18N.gui("common.buttons.save"), I18N.gui.get("common.buttons.cancel"))
      val message = s"This model was made with $savedVersion. " +
        s"If you save it in ${org.nlogo.api.Version.version} " +
        "it may not work in the old version anymore."
      if (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options) != 0) throw new UserCancelException
      savedVersion = org.nlogo.api.Version.version
    }
  }

  private def checkWithUserBeforeOpeningModelFromFutureVersion(version: String) {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    val message = "You are attempting to open a model that was created in a newer version of NetLogo.  " +
      s"(This is ${org.nlogo.api.Version.version}; the model was created in $version.) " +
      "NetLogo can try to open the model, but it may or may not work."
    if (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options) != 0) throw new UserCancelException
  }

  private def checkWithUserBeforeOpening3DModelin2D(version: String) {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    val message = "You are attempting to open a model that was created in a 3D version of NetLogo.  " +
      s"(This is ${org.nlogo.api.Version.version}; the model was created in $version.) " +
      "NetLogo can try to open the model, but it may or may not work."
    if (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options) != 0) throw new UserCancelException
  }

  private def checkWithUserBeforeOpening2DModelin3D() {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
    val message = s"You are attempting to open a 2D model in ${org.nlogo.api.Version.version}. " +
      "You might need to make changes before it will work in 3D."
    if (org.nlogo.swing.OptionDialog.show(this, "NetLogo", message, options) != 0) throw new UserCancelException
  }

  private def notifyUserNotValidFile() {
    val options = Array[AnyRef](I18N.gui.get("common.buttons.ok"))
    org.nlogo.swing.OptionDialog.show(this, "NetLogo", "The file is not a valid NetLogo model file.", options)
    throw new UserCancelException
  }
}
