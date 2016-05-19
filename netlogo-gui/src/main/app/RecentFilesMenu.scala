// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.util.prefs.Preferences
import org.nlogo.window.Events._

import org.nlogo.core.I18N
import org.nlogo.api
import org.nlogo.window

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JMenuItem
import javax.swing.JOptionPane

case class ModelEntry(path: String, modelType: api.ModelType) {
  def this(line: String) {
    this(line.drop(1), (line(0) match {
                          case 'N' => api.ModelType.Normal
                          case 'L' => api.ModelType.Library
                          case _ => api.ModelType.Normal // This case is only for malformed prefs, which will bork later. FD 5/29/14
    }))
  }

  override def toString(): String =
    (modelType match {
      case api.ModelType.Normal => "N"
      case api.ModelType.Library => "L"
      case api.ModelType.New => throw new RuntimeException("Cannot add new file to menu!")
     }) + path
}

object OpenRecentFileAction {
  def trimForDisplay(path: String): String = {
    val maxDisplayLength = 100
    val prefix = "..."
    if (path.length > maxDisplayLength)
      prefix + path.takeRight(maxDisplayLength - prefix.length)
    else
      path
  }
}

import OpenRecentFileAction.trimForDisplay

class OpenRecentFileAction(modelEntry: ModelEntry, fileMenu: FileMenu) extends AbstractAction(trimForDisplay(modelEntry.path)) {
  override def actionPerformed(e: ActionEvent): Unit = {
    open(modelEntry)
  }

  def open(modelEntry: ModelEntry): Unit = {
    try {
      fileMenu.offerSave()
      fileMenu.openFromPath(modelEntry.path, modelEntry.modelType)
    } catch {
      case ex: org.nlogo.awt.UserCancelException =>
        org.nlogo.api.Exceptions.ignore(ex)
      case ex: java.io.IOException => {
        JOptionPane.showMessageDialog(
          fileMenu,
          ex.getMessage,
          I18N.gui.get("common.messages.error"),
          JOptionPane.ERROR_MESSAGE)
      }
    }
  }
}

class RecentFiles {
  val prefs = Preferences.userNodeForPackage(getClass)
  val key = "recent_files"
  val maxEntries = 8

  loadFromPrefs()

  private var _models: List[ModelEntry] = _
  def models = _models
  def models_=(newModels: List[ModelEntry]) {
    _models = newModels
      .flatMap(ensureCanonicalPath)
      .distinct
      .take(maxEntries)
    prefs.put(key, _models.mkString("\n"))
  }

  /**
   * File.getCanonicalPath does some validation on the path
   * (without checking if the file actually exists) rejecting,
   * e.g., paths that are too long. NP 2014-01-29.
   */
  private def ensureCanonicalPath(modelEntry: ModelEntry): Option[ModelEntry] =
    try Some(ModelEntry(new java.io.File(modelEntry.path).getCanonicalPath(), modelEntry.modelType))
    catch { case _: java.io.IOException => None }

  def loadFromPrefs() {
    models = prefs.get(key, "").lines.toList.map(new ModelEntry(_))
  }

  def add(modelEntry: ModelEntry) {
    models = (modelEntry :: _models)
  }

  def clear() {
    models = Nil
  }
}

class RecentFilesMenu(frame: AppFrame, fileMenu: FileMenu)
  extends org.nlogo.swing.Menu("Recent Files")
  with ModelSavedEvent.Handler
  with BeforeLoadEvent.Handler {

  val recentFiles = new RecentFiles
  refreshMenu()
  frame.addLinkComponent(this)

  // Add models to list when the current model is saved
  def handle(e: ModelSavedEvent) {
    for (p <- Option(e.modelPath)) {
      recentFiles.add(ModelEntry(p, api.ModelType.Normal))
      refreshMenu()
    }
  }

  // Add models to list when new models are opened
  def handle(e: BeforeLoadEvent) {
    if (e.modelType != api.ModelType.New) {
      for (p <- Option(e.modelPath)) {
        recentFiles.add(ModelEntry(p, e.modelType))
        refreshMenu()
      }
    }
  }

  def refreshMenu() {
    getMenuComponents.foreach {
      case j: JMenuItem => j.getActionListeners.foreach {
        case o: OpenRecentFileAction => j.removeActionListener(o)
        case _ =>
      }
      case _ =>
    }
    removeAll()
    if (recentFiles.models.isEmpty)
      addMenuItem("<empty>").setEnabled(false)
    else {
      for (modelEntry <- recentFiles.models)
        addMenuItem(new OpenRecentFileAction(modelEntry, fileMenu))
      addSeparator()
      addMenuItem("Clear Items", () => {
        recentFiles.clear
        refreshMenu()
      })
    }
  }
}
