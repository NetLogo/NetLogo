// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, EventQueue, Toolkit }
import java.awt.event.KeyEvent
import java.util.concurrent.TimeoutException

import org.nlogo.agent.{ Link, Patch, Turtle }
import org.nlogo.api.ModelType
import org.nlogo.app.common.CommandLine
import org.nlogo.app.tools.{ AgentMonitorManager, LibrariesDialog, ModelsLibraryDialog, PreferencesDialog,
                             PreviewCommandsDialog, PreviewCommandsEditor }
import org.nlogo.core.LibraryInfo
import org.nlogo.lab.gui.{ LabManager, ManagerDialog }
import org.nlogo.util.GuiTest
import org.nlogo.window.GUIWorkspace

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.{ Await, Promise }
import scala.concurrent.duration.{ Duration, SECONDS }

// test a variety of specific behaviors within dialogs (Isaac B 10/29/25)
class DialogTests extends AnyFunSuite with BeforeAndAfterAll {
  private lazy val frame: AppFrame = App.app.frame
  private lazy val workspace: GUIWorkspace = App.app.workspace
  private lazy val fileManager: FileManager = App.app.fileManager
  private lazy val tabManager: TabManager = App.app.tabManager
  private lazy val monitorManager: AgentMonitorManager = App.app.monitorManager
  private lazy val commandLine: CommandLine = tabManager.interfaceTab.commandCenter.commandLine
  private lazy val labManager: LabManager = App.app.labManager

  private lazy val eventQueue: EventQueue = Toolkit.getDefaultToolkit.getSystemEventQueue

  test("Search for standard model in Models Library dialog", GuiTest.Tag) {
    ModelsLibraryDialog.open(frame, fileManager.openFromURI(_, ModelType.Library))

    // make sure the dialog is really open (Isaac B 10/31/25)
    EventQueue.invokeAndWait(() => {})

    ModelsLibraryDialog.dialog match {
      case Some(dialog) =>
        sendChars(dialog.searchField, "wolf sheep predation")

        // wait until the tree has updated its path visibilities (Isaac B 10/31/25)
        waitUntil(() => dialog.visibleModels.contains("Wolf Sheep Predation"),
                  "Models Library dialog did not filter the model tree correctly.")

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
        sendChars(dialog.searchField, "shwibble shwabble")

        // wait until the tree has updated its path visibilities (Isaac B 10/31/25)
        waitUntil(() => dialog.visibleModels.isEmpty, "Models Library dialog did not filter the model tree correctly.")

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

        val infos: Seq[LibraryInfo] = dialog.searchFor("bspace")

        assert(infos.size == 1)
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

        assert(dialog.searchFor("lasagna").isEmpty)

        dialog.setVisible(false)

      case _ =>
        println("Failed to create Extensions Manager dialog")
    }
  }

  test("Create and inspect turtle in turtle monitor", GuiTest.Tag) {
    sendLine(commandLine, "clear-all")
    sendLine(commandLine, "create-turtles 5 [ setxy random-pxcor random-pycor set color random 140 ]")
    sendLine(commandLine, "inspect turtle 3")

    // wait until the monitor window opens (Isaac B 11/2/25)
    waitUntil(monitorManager.areAnyVisible, "Turtle Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val turtle: Turtle = workspace.world.getTurtle(3)

        assert(window.validateFields(turtle))

        sendLine(window.commandLine, "set heading 0")

        waitUntil(() => turtle.heading == 0, "Turtle Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Turtle Monitor window failed to open.")
    }
  }

  test("Inspect patch in patch monitor", GuiTest.Tag) {
    sendLine(commandLine, "clear-all")
    sendLine(commandLine, "ask patches [ set pcolor random 140 ]")
    sendLine(commandLine, "inspect patch 5 5")

    // wait until the monitor window opens (Isaac B 11/2/25)
    waitUntil(monitorManager.areAnyVisible, "Patch Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val patch: Patch = workspace.world.getPatchAt(5, 5)

        assert(window.validateFields(patch))

        sendLine(window.commandLine, "set pcolor black")

        waitUntil(() => patch.pcolor == 0, "Patch Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Patch Monitor window failed to open.")
    }
  }

  test("Create and inspect link in link monitor", GuiTest.Tag) {
    sendLine(commandLine, "clear-all")
    sendLine(commandLine, "create-turtles 5 [ setxy random-pxcor random-pycor set color random 140 ]")
    sendLine(commandLine, "ask turtle 3 [ create-link-with turtle 4 ]")
    sendLine(commandLine, "inspect one-of links")

    // wait until the monitor window opens (Isaac B 11/2/25)
    waitUntil(monitorManager.areAnyVisible, "Link Monitor window failed to open.")

    monitorManager.getMonitorWindows.headOption match {
      case Some(window) =>
        val link: Link = workspace.world.getLink(Double.box(3), Double.box(4), workspace.world.links)

        assert(window.validateFields(link))

        sendLine(window.commandLine, "set thickness 5")

        waitUntil(() => link.lineThickness == 5, "Link Monitor window failed to run command.")

        window.close()

      case _ =>
        fail("Link Monitor window failed to open.")
    }
  }

  test("Run standard model preview commands", GuiTest.Tag) {
    fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)

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
    fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)

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

    dialog.runForTesting()

    labManager.close()
  }

  override def beforeAll(): Unit = {
    App.reset()
    App.main(Array("--testing"))
  }

  private def sendChars(comp: Component, text: String): Unit = {
    text.foreach { char =>
      eventQueue.postEvent(new KeyEvent(comp, KeyEvent.KEY_TYPED, System.currentTimeMillis, 0, KeyEvent.VK_UNDEFINED,
                                        char))
    }
  }

  private def sendLine(comp: Component, text: String): Unit = {
    sendChars(comp, text)

    eventQueue.postEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 0, KeyEvent.VK_ENTER,
                                      KeyEvent.CHAR_UNDEFINED))
  }

  private def waitUntil(test: () => Boolean, message: String): Unit = {
    val promise = Promise[Unit]()

    new Thread {
      override def run(): Unit = {
        while (!test())
          Thread.sleep(250)

        promise.success({})
      }
    }.start()

    try {
      Await.ready(promise.future, Duration(5, SECONDS))
    } catch {
      case _: TimeoutException =>
        fail(message)
    }
  }
}
