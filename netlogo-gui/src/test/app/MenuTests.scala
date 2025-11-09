// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, EventQueue }
import javax.swing.{ JMenu, JMenuItem }

import org.nlogo.swing.{ AutomationUtils, WindowAutomator }
import org.nlogo.util.GuiTest

import org.scalatest.funspec.AnyFunSpec

// make sure all the menu actions for each tab can be executed without the app blowing up (Isaac B 10/29/25)
class MenuTests extends AnyFunSpec {
  private lazy val tabManager: TabManager = App.app.tabManager
  private lazy val menuBar: MainMenuBar = App.app.mainMenuBar

  App.reset()
  App.main(Array("--testing", "--automated"))

  // there are different menu actions for each tab, so test each one separately (Isaac B 10/29/25)
  describe("Interface Tab") {
    actionsForTab(tabManager.interfaceTab)
  }

  describe("Info Tab") {
    actionsForTab(tabManager.infoTab)
  }

  describe("Code Tab") {
    actionsForTab(tabManager.mainCodeTab)
  }

  private def actionsForTab(tab: Component): Unit = {
    EventQueue.invokeAndWait(() => {
      tabManager.setSelectedTab(tab)
    })

    menuBar.getComponents.foreach(recurseMenu(tab, _, Seq()))
  }

  // for some reason the action names have unprintable characters at the end, and the
  // test output looks nicer with those removed (Isaac B 10/30/25)
  private def cleanText(text: String): String =
    text.replaceAll("[^\\x00-\\x7F]", "")

  private def recurseMenu(tab: Component, comp: Component, path: Seq[String]): Unit = {
    comp match {
      case menu: JMenu =>
        val newPath = path :+ cleanText(menu.getText)

        menu.getMenuComponents.foreach(recurseMenu(tab, _, newPath))

      case item: JMenuItem =>
        // exclude Quit because that exits the JVM and aborts the test suite (Isaac B 10/30/25)
        if (!item.getAction.isInstanceOf[FileManager.QuitAction]) {
          it((path :+ cleanText(item.getText)).mkString(" -> "), GuiTest.Tag) {
            if (AutomationUtils.waitForGUI(() => {
              // select the correct tab again here, just in case it got reset to the Interface Tab and this action
              // is set to be ignored when its tab is not selected (Isaac B 10/30/25)
              tabManager.setSelectedTab(tab)

              item.doClick()
            }).isEmpty) {
              fail("Menu action timed out, there is likely an EventQueue deadlock or an infinite loop.")
            }

            // clear EventQueue so non-modal dialogs can receive their close events (Isaac B 10/30/25)
            EventQueue.invokeAndWait(() => {})

            WindowAutomator.getVisibleWindows
              .headOption.foreach { window =>

              alert("Failed to close window: " + window.getClass.getName)

              window.setVisible(false)
            }
          }
        }

      case _ =>
    }
  }
}
