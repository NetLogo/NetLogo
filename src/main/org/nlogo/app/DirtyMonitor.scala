// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.window.Events._
import org.nlogo.api

object DirtyMonitor {
  val autoSaveFileName = {
    val df = new java.text.SimpleDateFormat("yyyy-MM-dd.HH_mm_ss",
                                            java.util.Locale.US)
    System.getProperty("java.io.tmpdir") +
      System.getProperty("file.separator") + "autosave_" +
      df.format(new java.util.Date()) + "." + api.ModelReader.modelSuffix
  }
}

class DirtyMonitor(frame: javax.swing.JFrame)
extends BeforeLoadEvent.Handler
with AfterLoadEvent.Handler
with WidgetAddedEvent.Handler
with WidgetRemovedEvent.Handler
with DirtyEvent.Handler
with AboutToQuitEvent.Handler
with ModelSavedEvent.Handler
{
  // we don't want auto save to kick in when a model isn't completely loaded yet - ST 8/6/09
  private var loading = true
  private var _dirty = false
  def dirty = _dirty && !loading
  private def dirty(dirty: Boolean, lockTitleBar: Boolean = false) {
    if (dirty != _dirty && !loading) {
      _dirty = dirty
      // on a Mac, this will make a gray dot appear in the red close button in the frame's title bar
      // to indicate that the document has unsaved changes, as documented at
      // developer.apple.com/qa/qa2001/qa1146.html - ST 7/30/04
      if (System.getProperty("os.name").startsWith("Mac"))
        frame.getRootPane.putClientProperty("Window.documentModified", dirty)
      else if (!lockTitleBar)
        if (dirty) frame.setTitle("* " + frame.getTitle) else frame.setTitle(frame.getTitle.substring(2))
    }
  }
  def handle(e: AboutToQuitEvent) {
    new java.io.File(DirtyMonitor.autoSaveFileName).delete()
  }
  private var lastTimeAutoSaved = 0L
  private def doAutoSave() {
    // autoSave when we get a dirty event but no more than once a minute I have no idea if this is a
    // good number or even the right ballpark.  feel free to change it. ev 8/22/06
    if (!dirty || (System.currentTimeMillis() - lastTimeAutoSaved) < 60000)
      return
    try {
      lastTimeAutoSaved = System.currentTimeMillis()
      api.FileIO.writeFile(DirtyMonitor.autoSaveFileName,
                           new ModelSaver(App.app).save)
    }
    catch {
      case ex: java.io.IOException =>
        // not sure what the right thing to do here is we probably
        // don't want to be telling the user all the time that they
        // the auto save failed. ev 8/22/06
        org.nlogo.util.Exceptions.ignore(ex)
    }
  }

  /// how we get clean
  def handle(e: ModelSavedEvent) {
    dirty(false, true)
  }
  def handle(e: BeforeLoadEvent) {
    dirty(false)
    loading = true
  }
  def handle(e: AfterLoadEvent) {
    dirty(false)
    loading = false
  }

  /// how we get dirty
  def handle(e: DirtyEvent) {
    dirty(true)
    doAutoSave()
  }
  def handle(e: WidgetAddedEvent) {
    dirty(true)
    doAutoSave()
  }
  def handle(e: WidgetRemovedEvent) {
    dirty(true)
    doAutoSave()
  }
}
