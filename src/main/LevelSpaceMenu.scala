package org.nlogo.ls.gui

import java.awt.FileDialog.{ LOAD => LOADFILE, SAVE => SAVEFILE }
import java.awt.event.ActionEvent
import java.nio.file.{ Paths, Files }
import java.io.IOException
import javax.swing._

import org.nlogo.api.{ Exceptions, ExtensionException, Version }
import org.nlogo.core.CompilerException
import org.nlogo.app.{ App, TabManager }
import org.nlogo.app.codetab.CodeTab
import org.nlogo.awt.UserCancelException
import org.nlogo.fileformat.FileFormat
import org.nlogo.swing.{ FileDialog, Menu, MenuItem }
import org.nlogo.workspace.{ AbstractWorkspace, ModelsLibrary, ModelTracker, SaveModel }

trait ModelManager {
  def removeTab(tab: ModelCodeTab): Unit
  def existingTab(filePath: String): Option[CodeTab]
  def registerTab(filePath: String)
                 (f: AbstractWorkspace => ModelCodeTab): Option[ModelCodeTab]
}

trait LevelSpaceMenu extends JMenu

class GUILevelSpaceMenu(tabManager: TabManager, val backingModelManager: ModelManager)
  extends Menu("LevelSpace") with LevelSpaceMenu {

  import LevelSpaceMenu._

  val selectModel = new MenuItem(new SelectModelAction("Open Model in Code Tab", backingModelManager))
  val openModels  = new Menu("Edit Open Models...")
  val newModel    = new MenuItem(new NewModelAction("Create new LevelSpace Model", backingModelManager))

  add(selectModel)
  add(openModels)
  add(newModel)

  def addMenuItemsForOpenModels(modelPaths: Seq[String]) = {
    openModels.removeAll()
    modelPaths.foreach(addModelAction(openModels, _))
    if (openModels.getMenuComponentCount == 0)
      openModels.setEnabled(false)
    else
      openModels.setEnabled(true)
  }

  def replaceTab(oldTab: ModelCodeTab): Unit =
    newHeadlessBackedTab(oldTab.filePath).foreach {
      newTab => replaceSwingTab(oldTab, newTab)
    }

  private def addModelAction(menu: JMenu, filePath: String): Unit = {
    menu.add(new MenuItem(new OpenModelAction(filePath, backingModelManager)))
  }

  private def newHeadlessBackedTab(filePath: String): Option[ModelCodeTab] =
    backingModelManager.registerTab(filePath) { workspace =>
      new ModelCodeTab(workspace, tabManager, backingModelManager)
    }

  private def replaceSwingTab(oldTab: ModelCodeTab, newTab: ModelCodeTab): Unit = {
    tabManager.replaceTab(oldTab, newTab)
  }
}

object LevelSpaceMenu {
  abstract class NewTabAction(name: String, modelManager: ModelManager) extends AbstractAction(name) {
    val tabManager = App.app.tabManager

    def filePath: Option[String]

    def actingTab: Option[CodeTab] =
      filePath.flatMap(path => locateExistingTab(path) orElse createNewTab(path))

    private def locateExistingTab(path: String): Option[CodeTab] =
      modelManager.existingTab(path)

    private def createNewTab(path: String): Option[CodeTab] = {
      modelManager.registerTab(path) { workspace =>
        val tab = new ModelCodeTab(workspace, tabManager, modelManager)
        tabManager.addTab(tab, tab.tabName)
        tab
      }
    }

    override def actionPerformed(actionEvent: ActionEvent): Unit =
      actingTab.foreach(tabManager.setSelectedTab)
  }

  class OpenModelAction(fileName: String, modelManager: ModelManager) extends NewTabAction(fileName, modelManager) {
    override def filePath: Option[String] = Some(fileName)
  }

  class SelectModelAction(name: String, modelManager: ModelManager) extends NewTabAction(name, modelManager) {
    override def filePath: Option[String] = selectFile

    override def actingTab: Option[CodeTab] =
      try {
        super.actingTab
      } catch {
        case e: CompilerException =>
          // we shouldn't have to raise an exception here, we should just be able to open it, but
          // in order to do that, we'll need to change child models not to compile in their constructors
          throw new ExtensionException(s"$filePath did not compile properly. There is probably something wrong " +
            s"with its code. Exception said ${e.getMessage}")
        case e: IOException =>
          throw new ExtensionException(s"There was no model file at the path: \"$filePath\"")
      }

    private def selectFile: Option[String] =
      showLoadSelection.flatMap(path =>
        if (ModelsLibrary.getModelPaths.contains(path)) {
          showLibraryModelErrorMessage()
          None
        } else
          Some(path))

    private def showLoadSelection: Option[String] =
      try {
        Some(FileDialog.showFiles(App.app.frame, "Load a LevelSpace Model...", LOADFILE))
      } catch {
        case e: UserCancelException =>
          Exceptions.ignore(e)
          None
      }

    private def showLibraryModelErrorMessage(): Unit =
      JOptionPane.showMessageDialog(
        App.app.frame,
        """|The model you selected is a library model, which cannot be opened in a LevelSpace code tab.
        |Please save the model elsewhere and try re-opening""".stripMargin)
  }

  class NewModelAction(name: String, modelManager: ModelManager) extends NewTabAction(name, modelManager) {
    override def filePath: Option[String] = {
      val ws = App.app.workspace
      val loader = FileFormat.standardAnyLoader(false, ws.compiler.utilities)
      val controller = new SaveModel.Controller {
        def chooseFilePath(modelType: org.nlogo.api.ModelType): Option[java.net.URI] = {
          try {
            val userEntry = FileDialog.showFiles(App.app.frame, "Select a path for new Model...", SAVEFILE)
            // we basically need to write an empty NetLogo model in before we read...
            val fileName =
              if (userEntry.endsWith(".nlogo") || userEntry.endsWith(".nlogox")) userEntry else userEntry + ".nlogox"
            val path = Paths.get(fileName)
            if (Files.exists(path)) {
              val fileAlreadyExists = s"The file $fileName already exists. Please choose a different name"
              throw new ExtensionException(fileAlreadyExists)
            }
            Some(path.toUri)
          } catch {
            case e: UserCancelException =>
              Exceptions.ignore(e)
              None
          }
        }
        def shouldSaveModelOfDifferingVersion(version: String): Boolean = true
        def warnInvalidFileFormat(format: String): Unit = {
          // we force users to save in NetLogo, so this doesn't happen
        }
      }
      val modelTracker = new ModelTracker {
        def compiler = App.app.workspace.compiler
        def getExtensionManager() = App.app.workspace.getExtensionManager
      }
      SaveModel(org.nlogo.core.Model(), loader, controller, modelTracker, Version).flatMap(_.apply().toOption.map(uri => Paths.get(uri).toString))
    }
  }
}
