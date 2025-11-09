// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.EventQueue
import java.awt.event.KeyEvent

import org.nlogo.api.{ ModelType, Version }
import org.nlogo.app.interfacetab.{ InterfacePanel, InterfaceTab, InterfaceWidgetControls }
import org.nlogo.swing.{ AutomationUtils, MenuItem }
import org.nlogo.util.GuiTest
import org.nlogo.window.InterfaceMode

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Random

// test important interactions on the Interface Tab (Isaac B 11/8/25)
class InterfaceTests extends AnyFunSuite with BeforeAndAfterAll {
  private lazy val fileManager: FileManager = App.app.fileManager
  private lazy val interfaceTab: InterfaceTab = App.app.tabManager.interfaceTab
  private lazy val interfacePanel: InterfacePanel = interfaceTab.iP
  private lazy val widgetControls: InterfaceWidgetControls = interfaceTab.widgetControls

  test("Add all widgets from dropdown", GuiTest.Tag) {
    widgetControls.openWidgetMenu() match {
      case Some(widgetMenu) =>
        widgetMenu.getComponents.foreach {
          case item: MenuItem =>
            val targetSize = interfacePanel.getPermanentWidgets.size + 1

            EventQueue.invokeAndWait(() => {
              item.doClick()
            })

            if (!AutomationUtils.waitUntil(() => interfacePanel.getInterfaceMode == InterfaceMode.Add))
              fail("Interface mode did not change to Add.")

            val x = Random.nextInt(500)
            val y = Random.nextInt(500)

            AutomationUtils.sendClick(interfacePanel, x, y)

            if (!AutomationUtils.waitUntil(() => interfacePanel.getPermanentWidgets.size == targetSize))
              fail("New widget was not successfully placed.")

          case _ =>
        }

      case _ =>
        fail("Add Widget menu failed to open.")
    }
  }

  test("Perform all widget alignment actions from dropdown on minimal model", GuiTest.Tag) {
    if (Version.is3D) {
      fileManager.openFromPath("models/test/Alignment.nlogox3d", ModelType.Library)
    } else {
      fileManager.openFromPath("models/test/Alignment.nlogox", ModelType.Library)
    }

    EventQueue.invokeAndWait(() => {
      widgetControls.selectButton.doClick()
    })

    AutomationUtils.sendDrag(interfacePanel, 0, 0, 200, 200)

    widgetControls.openAlignmentMenu() match {
      case Some(alignmentMenu) =>
        alignmentMenu.getComponents.foreach {
          case item: MenuItem =>
            item.doClick()

            // make sure any resulting events are processed (Isaac B 11/9/25)
            EventQueue.invokeAndWait(() => {})

            // undo each alignment action, otherwise the widgets will converge at one point
            // and the rest of the alignment actions won't do anything (Isaac B 11/9/25)
            if (!AutomationUtils.sendKey(interfacePanel, KeyEvent.VK_Z, true))
              fail("Interface panel did not receive focus.")

          case _ =>
        }

      case _ =>
        fail("Align Widgets menu failed to open.")
    }
  }

  test("Perform all widget alignment actions from dropdown on standard model", GuiTest.Tag) {
    if (Version.is3D) {
      fileManager.openFromPath("models/3D/Sample Models/Life 3D.nlogox3d", ModelType.Library)
    } else {
      fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)
    }

    EventQueue.invokeAndWait(() => {
      widgetControls.selectButton.doClick()
    })

    AutomationUtils.sendDrag(interfacePanel, 0, 0, interfacePanel.getWidth, interfacePanel.getHeight)

    widgetControls.openAlignmentMenu() match {
      case Some(alignmentMenu) =>
        alignmentMenu.getComponents.foreach {
          case item: MenuItem =>
            EventQueue.invokeAndWait(() => {
              item.doClick()
            })

            // make sure any resulting events are processed (Isaac B 11/9/25)
            EventQueue.invokeAndWait(() => {})

            // undo each alignment action, otherwise the widgets will converge at one point
            // and the rest of the alignment actions won't do anything (Isaac B 11/9/25)
            if (!AutomationUtils.sendKey(interfacePanel, KeyEvent.VK_Z, true))
              fail("Interface panel did not receive focus.")

          case _ =>
        }

      case _ =>
        fail("Align Widgets menu failed to open.")
    }
  }

  test("Edit all widgets in standard model with Edit tool", GuiTest.Tag) {
    if (Version.is3D) {
      fileManager.openFromPath("models/3D/Sample Models/Life 3D.nlogox3d", ModelType.Library)
    } else {
      fileManager.openFromPath("models/Sample Models/Biology/Wolf Sheep Predation.nlogox", ModelType.Library)
    }

    EventQueue.invokeAndWait(() => {
      widgetControls.editButton.doClick()
    })

    interfacePanel.getPermanentWidgets.foreach { widget =>
      AutomationUtils.sendClick(interfacePanel, widget.getX + widget.getWidth / 2, widget.getY + widget.getHeight / 2)

      // wait for widget edit to complete (Isaac B 11/9/25)
      EventQueue.invokeAndWait(() => {})

      assert(!App.app.dirtyMonitor.modelDirty)
    }
  }

  override def beforeAll(): Unit = {
    App.reset()
    App.main(Array("--testing", "--automated"))
  }
}
