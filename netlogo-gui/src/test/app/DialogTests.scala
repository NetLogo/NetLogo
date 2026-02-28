// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, EventQueue}

import org.nlogo.agent.{ Link, Patch, Turtle }
import org.nlogo.api.{ ModelType, Version }
import org.nlogo.app.tools.{ AgentMonitorManager, LibrariesDialog, ModelsLibraryDialog, PreferencesDialog,
                             PreviewCommandsDialog, PreviewCommandsEditor }
import org.nlogo.core.LibraryInfo
import org.nlogo.editor.EditorArea
import org.nlogo.lab.gui.{ LabManager, ManagerDialog }
import org.nlogo.swing.AutomationUtils
import org.nlogo.util.GuiTest
import org.nlogo.window.GUIWorkspace

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

// test a variety of specific behaviors within dialogs (Isaac B 10/29/25)
class DialogTests extends AnyFunSuite with BeforeAndAfterAll {
  private lazy val frame: AppFrame = App.app.frame
  private lazy val workspace: GUIWorkspace = App.app.workspace
  private lazy val fileManager: FileManager = App.app.fileManager
  private lazy val tabManager: TabManager = App.app.tabManager
  private lazy val monitorManager: AgentMonitorManager = App.app.monitorManager
  private lazy val commandLine: EditorArea = tabManager.interfaceTab.commandCenter.commandLine.textField
  private lazy val labManager: LabManager = App.app.labManager

  test("Search for standard model in Models Library dialog", GuiTest.Tag) {
    ModelsLibraryDialog.open(frame, fileManager.openFromURI(_, ModelType.Library))

    // make sure the dialog is really open (Isaac B 10/31/25)
    EventQueue.invokeAndWait(() => {})

    ModelsLibraryDialog.dialog match {
      case Some(dialog) =>
        if (!AutomationUtils.sendChars(dialog.searchField, "wolf sheep predation"))
          fail("Search field did not receive focus.")

        // wait until the tree has updated its path visibilities (Isaac B 10/31/25)
        if (!AutomationUtils.waitUntilGUI(() => dialog.visibleModels.contains("Wolf Sheep Predation")))
          fail("Models Library dialog did not filter the model tree correctly.")

        EventQueue.invokeAndWait(() => {
          dialog.searchField.setText("")
          dialog.setVisible(false)
        })

      case _ =>
        fail("Models Library dialog did not open.")
    }
  }

  test("Search for imaginary model in Models Library dialog", GuiTest.Tag) {
    ModelsLibraryDialog.open(frame, fileManager.openFromURI(_, ModelType.Library))

    // make sure the dialog is really open (Isaac B 10/31/25)
    EventQueue.invokeAndWait(() => {})

    ModelsLibraryDialog.dialog match {
      case Some(dialog) =>
        if (!AutomationUtils.sendChars(dialog.searchField, "shwibble shwabble"))
          fail("Search field did not receive focus.")

        // wait until the tree has updated its path visibilities (Isaac B 10/31/25)
        if (!AutomationUtils.waitUntilGUI(() => dialog.visibleModels.isEmpty))
          fail("Models Library dialog did not filter the model tree correctly.")

        EventQueue.invokeAndWait(() => {
          dialog.searchField.setText("")
          dialog.setVisible(false)
        })

      case _ =>
        fail("Models Library dialog did not open.")
    }
  }

  test("Change and revert all preferences", GuiTest.Tag) {
    var prefsDialog: Option[PreferencesDialog] = None

    EventQueue.invokeAndWait(() => {
      prefsDialog = Option(new PreferencesDialog(frame, tabManager, tabManager.interfaceTab.iP))
    })

    prefsDialog.foreach { dialog =>
      dialog.syncTheme()
      dialog.setVisible(true)
      dialog.scramble()

      EventQueue.invokeAndWait(() => {
        dialog.reset(true)
      })

      dialog.setVisible(false)
    }
  }

  test("Search for, install, and uninstall extension in Extensions Manager dialog", GuiTest.Tag) {
    App.app.openLibrariesDialog.createDialog() match {
      case dialog: LibrariesDialog =>
        dialog.syncTheme()
        dialog.setVisible(true)

        // make sure the dialog is really open (Isaac B 11/2/25)
        EventQueue.invokeAndWait(() => {})

        val infos: Seq[LibraryInfo] = dialog.searchFor("bspace", 1).getOrElse {
          fail("Extensions Manager dialog did not filter extensions correctly.")
        }

        assert(infos(0).name == "bspace")

        dialog.testInstall(infos(0))

        dialog.setVisible(false)

      case _ =>
        println("Failed to create Extensions Manager dialog")
    }
  }

