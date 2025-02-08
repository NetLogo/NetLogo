// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

// pulled this out of CommandLine.java so I could translate it separately to Scala - ST 8/19/10

import java.awt.{ Font, Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JButton }

import org.nlogo.awt.{ Fonts, Mouse }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ MenuItem, PopupMenu, Utils }

class HistoryPrompt(commandLine: CommandLine) extends JButton {

  locally {
    val arrowIcon = Utils.icon("/images/popup.gif")
    setIcon(arrowIcon)
    setDisabledIcon(arrowIcon)
  }
  setEnabled(false)
  setOpaque(true) // needed as of quaqua 3.4.1 - ST 10/4/05
  setFont(new Font(Fonts.platformFont, Font.PLAIN, 9)) // play nice with zoomer
  addMouseListener(
    new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        if(!e.isPopupTrigger && Mouse.hasButton1(e))
          doPopupMenu()}})
  lazy val isMac = System.getProperty("os.name").startsWith("Mac")

  override def getInsets = new Insets(0, 4, 0, 4)  // ad hoc - ST 11/24/04

  private def doPopupMenu(): Unit = {
    val popMenu = new PopupMenu(I18N.gui.get("tabs.run.commandcenter.history"))

    for(ex <- commandLine.getExecutionList) {
      val str =
        if(commandLine.agent != null)          // if we're in an agent monitor
          CommandLine.PROMPT + " " + ex.string
        else {
          val prompt = ex.agentClass match {
            case AgentKind.Observer => CommandLine.OBSERVER_PROMPT
            case AgentKind.Turtle   => CommandLine.TURTLE_PROMPT
            case AgentKind.Patch    => CommandLine.PATCH_PROMPT
            case AgentKind.Link     => CommandLine.LINK_PROMPT
          }
          prompt + " " + ex.string
        }
      popMenu.add(new MenuItem(new AbstractAction(str) {
        def actionPerformed(e: ActionEvent): Unit = {
          commandLine.setExecutionString(ex)
          commandLine.requestFocus()
        }
      }))
    }
    if (commandLine.getExecutionList.isEmpty)
      popMenu.add(new MenuItem(I18N.gui.get("tabs.run.commandcenter.nohistory"))).setEnabled(false)
    else {
      popMenu.addSeparator()
      popMenu.add(new MenuItem(I18N.gui.get("tabs.run.commandcenter.useArrowKeys"))).setEnabled(false)
      popMenu.addSeparator()
      popMenu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.commandcenter.clearHistory")) {
        def actionPerformed(e: ActionEvent): Unit = {
          commandLine.clearList()
        }
      }))
    }
    popMenu.show(this, getWidth / 2, getHeight / 2)
  }

}
