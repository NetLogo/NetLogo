// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

// pulled this out of CommandLine.java so I could translate it separately to Scala - ST 8/19/10

import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ Button, CollapsibleArrow, MenuItem, PopupMenu }

class HistoryPrompt(commandLine: CommandLine) extends Button(null) {
  setAction(new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      doPopupMenu()
    }
  })

  setIcon(new CollapsibleArrow(this, true))

  override def getPreferredSize: Dimension = {
    val height = commandLine.getPreferredSize.height

    new Dimension(height + zoom(3), height)
  }

  private def doPopupMenu(): Unit = {
    val menu = new PopupMenu(this, loc"History")

    if (commandLine.getExecutionList.isEmpty) {
      menu.add(new MenuItem(loc"No History")).setEnabled(false)
    } else {
      commandLine.getExecutionList.foreach { command =>
        val prompt = {
          if (commandLine.agent != null) { // if we're in an agent monitor
            CommandLine.PROMPT
          } else {
            command.getPrompt
          }
        }

        menu.add(new MenuItem(s"$prompt ${command.string}", () => {
          commandLine.setExecutionString(command)
          commandLine.requestFocus()
        }))
      }

      menu.addSeparator()
      menu.add(new MenuItem(loc"or use up and down arrow keys")).setEnabled(false)
      menu.addSeparator()
      menu.add(new MenuItem(loc"Clear History", () => commandLine.clearList()))
    }

    menu.show(this, 0, getHeight)
  }
}
