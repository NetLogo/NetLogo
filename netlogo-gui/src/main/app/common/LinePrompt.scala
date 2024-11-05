// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ Color, Graphics }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JLabel, JPopupMenu }

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.PopupMenuItem
import org.nlogo.theme.InterfaceColors

class LinePrompt(commandLine: CommandLine) extends JLabel {
  setText(getPrompt)

  private var mouseInBounds = false

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent) {
      mouseInBounds = true

      repaint()
    }

    override def mouseExited(e: MouseEvent) {
      mouseInBounds = false

      repaint()
    }

    override def mousePressed(e: MouseEvent)  {
      if (isEnabled) {
        val popMenu = new JPopupMenu("Ask who?")

        popMenu.setBackground(InterfaceColors.POPUP_BACKGROUND)

        def addItem(name: String, clazz: AgentKind) {
          popMenu.add(new PopupMenuItem(new AbstractAction(name) {
            def actionPerformed(e: ActionEvent) {
              commandLine.agentKind(clazz)
              LinePrompt.this.repaint()
              commandLine.requestFocus()
            }
          }))
        }
        addItem(I18N.gui.get("common.observer"), AgentKind.Observer)
        addItem(I18N.gui.get("common.turtles"), AgentKind.Turtle)
        addItem(I18N.gui.get("common.patches"), AgentKind.Patch)
        addItem(I18N.gui.get("common.links"), AgentKind.Link)
        popMenu.add(new JPopupMenu.Separator)
        popMenu.add(new PopupMenuItem(I18N.gui.get("tabs.run.commandcenter.orusetabkey"))).setEnabled(false)
        popMenu.show(LinePrompt.this, 0, getHeight)
      }
    }
  })

  private def getPrompt = {
    commandLine.kind match {
      case AgentKind.Observer => CommandLine.OBSERVER_PROMPT
      case AgentKind.Turtle   => CommandLine.TURTLE_PROMPT
      case AgentKind.Patch    => CommandLine.PATCH_PROMPT
      case AgentKind.Link     => CommandLine.LINK_PROMPT
    }
  }

  override def paintComponent(g: Graphics) {
    setText(getPrompt)

    if (mouseInBounds)
      setForeground(Color.BLUE)
    else
      setForeground(InterfaceColors.COMMAND_CENTER_TEXT)

    super.paintComponent(g)
  }
}
