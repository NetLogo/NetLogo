// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.util.Map;

import org.nlogo.api.I18N;
import org.nlogo.api.ModelReader;
import org.nlogo.api.ModelSection;
import org.nlogo.api.ModelSectionJ;
import org.nlogo.api.ModelType;
import org.nlogo.api.ModelTypeJ;
import static org.nlogo.api.ModelReader.modelSuffix;
import static org.nlogo.api.ModelReader.emptyModelPath;
import org.nlogo.awt.UserCancelException;

import org.nlogo.modelingcommons.ModelingCommons;

/*
 * note that multiple instances of this class may exist
 * as there are now multiple frames that each have their own menu bar
 * and menus ev 8/25/05
 */

public strictfp class FileMenu
    extends org.nlogo.swing.Menu
    implements org.nlogo.window.Events.OpenModelEvent.Handler {

  private final App app;
  private final ModelSaver modelSaver;
  private final AppletSaver appletSaver;

  ///

  public FileMenu(App app, ModelSaver modelSaver, AppletSaver appletSaver) {
    super(I18N.guiJ().get("menu.file"));
    this.app = app;
    this.modelSaver = modelSaver;
    this.appletSaver = appletSaver;
    addMenuItem('N', new NewAction());
    addMenuItem('O', new OpenAction());
    addMenuItem('M', new ModelsLibraryAction());
    addSeparator();
    addMenuItem('S', new SaveAction());
    addMenuItem('S', true, new SaveAsAction());
    addMenuItem(new SaveAppletAction());
    addMenuItem(new SaveModelingCommonsAction());

    addSeparator();
    addMenuItem(I18N.guiJ().get("menu.file.print"), 'P', app.tabs().printAction());
    addSeparator();
    org.nlogo.swing.Menu exportMenu =
        new org.nlogo.swing.Menu(I18N.guiJ().get("menu.file.export"));
    exportMenu.addMenuItem(new ExportWorldAction());
    exportMenu.addMenuItem(new ExportPlotAction());
    exportMenu.addMenuItem(new ExportAllPlotsAction());
    exportMenu.addMenuItem(new ExportGraphicsAction());
    exportMenu.addMenuItem(new ExportInterfaceAction());
    exportMenu.addMenuItem(new ExportOutputAction());
    add(exportMenu);
    addSeparator();
    org.nlogo.swing.Menu importMenu =
        new org.nlogo.swing.Menu(I18N.guiJ().get("menu.file.import"));
    importMenu.addMenuItem(new ImportWorldAction());
    importMenu.addMenuItem(new ImportPatchColorsAction());
    importMenu.addMenuItem(new ImportPatchColorsRGBAction());
    if (!org.nlogo.api.Version.is3D()) {
      importMenu.addMenuItem(new ImportDrawingAction());
    }
    importMenu.addMenuItem(new ImportClientAction());

    add(importMenu);
    if (!System.getProperty("os.name").startsWith("Mac")) {
      addSeparator();
      addMenuItem('Q', new QuitAction());
    }
    // initialize here, unless there's a big problem early on in the
    // initial load process it'll get initialize properly below
    // maybe this fixes Nigel Gilbert's bug. maybe. ev 1/30/07
    savedVersion = org.nlogo.api.Version.version();
  }

  ///

  private abstract class FileMenuAction
      extends javax.swing.AbstractAction {
    FileMenuAction(String name) {
      super(name);
    }

    abstract void action()
        throws UserCancelException, java.io.IOException;

    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        action();
      } catch (UserCancelException ex) {
        org.nlogo.util.Exceptions.ignore(ex);
      } catch (java.io.IOException ex) {
        javax.swing.JOptionPane.showMessageDialog
            (FileMenu.this, ex.getMessage(),
                I18N.guiJ().get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private abstract class ImportMenuAction
      extends javax.swing.AbstractAction {
    ImportMenuAction(String name) {
      super(name);
    }

    abstract void action()
        throws UserCancelException, java.io.IOException;

    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        action();
      } catch (UserCancelException ex) {
        org.nlogo.util.Exceptions.ignore(ex);
      } catch (java.io.IOException ex) {
        javax.swing.JOptionPane.showMessageDialog
            (FileMenu.this, ex.getMessage(),
                I18N.guiJ().get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class NewAction extends FileMenuAction {
    NewAction() {
      super(I18N.guiJ().get("menu.file.new"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      offerSave();
      newModel();
    }
  }

  private class OpenAction extends FileMenuAction {
    OpenAction() {
      super(I18N.guiJ().get("menu.file.open"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      offerSave();
      openFromPath
          (userChooseLoadPath(),
           ModelTypeJ.NORMAL());
    }
  }

  private class ModelsLibraryAction extends FileMenuAction {
    ModelsLibraryAction() {
      super(I18N.guiJ().get("menu.file.modelsLibrary"));
    }

    @Override
    void action()
        throws UserCancelException {
      offerSave();
      String source =
          ModelsLibraryDialog.open
              (org.nlogo.awt.Hierarchy.getFrame(FileMenu.this));
      String modelPath = ModelsLibraryDialog.getModelPath();
      openFromSource(source, modelPath, "Loading...",
                     ModelTypeJ.LIBRARY());
    }
  }

    private class SaveModelingCommonsAction extends FileMenuAction {
      SaveModelingCommonsAction(String title) {
      super(title);
      }
      SaveModelingCommonsAction() {
        this("Upload To Modeling Commons");
      }

        @Override
        void action() throws UserCancelException {
          checkWithUserBeforeSavingModelFromOldVersion();
          ModelingCommons communicator = new ModelingCommons(modelSaver, org.nlogo.awt.Hierarchy.getFrame(FileMenu.this), app);
          communicator.saveToModelingCommons();
        }
    }
  private class SaveAction extends FileMenuAction {
    SaveAction() {
      super(I18N.guiJ().get("menu.file.save"));
    }

    @Override
    void action()
        throws UserCancelException {
      save();
    }
  }

  private class SaveAsAction extends FileMenuAction {
    SaveAsAction() {
      super(I18N.guiJ().get("menu.file.saveAs"));
    }

    @Override
    void action()
        throws UserCancelException {
      saveAs();
    }
  }


  private class SaveAppletAction extends FileMenuAction {
    SaveAppletAction(String title) {
      super(title);
    }

    SaveAppletAction() {
      super(I18N.guiJ().get("menu.file.saveAsApplet"));
      // disabled for 3-D since it doesn't work - ST 2/25/05
      setEnabled(!org.nlogo.api.Version.is3D());
    }

    @Override
    void action()
        throws UserCancelException {
      // first, force the user to save.
      save();

      String exportPath = getExportPath("");

      app.resetZoom();

      // Use workspace.modelNameForDisplay() and
      // workspace.getModelFileName() to guarantee consistency. this should
      // be fine since we forced a save.
      appletSaver.save
          (org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
              app.tabs().interfaceTab().getInterfacePanel(),
              app.workspace().modelNameForDisplay(),
              exportPath,
              app.workspace().getModelFileName(),
              app.tabs().infoTab().info(),
              app.tabs().proceduresTab().getText(),
              app.workspace().getExtensionManager().getJarPaths(),
              app.workspace().getExtensionManager().getExtensionNames());
    }

    String getExportPath(String suffix)
        throws UserCancelException {
      // we use workspace.getModelFileName() here, because it really should
      // never any longer be null, now that we've forced the user to save.
      // it's important that it not be, in fact, since the applet relies
      // on the model having been saved to some file.
      String suggestedFileName = app.workspace().getModelFileName();

      // try to guess a decent file name to export to...
      int suffixIndex = suggestedFileName.lastIndexOf("." + modelSuffix());
      if (suffixIndex > 0
          && suffixIndex == suggestedFileName.length() - (modelSuffix().length() + 1)) {
        suggestedFileName = suggestedFileName.substring(0,
            suggestedFileName.length() - (modelSuffix().length() + 1));
      }
      suggestedFileName = suggestedFileName + suffix + ".html";

      // make the user choose the actual destination...
      return org.nlogo.swing.FileDialog.show
          (FileMenu.this, "Saving as Applet", java.awt.FileDialog.SAVE,
              suggestedFileName);
    }
  }

  public javax.swing.AbstractAction saveClientAppletAction() {
    return new SaveClientAppletAction();
  }

  private class SaveClientAppletAction extends SaveAppletAction {
    SaveClientAppletAction() {
      super(I18N.guiJ().get("menu.file.saveClientAsApplet")); // TODO i18n
    }

    @Override
    void action()
        throws UserCancelException {
      String exportPath = getExportPath("-client");

      app.resetZoom();

      // Use workspace.modelNameForDisplay() and
      // workspace.getModelFileName() to guarantee consistency. this should
      // be fine since we forced a save.
      appletSaver.saveClient
          (org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
              app.workspace().getHubNetManager().getInterfaceWidth(),
              app.workspace().getHubNetManager().getInterfaceHeight(),
              app.workspace().modelNameForDisplay(),
              exportPath);
    }
  }

  private class ExportWorldAction extends FileMenuAction {
    ExportWorldAction() {
      super(I18N.guiJ().get("menu.file.export.world"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String exportPath = org.nlogo.swing.FileDialog.show
          (FileMenu.this, "Export World", java.awt.FileDialog.SAVE,
              app.workspace().guessExportName("world.csv"));
      TaskWarning.maybeWarn(org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
                            app.workspace().world());
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply
          (org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
           "Exporting...",
           new Runnable() {
             public void run() {
               try {
                 app.workspace().exportWorld(exportPath);
               } catch (java.io.IOException ex) {
                 exception[0] = ex;
               }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ExportGraphicsAction extends FileMenuAction {
    ExportGraphicsAction() {
      super(I18N.guiJ().get("menu.file.export.view"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String exportPath =
          org.nlogo.swing.FileDialog.show
              (FileMenu.this, "Export View",
                  java.awt.FileDialog.SAVE,
                  app.workspace().guessExportName("view.png"));
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable() {
          public void run() {
            try {
              app.workspace().exportView(exportPath, "png");
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ExportInterfaceAction extends FileMenuAction {
    ExportInterfaceAction() {
      super(I18N.guiJ().get("menu.file.export.interface"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String exportPath =
          org.nlogo.swing.FileDialog.show
              (FileMenu.this, "Export Interface",
                  java.awt.FileDialog.SAVE,
                  app.workspace().guessExportName("interface.png"));
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable() {
          public void run() {
            try {
              app.workspace().exportInterface(exportPath);
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ExportOutputAction extends FileMenuAction {
    ExportOutputAction() {
      super(I18N.guiJ().get("menu.file.export.output"));
    }

    @Override
    void action()
        throws UserCancelException {
      final String exportPath = org.nlogo.swing.FileDialog.show
          (FileMenu.this, "Export Output", java.awt.FileDialog.SAVE,
              app.workspace().guessExportName("output.txt"));
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable() {
          public void run() {
            new org.nlogo.window.Events.ExportOutputEvent(exportPath)
              .raise(FileMenu.this);
          }});
    }
  }

  private class ExportPlotAction extends FileMenuAction {
    ExportPlotAction() {
      super(I18N.guiJ().get("menu.file.export.plot"));
    }

    @Override
    void action()
        throws UserCancelException {
      final String exportPath = org.nlogo.swing.FileDialog.show
          (FileMenu.this, "Export Plot", java.awt.FileDialog.SAVE,
              app.workspace().guessExportName("plot.csv"));
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable() {
          public void run() {
            new org.nlogo.window.Events.ExportPlotEvent
              (org.nlogo.window.PlotWidgetExportType.PROMPT, null, exportPath)
              .raise(FileMenu.this);
          }});
    }
  }

  private class ExportAllPlotsAction extends FileMenuAction {
    ExportAllPlotsAction() {
      super(I18N.guiJ().get("menu.file.export.allPlots"));
    }

    @Override
    void action()
        throws UserCancelException {
      final String exportPath = org.nlogo.swing.FileDialog.show
          (FileMenu.this, "Export All Plots", java.awt.FileDialog.SAVE,
              app.workspace().guessExportName("plots.csv"));
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Exporting...",
        new Runnable() {
          public void run() {
            new org.nlogo.window.Events.ExportPlotEvent(
              org.nlogo.window.PlotWidgetExportType.ALL, null, exportPath)
              .raise(FileMenu.this);
          }});
    }
  }

  private class ImportWorldAction extends ImportMenuAction {
    ImportWorldAction() {
      super(I18N.guiJ().get("menu.file.import.world"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, "Import World", java.awt.FileDialog.LOAD, null);
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing...",
        new Runnable() {
          public void run() {
            try {
              app.workspace().importWorld(importPath);
              app.workspace().view.dirty();
              app.workspace().view.repaint();
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ImportPatchColorsAction extends ImportMenuAction {
    ImportPatchColorsAction() {
      super(I18N.guiJ().get("menu.file.import.patchColors"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, "Import Patch Colors", java.awt.FileDialog.LOAD, null);
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Patch Colors...",
        new Runnable() {
          public void run() {
            try {
              // We can't wait for the thread to complete, or we end
              // up locking up the app since the Model Dialog and the
              // job wedge against one another. -- CLB 07/19/05
              org.nlogo.agent.ImportPatchColors.importPatchColors
                (new org.nlogo.api.LocalFile(importPath),
                 app.workspace().world(), true);
              app.workspace().view.dirty();
              app.workspace().view.repaint();
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ImportPatchColorsRGBAction extends ImportMenuAction {
    ImportPatchColorsRGBAction() {
      super(I18N.guiJ().get("menu.file.import.patchColorsRGB"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, "Import Patch Colors RGB", java.awt.FileDialog.LOAD, null);
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Patch Colors...",
        new Runnable() {
          public void run() {
            try {
              // We can't wait for the thread to complete, or we end
              // up locking up the app since the Model Dialog and the
              // job wedge against one another. -- CLB 07/19/05
              org.nlogo.agent.ImportPatchColors.importPatchColors
                (new org.nlogo.api.LocalFile(importPath),
                 app.workspace().world(), false);
              app.workspace().view.dirty();
              app.workspace().view.repaint();
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ImportDrawingAction extends ImportMenuAction {
    ImportDrawingAction() {
      super(I18N.guiJ().get("menu.file.import.drawing"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, "Import Drawing", java.awt.FileDialog.LOAD, null);
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
        "Importing Drawing...",
        new Runnable() {
          public void run() {
            try {
              app.workspace().importDrawing(importPath);
              app.workspace().view.dirty();
              app.workspace().view.repaint();
            }
            catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    }
  }

  private class ImportClientAction extends ImportMenuAction {
    ImportClientAction() {
      super(I18N.guiJ().get("menu.file.import.hubNetClientInterface"));
    }

    @Override
    void action()
        throws UserCancelException, java.io.IOException {
      final String importPath = org.nlogo.swing.FileDialog.show(
          FileMenu.this, "Import HubNet Client Interface", java.awt.FileDialog.LOAD, null);
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      final int choice =
          org.nlogo.swing.OptionDialog.show
              (app.workspace().getFrame(),
                  "Import HubNet Client",
                  "Which section would you like to import from?",
                  new String[]{"Interface Tab", "HubNet client", I18N.guiJ().get("common.buttons.cancel")});

      if (choice != 2) {
        org.nlogo.swing.ModalProgressTask.apply(
          org.nlogo.awt.Hierarchy.getFrame(FileMenu.this),
          "Importing Drawing...",
          new Runnable() {
            public void run() {
              try {
                app.workspace().getHubNetManager().importClientInterface
                  (importPath, choice == 1);
              } catch (java.io.IOException ex) {
                exception[0] = ex;
              }}});
        if (exception[0] != null) {
          throw exception[0];
        }
      }
    }
  }

  private class QuitAction extends FileMenuAction {
    QuitAction() {
      super(I18N.guiJ().get("menu.file.quit"));
    }

    @Override
    void action() {
      try {
        quit();
      } catch (UserCancelException ex) {
        org.nlogo.util.Exceptions.ignore(ex);
      }
    }
  }

  ///

  void quit()
      throws UserCancelException {
    offerSave();
    new org.nlogo.window.Events.AboutToQuitEvent().raise(this);
    app.workspace().getExtensionManager().reset();
    System.exit(0);
  }

  ///

  /**
   * makes a guess as to what the user would like to save this model as.
   * This is the model name if there is one, "Untitled.nlogo" otherwise.
   */
  private String guessFileName() {
    String fileName = app.workspace().getModelFileName();
    if (fileName == null) {
      return "Untitled." + modelSuffix();
    }
    return fileName;
  }

  /// model, how shall I load thee?  let me count the ways

  void newModel()
      throws UserCancelException, java.io.IOException {
    openFromURL(emptyModelPath());
  }

  /**
   * opens a model from a URL. Currently, this is only used to create a new
   * model (load the new model template).
   */
  private void openFromURL(String model)
      throws UserCancelException, java.io.IOException {
    String source = org.nlogo.util.Utils.url2String(model);
    if (model.equals(emptyModelPath())) {
      openFromSource(source, null, "Clearing...",
                     ModelTypeJ.NEW());
    } else {
      // models loaded from URLs are treated as library models, since
      // they are read-only. This is currently never used, so I'm
      // not even sure it's what we would really want...
      openFromSource(source, null, "Loading...",
                     ModelTypeJ.LIBRARY());
    }
  }

  public void handle(org.nlogo.window.Events.OpenModelEvent e) {
    try {
      openFromPath(e.path, ModelTypeJ.LIBRARY());
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /**
   * opens a model from a file path.
   */
  public void openFromPath(String path, ModelType modelType)
      throws java.io.IOException {
    try {
      String source = org.nlogo.api.FileIO.file2String(path);
      if (source == null) {
        throw new IllegalStateException
            ("couldn't open: '" + path + "'");
      }
      openFromSource(source, path, "Loading...", modelType);
    } catch (UserCancelException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }
  }

  private String savedVersion;

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
  void openFromSource(String source, String path,
                      String message, ModelType modelType)
      throws UserCancelException {
    // map elements are { source, info, resources, version }
    Map<ModelSection, String[]> map =
        ModelReader.parseModel(source);
    if (map == null || map.get(ModelSectionJ.VERSION()).length == 0) {
      notifyUserNotValidFile();
    }
    String version = org.nlogo.api.ModelReader.parseVersion(map);
    if (version == null || !version.startsWith("NetLogo")) {
      notifyUserNotValidFile();
    }
    if (org.nlogo.api.Version.is3D() &&
        !org.nlogo.api.Version.is3D(version)) {
      checkWithUserBeforeOpening2DModelin3D();
    }
    if (!org.nlogo.api.Version.is3D() &&
        org.nlogo.api.Version.is3D(version)) {
      checkWithUserBeforeOpening3DModelin2D(version);
    } else {
      if (!org.nlogo.api.Version.knownVersion(version)) {
        checkWithUserBeforeOpeningModelFromFutureVersion(version);
      }
    }
    openFromMap(map, path, message, modelType);
    savedVersion = version;
  }

  private boolean firstLoad = true;

  private void openFromMap(final Map<ModelSection, String[]> map,
                           final String path, String message,
                           final ModelType modelType) {
    try {
      if (firstLoad) {
        firstLoad = false;
        // app frame isn't even showing yet, so no need for ModalProgressTask
        org.nlogo.window.ModelLoader.load(this, path, modelType, map);
      } else {
        Runnable loader = new Runnable() {
          public void run() {
            try {
              org.nlogo.window.ModelLoader.load
                  (FileMenu.this, path, modelType, map);
            } catch (org.nlogo.window.InvalidVersionException e) {
              // we've already checked the version
              // so I don't expect this ever to happen
              // but in case it does...
              throw new IllegalStateException(e);
            }
          }
        };
        org.nlogo.swing.ModalProgressTask.apply(
          org.nlogo.awt.Hierarchy.getFrame(this), message, loader);
        app.tabs().requestFocus();
      }
    } catch (org.nlogo.window.InvalidVersionException e) {
      // we've already checked the version
      throw new IllegalStateException(e);
    }
  }

  /// saving

  private void save()
      throws UserCancelException {
    if (app.workspace().forceSaveAs()) {
      saveAs();
    } else {
      doSave(app.workspace().getModelPath());
    }
  }

  private void saveAs()
      throws UserCancelException {
    doSave(userChooseSavePath());
  }

  private class Saver implements Runnable {

    private boolean result = true;

    boolean getResult() {
      return result;
    }

    private java.io.IOException exception;

    java.io.IOException getException() {
      return exception;
    }

    private final String path;

    Saver(String path) {
      this.path = path;
    }

    public void run() {
      try {
        org.nlogo.api.FileIO.writeFile(path, modelSaver.save());
        new org.nlogo.window.Events.ModelSavedEvent(path)
            .raise(FileMenu.this);
      } catch (java.io.IOException ex) {
        result = false;
        exception = ex;
        // we don't want to call JOptionPane.showMessageDialog() here
        // because Java on Macs tends to barf when multiple modal dialogs
        // appear on top of each other, so we just hang onto the exception
        // until the modal progress task is done... - ST 11/3/04
      }
    }
  }

  private void doSave(final String path)
      throws UserCancelException {
    checkWithUserBeforeSavingModelFromOldVersion();
    Saver saver = new Saver(path);
    org.nlogo.swing.ModalProgressTask.apply(
      org.nlogo.awt.Hierarchy.getFrame(this), "Saving...", saver);
    if (saver.getException() != null) {
      javax.swing.JOptionPane.showMessageDialog
          (this, "Save failed.  Error: " + saver.getException().getMessage(),
              "NetLogo", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    if (!saver.getResult()) {
      throw new UserCancelException();
    }
    app.tabs().saveExternalFiles();
  }

  /// and now, a whole bunch of dialog boxes

  // this is called whenever a workspace is about to be destroyed
  public void offerSave()
      throws UserCancelException {

    // check if we have an open movie
    if (app.workspace().movieEncoder != null) {
      String[] options = {I18N.guiJ().get("common.buttons.ok"), I18N.guiJ().get("common.buttons.cancel")};
      String message = "There is a movie in progress. " +
          "Are you sure you want to exit this model? " +
          "You will lose the contents of your movie.";
      if (org.nlogo.swing.OptionDialog.show
          (this, "NetLogo", message, options) == 1) {
        throw new UserCancelException();
      }
      app.workspace().movieEncoder.cancel();
      app.workspace().movieEncoder = null;
    }

    if (app.dirtyMonitor().dirty() && userWantsToSaveFirst()) {
      save();
    }
  }

  private String userChooseLoadPath()
      throws UserCancelException {
    return org.nlogo.swing.FileDialog.show(this, "Open",
        java.awt.FileDialog.LOAD, null);
  }

  private String userChooseSavePath()
      throws UserCancelException {
    String newFileName = guessFileName();
    String newDirectoryName = null;
    if (app.workspace().getModelType() == ModelTypeJ.NORMAL()) {
      // we only default to saving in the model dir for normal and
      // models. for library and new models, we use the current
      // FileDialog dir.
      newDirectoryName = app.workspace().getModelDir();
    }
    org.nlogo.swing.FileDialog.setDirectory(newDirectoryName);
    String path = org.nlogo.swing.FileDialog.show(
      this, "Save As", java.awt.FileDialog.SAVE,
      newFileName);
    if(!path.endsWith("." + modelSuffix())) {
      path += "." + modelSuffix();
    }
    return path;
  }

  private boolean userWantsToSaveFirst()
      throws UserCancelException {
    String[] options = {I18N.guiJ().get("common.buttons.save"), "Discard", I18N.guiJ().get("common.buttons.cancel")};
    String message = "Do you want to save the changes you made to this model?";
    switch (org.nlogo.swing.OptionDialog.show
        (this, "NetLogo", message, options)) {
      case 0:
        return true;
      case 1:
        return false;
      default:
        throw new UserCancelException();
    }
  }

  private void checkWithUserBeforeSavingModelFromOldVersion()
      throws UserCancelException {
    if (!org.nlogo.api.Version.compatibleVersion(savedVersion)) {
      String[] options = {I18N.guiJ().get("common.buttons.save"), I18N.guiJ().get("common.buttons.cancel")};
      String message = "This model was made with " + savedVersion + ". "
          + "If you save it in " + org.nlogo.api.Version.version()
          + " it may not work in the old version anymore.";
      if (org.nlogo.swing.OptionDialog.show
          (this, "NetLogo", message, options) != 0) {
        throw new UserCancelException();
      }
      savedVersion = org.nlogo.api.Version.version();
    }
  }

  private void checkWithUserBeforeOpeningModelFromFutureVersion(String version)
      throws UserCancelException {
    String[] options = {I18N.guiJ().get("common.buttons.continue"), I18N.guiJ().get("common.buttons.cancel")};
    String message = "You are attempting to open a model that was created" +
        " in a newer version of NetLogo.  (This is " +
        org.nlogo.api.Version.version() + "; " +
        "the model was created in " + version + ".) " +
        "NetLogo can try to open the model, but it may " +
        "or may not work.";
    if (org.nlogo.swing.OptionDialog.show
        (this, "NetLogo", message, options) != 0) {
      throw new UserCancelException();
    }
  }

  private void checkWithUserBeforeOpening3DModelin2D(String version)
      throws UserCancelException {
    String[] options = {I18N.guiJ().get("common.buttons.continue"), I18N.guiJ().get("common.buttons.cancel")};
    String message = "You are attempting to open a model that was created" +
        " in a 3D version of NetLogo.  (This is " +
        org.nlogo.api.Version.version() + "; " +
        "the model was created in " + version + ".) " +
        "NetLogo can try to open the model, but it may " +
        "or may not work.";
    if (org.nlogo.swing.OptionDialog.show
        (this, "NetLogo", message, options) != 0) {
      throw new UserCancelException();
    }
  }

  private void checkWithUserBeforeOpening2DModelin3D()
      throws UserCancelException {
    String[] options = {I18N.guiJ().get("common.buttons.continue"), I18N.guiJ().get("common.buttons.cancel")};
    String message = "You are attempting to open a 2D model in " +
        org.nlogo.api.Version.version() + ". " +
        "You might need to make changes before it will work in 3D.";
    if (org.nlogo.swing.OptionDialog.show
        (this, "NetLogo", message, options) != 0) {
      throw new UserCancelException();
    }
  }

  private void notifyUserNotValidFile()
      throws UserCancelException {
    String[] options = {I18N.guiJ().get("common.buttons.ok")};
    org.nlogo.swing.OptionDialog.show
        (this, "NetLogo",
            "The file is not a valid NetLogo model file.",
            options);
    throw new UserCancelException();
  }

}
