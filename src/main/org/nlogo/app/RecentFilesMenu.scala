// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.util.prefs.Preferences

import org.nlogo.api
import org.nlogo.window

import javax.swing.JOptionPane

class RecentFiles {
  val prefs = Preferences.userNodeForPackage(getClass)
  val key = "recent_files"
  val maxEntries = 8
  val maxEntryLength = 4096

  loadFromPrefs()

  private var _paths: List[String] = _
  def paths = _paths
  def paths_=(newPaths: List[String]) {
    _paths = newPaths
      .filter(_.length <= maxEntryLength)
      .distinct
      .take(maxEntries)
    prefs.put(key, _paths.mkString("\n"))
  }

  def loadFromPrefs() {
    paths = prefs.get(key, "").lines.toList
  }

  def add(path: String) {
    paths = (path :: _paths)
  }

  def clear() {
    paths = Nil
  }
}

class RecentFilesMenu(workspace: window.GUIWorkspace, fileMenu: FileMenu)
  extends org.nlogo.swing.Menu("Recent Files") {

  val recentFiles = new RecentFiles
  refreshMenu()

  // Add paths to list when new models are opened
  workspace.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def modelOpened(path: String) {
        if (workspace.getModelType == api.ModelType.Normal)
          for (p <- Option(path)) {
            recentFiles.add(p)
            refreshMenu()
          }
      }
    })

  def refreshMenu() {
    removeAll()
    if (recentFiles.paths.isEmpty)
      addMenuItem("<empty>").setEnabled(false)
    else {
      for (path <- recentFiles.paths)
        addMenuItem(path, () => open(path))
      addSeparator()
      addMenuItem("Clear Items", () => {
        recentFiles.clear
        refreshMenu()
      })
    }
  }

  def open(path: String) {
    try {
      fileMenu.offerSave()
      fileMenu.openFromPath(path, api.ModelType.Normal)
    } catch {
      case ex: org.nlogo.awt.UserCancelException =>
        org.nlogo.util.Exceptions.ignore(ex)
      case ex: java.io.IOException => {
        JOptionPane.showMessageDialog(
          RecentFilesMenu.this,
          ex.getMessage,
          api.I18N.gui.get("common.messages.error"),
          JOptionPane.ERROR_MESSAGE)
      }
    }
  }
}
