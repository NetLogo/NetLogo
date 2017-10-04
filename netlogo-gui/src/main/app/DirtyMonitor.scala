// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.net.URI
import java.io.IOException
import java.nio.file.{ Files, Path, Paths }
import javax.swing.JFrame

import org.nlogo.api.{ Exceptions, ModelLoader, ModelReader, ModelType, Version }
import org.nlogo.window.Events._
import org.nlogo.workspace.{ ModelTracker, SaveModel }

import scala.util.Try

object DirtyMonitor {
  val autoSaveFileName = {
    val df = new java.text.SimpleDateFormat("yyyy-MM-dd.HH_mm_ss",
                                            java.util.Locale.US)
    System.getProperty("java.io.tmpdir") +
      System.getProperty("file.separator") + "autosave_" +
      df.format(new java.util.Date()) + "." + ModelReader.modelSuffix
  }
}

class DirtyMonitor(frame: JFrame, modelSaver: ModelSaver, modelLoader: ModelLoader, modelTracker: ModelTracker, title: Option[String] => String)
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
  private var priorTempFile = Option.empty[Path]

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
    frame.setTitle(title(path))
  }

  def handle(e: AboutToQuitEvent) {
    new java.io.File(DirtyMonitor.autoSaveFileName).delete()
    Exceptions.ignoring(classOf[IOException]) {
      priorTempFile.foreach(Files.deleteIfExists)
    }
  }

  private var lastTimeAutoSaved = 0L
  private def doAutoSave() {
    // autoSave when we get a dirty event but no more than once a minute I have no idea if this is a
    // good number or even the right ballpark.  feel free to change it. ev 8/22/06
    if (!modelDirty || (System.currentTimeMillis() - lastTimeAutoSaved) < 60000)
      return
    try {
      lastTimeAutoSaved = System.currentTimeMillis()
      SaveModel(modelSaver.currentModel, modelLoader, this, TempFileModelTracker, Version).foreach { f =>
        f().foreach { savedUri =>
          if (System.getProperty("os.name").startsWith("Windows")) {
            Files.setAttribute(Paths.get(savedUri), "dos:hidden", true)
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

  /// how we get clean
  def handle(e: ModelSavedEvent) = setDirty(false)
  def handle(e: ExternalFileSavedEvent) = setDirty(false, path = Some(e.path))
  def handle(e: BeforeLoadEvent): Unit = {
    setDirty(false)
    loading = true
  }

  def handle(e: AfterLoadEvent): Unit = {
    setDirty(false)
    loading = false
    Exceptions.ignoring(classOf[IOException]) {
      priorTempFile.foreach(Files.deleteIfExists)
    }
    priorTempFile = TempFileModelTracker.getModelFileUri.flatMap(u => Try(Paths.get(u)).toOption)
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

    override def getModelType = delegate.getModelType
    override def getModelFileUri: Option[URI] = {
      delegate.getModelFileUri.map { u =>
        val p = Paths.get(u)
        val name = p.getName(p.getNameCount - 1).toString
        val extension = name.split("\\.").last
        val nameContent = name.split("\\.").init.mkString(".")
        p.getParent.resolve(s".$nameContent.tmp.$extension").toUri
      }
    }
  }

  // SaveModel.Controller

  // chooseFilePath is used when the file doesn't yet have a path
  def chooseFilePath(modelType: ModelType): Option[URI] =
    Some(Paths.get(DirtyMonitor.autoSaveFileName).toUri)

  def shouldSaveModelOfDifferingVersion(version: String): Boolean = true
  def warnInvalidFileFormat(format: String): Unit = {}
}