  test("Search for imaginary extension in Extensions Manager dialog", GuiTest.Tag) {
    App.app.openLibrariesDialog.createDialog() match {
      case dialog: LibrariesDialog =>
        dialog.syncTheme()
        dialog.setVisible(true)

        // make sure the dialog is really open (Isaac B 11/2/25)
        EventQueue.invokeAndWait(() => {})

        if (dialog.searchFor("lasagna", 0).isEmpty)
          fail("Extensions Manager dialog did not filter extensions correctly.")

        dialog.setVisible(false)

      case _ =>
        println("Failed to create Extensions Manager dialog")
    }
  }

  test("Create and inspect turtle in turtle monitor", GuiTest.Tag) {
    sendLineOrFail(commandLine, "clear-all", "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "create-turtles 5 [ setxy random-pxcor random-pycor set color random 140 ]",
                   "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "inspect turtle 3", "Command Center did not receive focus.")

    // wait until the monitor window opens (Isaac B 11/2/25)
    if (!AutomationUtils.waitUntil(monitorManager.areAnyVisible))
      fail("Turtle Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val turtle: Turtle = workspace.world.getTurtle(3)

        assert(window.validateFields(turtle))

        sendLineOrFail(window.commandLine.textField, "set heading 0", "Command line did not receive focus.")

        if (!AutomationUtils.waitUntil(() => turtle.heading == 0))
          fail("Turtle Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Turtle Monitor window failed to open.")
    }
  }

  test("Inspect patch in patch monitor", GuiTest.Tag) {
    sendLineOrFail(commandLine, "clear-all", "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "ask patches [ set pcolor random 140 ]", "Command Center did not receive focus.")

    if (Version.is3D) {
      sendLineOrFail(commandLine, "inspect patch 5 5 5", "Command Center did not receive focus.")
    } else {
      sendLineOrFail(commandLine, "inspect patch 5 5", "Command Center did not receive focus.")
    }

    // wait until the monitor window opens (Isaac B 11/2/25)
    if (!AutomationUtils.waitUntil(monitorManager.areAnyVisible))
      fail("Patch Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val patch: Patch = {
          if (Version.is3D) {
            workspace.world.asInstanceOf[org.nlogo.agent.World3D].getPatchAt(5, 5, 5)
          } else {
            workspace.world.getPatchAt(5, 5)
          }
        }

        assert(window.validateFields(patch))

        sendLineOrFail(window.commandLine.textField, "set pcolor black", "Command line did not receive focus.")

        if (!AutomationUtils.waitUntil(() => patch.pcolor == 0))
          fail("Patch Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Patch Monitor window failed to open.")
    }
  }

  test("Create and inspect link in link monitor", GuiTest.Tag) {
    sendLineOrFail(commandLine, "clear-all", "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "create-turtles 5 [ setxy random-pxcor random-pycor set color random 140 ]",
                   "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "ask turtle 3 [ create-link-with turtle 4 ]", "Command Center did not receive focus.")
    sendLineOrFail(commandLine, "inspect one-of links", "Command Center did not receive focus.")

    // wait until the monitor window opens (Isaac B 11/2/25)
    if (!AutomationUtils.waitUntil(monitorManager.areAnyVisible))
      fail("Link Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val link: Link = workspace.world.getLink(Double.box(3), Double.box(4), workspace.world.links)

        assert(window.validateFields(link))

        sendLineOrFail(window.commandLine.textField, "set thickness 5", "Command line did not receive focus.")

        if (!AutomationUtils.waitUntil(() => link.lineThickness == 5))
          fail("Link Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Link Monitor window failed to open.")
    }
  }

  test("Run standard model preview commands", GuiTest.Tag) {
    if (Version.is3D) {
      fileManager.openFromPath("models/3D/Code Examples/Airplane Landing Example 3D.nlogox3d", ModelType.Library)
    } else {
      fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)
    }

    // wait for any resulting events to be processed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    val dialog = new PreviewCommandsDialog(frame, PreviewCommandsEditor.title, fileManager.currentModel,
                                           workspace.getModelPath, App.app.workspaceFactory, App.app.graphicsPreview,
                                           false)

    dialog.setVisible(true)

    dialog.previewPanel.button.doClick()

    // wait for any resulting events to be processed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    dialog.setVisible(false)
  }

  test("Open and run standard model BehaviorSpace experiment", GuiTest.Tag) {
    if (Version.is3D) {
      fileManager.openFromPath("models/3D/Sample Models/GasLab/GasLab Free Gas 3D.nlogox3d", ModelType.Library)
    } else {
      fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)
    }

    // wait for any resulting events to be processed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    labManager.show()

    // make sure the dialog is really open (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    val dialog: ManagerDialog = labManager.getDialog

    assert(dialog.getExperiments.nonEmpty)

    dialog.select(0)
    dialog.editAndClose()

    // make sure the dialog is really closed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    if (!dialog.runForTesting())
      fail("Experiment was not completed.")

    labManager.close()
  }

  override def beforeAll(): Unit = {
    App.reset()
    App.main(Array("--testing"))
  }

  private def sendLineOrFail(comp: Component, text: String, message: String): Unit = {
    if (!AutomationUtils.sendLine(comp, text))
      fail(message)
  }
}
