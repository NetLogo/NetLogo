// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.ActionEvent
import java.util.prefs.Preferences
import javax.swing.{AbstractAction, Action, JOptionPane}

import org.nlogo.api.ModelType
import org.nlogo.core.I18N
import org.nlogo.swing.UserAction, UserAction.{ Menu => ActionMenu, MenuAction }
import org.nlogo.window.Events._

case class ModelEntry(path: String, modelType: ModelType) {
  def this(line: String) {
    this(line.drop(1), (line(0) match {
                          case 'N' => ModelType.Normal
                          case 'L' => ModelType.Library
                          case _ => ModelType.Normal // This case is only for malformed prefs, which will bork later. FD 5/29/14
    }))
  }

  override def toString(): String =
    (modelType match {
      case ModelType.Normal => "N"
      case ModelType.Library => "L"
      case ModelType.New => throw new RuntimeException("Cannot add new file to menu!")
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

  val FilesGroup = "org.nlogo.app.OpenRecentFileAction.FilesGroup"
}

import OpenRecentFileAction._

class OpenRecentFileAction(modelEntry: ModelEntry, fileManager: FileManager, index: Int)
  extends AbstractAction(trimForDisplay(modelEntry.path))
  with MenuAction {

    category    = UserAction.FileCategory
    subcategory = UserAction.FileRecentSubcategory
    group       = FilesGroup
    rank        = index.toDouble

  override def actionPerformed(e: ActionEvent): Unit = {
    val sourceComponent = e.getSource match {
      case component: Component => component
      case _ => null
    }
    open(modelEntry, sourceComponent)
  }

  def open(modelEntry: ModelEntry, source: Component): Unit = {
    try {
      fileManager.aboutToCloseFiles()
      fileManager.openFromPath(modelEntry.path, modelEntry.modelType)
    } catch {
      case ex: org.nlogo.awt.UserCancelException =>
        org.nlogo.api.Exceptions.ignore(ex)
      case ex: java.io.IOException => {
        JOptionPane.showMessageDialog(
          source,
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

class RecentFilesMenu(fileManager: FileManager)
  extends ModelSavedEvent.Handler
  with BeforeLoadEvent.Handler {

  val recentFiles = new RecentFiles
  private var currentActions = Seq.empty[Action]
  private var menu = Option.empty[ActionMenu]

  def setMenu(newMenu: ActionMenu): Unit = {
    menu.foreach(oldMenu => currentActions.foreach(oldMenu.revokeAction))
    menu = Some(newMenu)
    refreshMenu()
  }

  // Add models to list when the current model is saved
  def handle(e: ModelSavedEvent) {
    for (p <- Option(e.modelPath)) {
      recentFiles.add(ModelEntry(p, ModelType.Normal))
      refreshMenu()
    }
  }

  // Add models to list when new models are opened
  def handle(e: BeforeLoadEvent) {
    if (e.modelType != ModelType.New) {
      for (p <- e.modelPath) {
        recentFiles.add(ModelEntry(p, e.modelType))
        refreshMenu()
      }
    }
  }

  def computeActions: Seq[Action] = {
    val fileActions =
    if (recentFiles.models.isEmpty) Seq(EmptyAction)
    else (for ((modelEntry, i) <- recentFiles.models.zipWithIndex)
      yield new OpenRecentFileAction(modelEntry, fileManager, i))
    fileActions :+ new ClearItems()
  }

  def refreshMenu() {
    val oldActions = currentActions
    currentActions = computeActions
    menu.foreach { m =>
      oldActions.foreach(m.revokeAction)
      currentActions.foreach(m.offerAction)
    }
  }

  object EmptyAction extends AbstractAction(I18N.gui.get("common.menus.empty"))
  with MenuAction {
    category    = UserAction.FileCategory
    subcategory = UserAction.FileRecentSubcategory
    group       = FilesGroup
    setEnabled(false)

    override def actionPerformed(e: ActionEvent): Unit = {}
  }

  class ClearItems extends AbstractAction(I18N.gui.get("menu.file.recent.clear"))
  with MenuAction {
    category    = UserAction.FileCategory
    subcategory = UserAction.FileRecentSubcategory
    group       = "org.nlogo.app.RecentFilesMenu.ClearItems"

    def actionPerformed(e: ActionEvent): Unit = {
      recentFiles.clear()
      refreshMenu()
    }
  }
}
