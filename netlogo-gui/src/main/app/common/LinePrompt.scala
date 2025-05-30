// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ Color, Graphics }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JLabel }

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ MenuItem, PopupMenu }
import org.nlogo.theme.InterfaceColors

class LinePrompt(commandLine: CommandLine) extends JLabel {
  setText(getPrompt)

  private var mouseInBounds = false

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent): Unit = {
      mouseInBounds = true

      repaint()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      mouseInBounds = false

      repaint()
    }

    override def mousePressed(e: MouseEvent): Unit =  {
      if (isEnabled) {
        val popMenu = new PopupMenu("Ask who?")

        def addItem(name: String, clazz: AgentKind): Unit = {
          popMenu.add(new MenuItem(new AbstractAction(name) {
            def actionPerformed(e: ActionEvent): Unit = {
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
        popMenu.addSeparator()
        popMenu.add(new MenuItem(I18N.gui.get("tabs.run.commandcenter.orusetabkey"))).setEnabled(false)
        popMenu.show(LinePrompt.this, 0, getHeight)
      }
    }
  })

  protected def getPrompt: String = {
    commandLine.kind match {
      case AgentKind.Observer => CommandLine.OBSERVER_PROMPT
      case AgentKind.Turtle   => CommandLine.TURTLE_PROMPT
      case AgentKind.Patch    => CommandLine.PATCH_PROMPT
      case AgentKind.Link     => CommandLine.LINK_PROMPT
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    setText(getPrompt)

    if (mouseInBounds)
      setForeground(Color.BLUE)
    else
      setForeground(InterfaceColors.commandCenterText())

    super.paintComponent(g)
  }
}
