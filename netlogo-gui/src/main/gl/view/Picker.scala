// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.Toolkit
import java.awt.event.{ ActionEvent, ActionListener }
import java.util.{ List => JList }
import javax.swing.AbstractAction

import org.nlogo.api.{ Agent, Perspective, Turtle }
import org.nlogo.awt.{ Colors, ImageSelection }, Colors.colorize
import org.nlogo.core.AgentKind
import org.nlogo.gl.render.PickListener
import org.nlogo.swing.{ Menu, MenuItem, WrappingPopupMenu }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.Events.EditView3DEvent

class Picker(view: View) extends PickListener with ActionListener {

  def pick(mousePt: java.awt.Point, agents: JList[Agent]): Unit = {
    val menu = new WrappingPopupMenu

    menu.add(new MenuItem(new AbstractAction("Edit...") {
      def actionPerformed(e: ActionEvent): Unit = {
        new EditView3DEvent(view.viewManager.workspace.viewWidget.settings).raise(view)
      }
    }))

    menu.addSeparator()

    menu.add(new MenuItem(new AbstractAction("Copy View") {
      def actionPerformed(e: ActionEvent): Unit = {
        Toolkit.getDefaultToolkit.getSystemClipboard.setContents(
          new ImageSelection(view.exportView), null)
      }
    }))

    menu.add(new MenuItem(new AbstractAction("Export View...") {
      def actionPerformed(e: ActionEvent): Unit = {
        view.viewManager.workspace.doExportView(view.viewManager)
      }
    }))

    menu.addSeparator()

    menu.add(new MenuItem(new AbstractAction("inspect globals") {
      def actionPerformed(e: ActionEvent): Unit = {
        view.viewManager.workspace.inspectAgent(AgentKind.Observer)
      }
    }))

    menu.addSeparator()

    val resetItem = new MenuItem(new AbstractAction(
        "<html>" + colorize("reset-perspective", InterfaceColors.commandColor())) {
      def actionPerformed(e: ActionEvent): Unit = {
        view.resetPerspective()
      }
    })

    menu.add(resetItem)

    if (view.viewManager.world.observer.atHome3D) {
      resetItem.setEnabled(false)
      resetItem.setText("reset-perspective")
    }

    var last: Class[?] = null
    import scala.jdk.CollectionConverters.ListHasAsScala
    for(agent <- agents.asScala) {
      if (last == null || !last.isInstance(agent)) {
        menu.addSeparator()
        last = agent.getClass
      }
      if (agent.isInstanceOf[Turtle]) {
        val submenu = new AgentMenu(agent)
        submenu.add(new AgentMenuItem(agent, Inspect, "inspect"))
        submenu.addSeparator()
        submenu.add(new AgentMenuItem(agent, Watch, "watch"))
        submenu.add(new AgentMenuItem(agent, Follow, "follow"))
        submenu.add(new AgentMenuItem(agent, Ride, "ride"))
        menu.add(submenu)
      }
      else
        menu.add(new AgentMenuItem(agent, Inspect, "inspect"))
    }
    if (menu.getSubElements.nonEmpty)
      menu.show(view.canvas, mousePt.getX.toInt, mousePt.getY.toInt)
  }

  /// context menu

  private class AgentMenu(agent: Agent) extends Menu(agent.toString) {
    var action: AgentAction = null
    override def menuSelectionChanged(isIncluded: Boolean): Unit = {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent
                                 else null)
      view.display()
    }
  }

  sealed trait AgentAction
  case object Inspect extends AgentAction
  case object Follow extends AgentAction
  case object Ride extends AgentAction
  case object Watch extends AgentAction

  def htmlString(agent: Agent, caption: String) =
    "<html>" +
    colorize(caption, InterfaceColors.commandColor()) + " "  +
    colorize(agent.classDisplayName, InterfaceColors.reporterColor()) +
    colorize(agent.toString.drop(agent.classDisplayName.size), InterfaceColors.constantColor())

  private class AgentMenuItem(val agent: Agent, val action: AgentAction, caption: String)
  extends MenuItem(htmlString(agent, caption)) {
    addActionListener(Picker.this)
    override def menuSelectionChanged(isIncluded: Boolean): Unit = {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent else null)
      view.display()
    }
  }

  def actionPerformed(e: ActionEvent): Unit = {
    val item = e.getSource.asInstanceOf[AgentMenuItem]
    val observer = view.viewManager.world.observer
    def update(): Unit = {
      view.display()
    }
    item.action match {
      case Inspect =>
        view.viewManager.workspace.inspectAgent(item.agent, 3)
      case Follow =>
        val distance = (item.agent.asInstanceOf[Turtle].size * 5).toInt
        val followDistance = 1 max distance min 100
        observer.setPerspective(Perspective.Follow(item.agent, followDistance))
        update()
      case Ride =>
        observer.setPerspective(Perspective.Ride(item.agent))
        update()
      case Watch =>
        observer.home()
        observer.setPerspective(Perspective.Watch(item.agent))
    }
  }

}
