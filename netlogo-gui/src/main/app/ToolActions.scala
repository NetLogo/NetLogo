// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Frame
import java.awt.event.ActionEvent
import java.io.IOException
import java.net.HttpURLConnection
import java.nio.file.{ Files, FileVisitResult, Path, Paths, SimpleFileVisitor, StandardCopyOption }
import java.nio.file.attribute.BasicFileAttributes
import javax.swing.{ AbstractAction, JDialog }

import net.lingala.zip4j.core.ZipFile

import org.nlogo.api.AggregateManagerInterface
import org.nlogo.app.common.TabsInterface
import org.nlogo.app.tools.{ LibrariesCategoryInstaller, LibrariesDialog, LibraryInfo, LibraryManager, Preferences, PreferencesDialog }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.workspace.{ AbstractWorkspaceScala, ExtensionManager }
import org.nlogo.window.{ ColorDialog, LinkRoot }
import org.nlogo.shape.ShapesManagerInterface
import org.nlogo.swing.UserAction._

abstract class ShowDialogAction(name: String) extends AbstractAction(name) {
  protected def createDialog(): JDialog

  lazy protected val createdDialog = createDialog

  override def actionPerformed(e: ActionEvent): Unit = {
    createdDialog.setVisible(true)
  }
}

class ShowPreferencesDialog(frame: Frame, tabs: TabsInterface)
extends ShowDialogAction(I18N.gui.get("menu.tools.preferences"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup

  override def createDialog = new PreferencesDialog(frame,
    Preferences.Language,
    new Preferences.LineNumbers(tabs),
    Preferences.IncludedFilesMenu)
}

class OpenLibrariesDialog(frame: Frame)
extends ShowDialogAction(I18N.gui.get("menu.tools.extensionsAndIncludeFiles"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup

  def createDialog() = {
    val categories = Map("extensions" -> ExtensionsInstaller)
    new LibrariesDialog(frame, categories)
  }

  private object ExtensionsInstaller extends LibrariesCategoryInstaller {
    def install(ext: LibraryInfo): Unit = {
      val conn = ext.downloadURL.openConnection.asInstanceOf[HttpURLConnection]
      if (conn.getResponseCode == 200) {
        val urlPath = ext.downloadURL.getPath.stripSuffix("/")
        val basename = urlPath.substring(urlPath.lastIndexOf('/') + 1).dropRight(4)
        val zipPath = Files.createTempFile(basename, ".zip")
        Files.copy(conn.getInputStream, zipPath, StandardCopyOption.REPLACE_EXISTING)
        val extDir = Paths.get(ExtensionManager.userExtensionsPath, ext.codeName)
        if (!Files.isDirectory(extDir))
          Files.createDirectory(extDir)
        new ZipFile(zipPath.toFile).extractAll(extDir.toString)
        Files.delete(zipPath)
        LibraryManager.updateInstalledVersion("extensions", ext)
      }
    }

    def uninstall(ext: LibraryInfo): Unit = {
      val extDir = Paths.get(ExtensionManager.userExtensionsPath, ext.codeName)
      if (Files.exists(extDir))
        Files.walkFileTree(extDir, new SimpleFileVisitor[Path] {
          override def visitFile(file: Path, attrs: BasicFileAttributes) = delete(file)
          override def postVisitDirectory(dir: Path, ex: IOException) = delete(dir)
          private def delete(path: Path) = {
            Files.delete(path)
            FileVisitResult.CONTINUE
          }
        })
      LibraryManager.updateInstalledVersion("extensions", ext, uninstall = true)
    }
  }
}

class OpenColorDialog(frame: Frame)
extends ShowDialogAction(I18N.gui.get("menu.tools.colorSwatches"))
with MenuAction {
  category = ToolsCategory
  group = ToolsDialogsGroup

  def createDialog() = new ColorDialog(frame, false)

  var hasShown = false

  override def actionPerformed(e: ActionEvent) {
    Positioning.center(createdDialog, frame)
    if (!hasShown) {
      createdDialog.asInstanceOf[ColorDialog].showDialog()
      hasShown = true
    } else
      super.actionPerformed(e)
  }
}

class ShowShapeManager(key: String, shapeManager: => ShapesManagerInterface)
extends AbstractAction(I18N.gui.get(s"menu.tools.$key"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsDialogsGroup

  override def actionPerformed(e: ActionEvent) {
    shapeManager.init(I18N.gui.get(s"menu.tools.$key"))
  }
}

class ShowSystemDynamicsModeler(aggregateManager: AggregateManagerInterface)
extends AbstractAction(I18N.gui.get("menu.tools.systemDynamicsModeler"))
with MenuAction {
  category    = ToolsCategory
  group       = ToolsDialogsGroup
  accelerator = KeyBindings.keystroke('D', withMenu = true, withShift = true)

  override def actionPerformed(e: ActionEvent) {
    aggregateManager.showEditor()
  }
}

class OpenHubNetClientEditor(workspace: AbstractWorkspaceScala, linkRoot: LinkRoot)
extends AbstractAction(I18N.gui.get("menu.tools.hubNetClientEditor"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsHubNetGroup

  override def actionPerformed(e: ActionEvent) {
    workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      linkRoot.addLinkComponent(mgr.clientEditor)
    }
  }
}
