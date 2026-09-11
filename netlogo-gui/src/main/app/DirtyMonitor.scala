// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.URI
import java.io.{ File, IOException }
import java.nio.file.{ Files, Paths, StandardOpenOption }
import java.util.{ Timer, TimerTask }
import javax.swing.JFrame

import org.nlogo.api.{ AbstractModelLoader, Exceptions, ModelSaver, ModelType, Version }
import org.nlogo.app.common.ModelConfig
import org.nlogo.window.Events._
import org.nlogo.workspace.{ ModelTracker, SaveModel }

class DirtyMonitor(frame: JFrame, modelSaver: ModelSaver, modelLoader: AbstractModelLoader, modelTracker: ModelTracker,
                   title: Option[String] => String, tabManager: TabManager)
extends BeforeLoadEvent.Handler
with AfterLoadEvent.Handler
with WidgetAddedEvent.Handler
with WidgetRemovedEvent.Handler
with DirtyEvent.Handler
with ModelSavedEvent.Handler
with ExternalFileSavedEvent.Handler
with UpdateModeChangedEvent.Handler
with SaveModel.Controller
{
  // we don't want auto save to kick in when a model isn't completely loaded yet - ST 8/6/09
  private var loading = true
  private var lastAutoSaveFile: Option[File] = None

  private var dirtyPaths = Set[Option[String]]()
  private var dirtyTimers = Map[Option[String], Timer]()

  def discardNewAutoSaves(): Unit = {
    Option(modelTracker.getModelPath).foreach(ModelConfig.discardNewModelAutoSaves)
  }

  def modelDirty: Boolean = dirtyPaths.contains(None) && !loading
  private def setDirty(dirty: Boolean, path: Option[String] = None): Unit = {
    if (dirty && !loading) {
      dirtyTimers.get(path).foreach { timer =>
        timer.cancel()
        timer.purge()
      }

      // auto save if no dirty event is received for a while (Isaac B 7/1/25)
      dirtyTimers += ((path, new Timer {
        schedule(new TimerTask {
          override def run(): Unit = {
            doAutoSave(path)
          }
        }, 5000)
      }))
    }

    if (!path.isDefined && dirty != dirtyPaths.contains(None) && !loading) {
      // on a Mac, this will make a gray dot appear in the red close button in the frame's title bar
      // to indicate that the document has unsaved changes, as documented at
      // developer.apple.com/qa/qa2001/qa1146.html - ST 7/30/04
      if (System.getProperty("os.name").startsWith("Mac"))
        frame.getRootPane.putClientProperty("Window.documentModified", dirty)
    }

    if (dirty) {
      dirtyPaths += path
    } else {
      dirtyPaths -= path
    }

    App.app.setWindowTitles()
  }

  private def doAutoSave(path: Option[String]): Unit = {
    if (dirtyPaths.contains(path)) {
      try {
        path match {
          case Some(includePath) =>
            tabManager.getTabWithFilename(Right(includePath)).foreach { tab =>
              Files.write(ModelConfig.getIncludeAutoSavePath(includePath), tab.getText.getBytes,
                          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            }

          case _ =>
            SaveModel(modelSaver.currentModel, modelLoader, this, TempFileModelTracker, Version).foreach { f =>
              f().foreach { savedUri =>
                if (System.getProperty("os.name").startsWith("Windows"))
                  Files.setAttribute(Paths.get(savedUri), "dos:hidden", true)

                // if the model is new, we can't keep track of old autosaves as easily,
                // so overwrite the previous autosave file instead of adding a new file (Isaac B 7/1/25)
                if (modelTracker.getModelType == ModelType.New) {
                  try {
                    lastAutoSaveFile.foreach(_.delete())
                  } catch {
                    case e: IOException =>
                  }
                }

                lastAutoSaveFile = Option(new File(savedUri.getPath))
              }
            }
        }
      } catch {
        case ex: java.io.IOException =>
          // not sure what the right thing to do here is we probably
          // don't want to be telling the user all the time that they
          // the auto save failed. ev 8/22/06
          Exceptions.ignore(ex)
      }
    }
  }

  /// how we get clean
  def handle(e: ModelSavedEvent) = { setDirty(false) }
  def handle(e: ExternalFileSavedEvent) = setDirty(false, path = Some(e.path))
  def handle(e: BeforeLoadEvent): Unit = {
    setDirty(false)
    loading = true
  }

  def handle(e: AfterLoadEvent): Unit = {
    setDirty(false)
    loading = false
  }

  /// how we get dirty
  def handle(e: DirtyEvent): Unit = {
    setDirty(true, path = e.path)
  }

  def handle(e: WidgetAddedEvent): Unit = {
    setDirty(true)
  }

  def handle(e: WidgetRemovedEvent): Unit = {
    setDirty(true)
  }

  override def handle(e: UpdateModeChangedEvent): Unit = {
    setDirty(true)
  }

  object TempFileModelTracker extends ModelTracker {
    val delegate = modelTracker
    def compiler: org.nlogo.nvm.PresentationCompilerInterface = delegate.compiler
    def getExtensionManager(): org.nlogo.workspace.ExtensionManager = delegate.getExtensionManager()
    override def getModelType = delegate.getModelType
    override def getModelFileUri: Option[URI] =
      Option(ModelConfig.getModelAutoSavePath(Option(delegate.getModelPath)).toUri)
  }

  // SaveModel.Controller

  // chooseFilePath is used when the file doesn't yet have a path
  def chooseFilePath(modelType: ModelType): Option[URI] =
    TempFileModelTracker.getModelFileUri

  def shouldSaveModelOfDifferingVersion(version: String): Boolean = true
  def warnInvalidFileFormat(format: String): Unit = {}
}
