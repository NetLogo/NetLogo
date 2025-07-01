// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.URI
import java.io.{ File, IOException }
import java.nio.file.{ Files, Paths }
import javax.swing.JFrame

import org.nlogo.api.{ AbstractModelLoader, Exceptions, ModelType, Version }
import org.nlogo.window.Events._
import org.nlogo.workspace.{ ModelTracker, SaveModel }

class DirtyMonitor(frame: JFrame, modelSaver: ModelSaver, modelLoader: AbstractModelLoader, modelTracker: ModelTracker,
                   title: Option[String] => String, codeWindow: JFrame)
extends BeforeLoadEvent.Handler
with AfterLoadEvent.Handler
with WidgetAddedEvent.Handler
with WidgetRemovedEvent.Handler
with DirtyEvent.Handler
with AboutToQuitEvent.Handler
with ModelSavedEvent.Handler
with ExternalFileSavedEvent.Handler
with SaveModel.Controller
{
  // we don't want auto save to kick in when a model isn't completely loaded yet - ST 8/6/09
  private var loading = true
  private var _modelDirty = false
  private var lastAutoSaveFile: Option[File] = None

  private def resetAutoSave(file: Option[File]): Unit = {
    try {
      lastAutoSaveFile.foreach(_.delete())
    } catch {
      case e: IOException =>
    }

    lastAutoSaveFile = file
  }

  def deleteLastAutoSave(): Unit = {
    resetAutoSave(None)
  }

  def modelDirty = _modelDirty && !loading
  private def setDirty(dirty: Boolean, path: Option[String] = None): Unit = {
    if (!path.isDefined && dirty != _modelDirty && !loading) {
      _modelDirty = dirty
      // on a Mac, this will make a gray dot appear in the red close button in the frame's title bar
      // to indicate that the document has unsaved changes, as documented at
      // developer.apple.com/qa/qa2001/qa1146.html - ST 7/30/04
      if (System.getProperty("os.name").startsWith("Mac"))
        frame.getRootPane.putClientProperty("Window.documentModified", dirty)
    }
    App.app.setWindowTitles()
  }

  def handle(e: AboutToQuitEvent): Unit = {
    Option(modelTracker.getModelPath).foreach(ModelConfig.pruneAutoSaves)
  }

  private var lastTimeAutoSaved = 0L
  private def doAutoSave(): Unit = {
    // autoSave when we get a dirty event but no more than once a minute I have no idea if this is a
    // good number or even the right ballpark.  feel free to change it. ev 8/22/06
    if (!modelDirty || (System.currentTimeMillis() - lastTimeAutoSaved) < 60000)
      return
    try {
      lastTimeAutoSaved = System.currentTimeMillis()
      SaveModel(modelSaver.currentModel, modelLoader, this, TempFileModelTracker, Version).foreach { f =>
        f().foreach { savedUri =>
          if (System.getProperty("os.name").startsWith("Windows"))
            Files.setAttribute(Paths.get(savedUri), "dos:hidden", true)

          resetAutoSave(Option(new File(savedUri.getPath)))
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
    resetAutoSave(None)
  }

  /// how we get dirty
  def handle(e: DirtyEvent): Unit = {
    setDirty(true, path = e.path)
    doAutoSave()
  }

  def handle(e: WidgetAddedEvent): Unit = {
    setDirty(true)
    doAutoSave()
  }

  def handle(e: WidgetRemovedEvent): Unit = {
    setDirty(true)
    doAutoSave()
  }

  object TempFileModelTracker extends ModelTracker {
    val delegate = modelTracker
    def compiler: org.nlogo.nvm.PresentationCompilerInterface = delegate.compiler
    def getExtensionManager(): org.nlogo.workspace.ExtensionManager = delegate.getExtensionManager()
    override def getModelType = delegate.getModelType
    override def getModelFileUri: Option[URI] =
      Option(ModelConfig.getAutoSavePath(Option(delegate.getModelPath)).toUri)
  }

  // SaveModel.Controller

  // chooseFilePath is used when the file doesn't yet have a path
  def chooseFilePath(modelType: ModelType): Option[URI] =
    TempFileModelTracker.getModelFileUri

  def shouldSaveModelOfDifferingVersion(version: String): Boolean = true
  def warnInvalidFileFormat(format: String): Unit = {}
}
