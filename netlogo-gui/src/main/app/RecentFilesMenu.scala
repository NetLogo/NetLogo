// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Frame
import java.awt.event.ActionEvent
import java.io.{ File, FileNotFoundException, IOException }
import javax.swing.AbstractAction

import org.nlogo.api.{ Exceptions, ModelType, Version }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ OptionPane, UserAction }, UserAction.{ Menu => ActionMenu, MenuAction }
import org.nlogo.window.Events._

case class ModelEntry(path: String, modelType: ModelType) {
  def this(line: String) = {
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

class OpenRecentFileAction(parent: Frame, modelEntry: ModelEntry, fileManager: FileManager, index: Int)
  extends AbstractAction(trimForDisplay(modelEntry.path))
  with MenuAction {

  category    = UserAction.FileCategory
  subcategory = UserAction.FileRecentSubcategory
  group       = FilesGroup
  rank        = index.toDouble

  override def actionPerformed(e: ActionEvent): Unit = {
    open(modelEntry)
  }

  def open(modelEntry: ModelEntry): Unit = {
    try {
      fileManager.aboutToCloseFiles()
      fileManager.openFromPath(modelEntry.path, modelEntry.modelType)
    } catch {
      case ex: UserCancelException =>
        Exceptions.ignore(ex)
      case ex: FileNotFoundException =>
        new OptionPane(parent, I18N.gui.get("common.messages.error"),
                       I18N.gui.getN("file.open.error.doesNotExist", modelEntry.path), OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
      case ex: IOException => {
        new OptionPane(parent, I18N.gui.get("common.messages.error"), ex.getMessage, OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
      }
    }
  }
}

class RecentFiles {
  val key = if (Version.is3D) "recent_files_3d" else "recent_files"
  val maxEntries = 8

  private var _models = List[ModelEntry]()

  loadFromPrefs(true)

  def models = _models
  def models_=(newModels: List[ModelEntry]): Unit = {
    _models = newModels
      .flatMap(ensureCanonicalPath)
      .distinctBy(_.path)
      .filter(x => Version.is3D == (x.path.endsWith(".nlogo3d") || x.path.endsWith(".nlogox3d")))
      .take(maxEntries)
    NetLogoPreferences.put(key, _models.mkString("\n"))
  }

  /**
   * File.getCanonicalPath does some validation on the path
   * (without checking if the file actually exists) rejecting,
   * e.g., paths that are too long. NP 2014-01-29.
   */
  private def ensureCanonicalPath(modelEntry: ModelEntry): Option[ModelEntry] =
    try Some(ModelEntry(new File(modelEntry.path).getCanonicalPath(), modelEntry.modelType))
    catch { case _: java.io.IOException => None }

  def loadFromPrefs(filter: Boolean = false): Unit = {
    models = NetLogoPreferences.get(key, "").linesIterator.toList.map(new ModelEntry(_))
                                                    .filter(entry => !filter || new File(entry.path).exists)
  }

  def add(modelEntry: ModelEntry): Unit = {
    if (_models.exists(entry => entry.path == modelEntry.path && entry.modelType == ModelType.Library)) {
      models = ModelEntry(modelEntry.path, ModelType.Library) +: _models
    } else {
      models = modelEntry +: _models
    }
  }

  def clear(): Unit = {
    models = Nil
  }
}

class RecentFilesMenu(frame: AppFrame, fileManager: FileManager)
  extends ModelSavedEvent.Handler
  with BeforeLoadEvent.Handler {

  val recentFiles = new RecentFiles
  private var currentActions = Seq[MenuAction]()
  private var menu: Option[ActionMenu] = None

  def setMenu(newMenu: ActionMenu): Unit = {
    menu.foreach(oldMenu => currentActions.foreach(oldMenu.revokeAction))
    menu = Some(newMenu)
    refreshMenu()
  }

  // Add models to list when the current model is saved
  def handle(e: ModelSavedEvent): Unit = {
    for (p <- Option(e.modelPath)) {
      recentFiles.add(ModelEntry(p, ModelType.Normal))
      refreshMenu()
    }
  }

  // Add models to list when new models are opened
  def handle(e: BeforeLoadEvent): Unit = {
    if (e.modelType != ModelType.New) {
      for (p <- e.modelPath) {
        recentFiles.add(ModelEntry(p, e.modelType))
        refreshMenu()
      }
    }
  }

  def computeActions: Seq[MenuAction] = {
    val fileActions =
    if (recentFiles.models.isEmpty) Seq(new EmptyAction)
    else (for ((modelEntry, i) <- recentFiles.models.zipWithIndex)
      yield new OpenRecentFileAction(frame, modelEntry, fileManager, i))
    fileActions :+ new ClearItems()
  }

  def refreshMenu(): Unit = {
    val oldActions = currentActions
    currentActions = computeActions
    menu.foreach { m =>
      oldActions.foreach(m.revokeAction)
      currentActions.foreach(m.offerAction)
    }
  }

  class EmptyAction extends AbstractAction(I18N.gui.get("common.menus.empty"))
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
