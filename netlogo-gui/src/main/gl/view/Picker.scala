// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.event.{ ActionEvent, ActionListener }
import java.awt.Toolkit
import java.util.{ List => JList }
import javax.swing.{ JMenu, JMenuItem, JPopupMenu }

import org.nlogo.core.AgentKind
import org.nlogo.api.{ Agent, Perspective, Turtle }
import org.nlogo.awt.{ Colors, ImageSelection }, Colors.colorize
import org.nlogo.gl.render.{ PickListener, ViewInterface }
import org.nlogo.window.SyntaxColors
import org.nlogo.window.Events.EditWidgetEvent

class Picker(viewManager: ViewManager) extends PickListener {

  def pick(mousePt: java.awt.Point, agents: JList[Agent], view: ViewInterface) {

    val menu = new org.nlogo.swing.WrappingPopupMenu()

    val editItem = new javax.swing.JMenuItem("Edit...")
    editItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          new EditWidgetEvent(viewManager.workspace.viewWidget.settings)
          .raise(view)
        }})
    menu.add(editItem)

    menu.add(new JPopupMenu.Separator)

    val copyItem = new JMenuItem("Copy View")
    copyItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          Toolkit.getDefaultToolkit.getSystemClipboard.setContents(
            new ImageSelection(view.exportView), null)}})
    menu.add(copyItem)

    val exportItem = new JMenuItem("Export View...")
    exportItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          viewManager.workspace.doExportView(viewManager)
        }})
    menu.add(exportItem)

    menu.add(new JPopupMenu.Separator)

    val inspectGlobalsItem = new JMenuItem("inspect globals")
    inspectGlobalsItem.addActionListener(
      new ActionListener {
        override def actionPerformed(p1: ActionEvent) = {
          viewManager.workspace.inspectAgent(AgentKind.Observer)
        }
      }
    )
    menu.add(inspectGlobalsItem)

    menu.add(new JPopupMenu.Separator)

    val resetItem = new javax.swing.JMenuItem(
        "<html>" + colorize("reset-perspective", SyntaxColors.COMMAND_COLOR))
    resetItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          view.resetPerspective()
        }})
    menu.add(resetItem)
    if (viewManager.world.observer.atHome3D) {
      resetItem.setEnabled(false)
      resetItem.setText("reset-perspective")
    }

    var last: Class[_] = null
    import collection.JavaConverters._
    for(agent <- agents.asScala) {
      if (last == null || !last.isInstance(agent)) {
        menu.add(new JPopupMenu.Separator)
        last = agent.getClass
      }
      if (agent.isInstanceOf[Turtle]) {
        val submenu = new AgentMenu(agent, view)
        submenu.add(new AgentMenuItem(agent, Inspect, "inspect", view))
        submenu.add(new JPopupMenu.Separator())
        submenu.add(new AgentMenuItem(agent, Watch, "watch", view))
        submenu.add(new AgentMenuItem(agent, Follow, "follow", view))
        submenu.add(new AgentMenuItem(agent, Ride, "ride", view))
        menu.add(submenu)
      }
      else
        menu.add(new AgentMenuItem(agent, Inspect, "inspect", view))
    }
    if(menu.getSubElements.nonEmpty)
      // move the menu over just a bit from the mouse point, it tends to
      // get in the way in 3D ev 5/12/06
      menu.show(view.canvas,
                mousePt.getX.toInt + 15,
                mousePt.getY.toInt + 15)
  }

  /// context menu

  private class AgentMenu(agent: Agent, view: ViewInterface) extends JMenu(agent.toString) {
    var action: AgentAction = null
    override def menuSelectionChanged(isIncluded: Boolean) {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent else null)
      view.signalViewUpdate()
    }
  }

  sealed trait AgentAction
  case object Inspect extends AgentAction
  case object Follow extends AgentAction
  case object Ride extends AgentAction
  case object Watch extends AgentAction

  def htmlString(agent: Agent, caption: String) =
    "<html>" +
    colorize(caption, SyntaxColors.COMMAND_COLOR) + " "  +
    colorize(agent.classDisplayName, SyntaxColors.REPORTER_COLOR) +
    colorize(agent.toString.drop(agent.classDisplayName.size), SyntaxColors.CONSTANT_COLOR)

  private class AgentMenuItem(val agent: Agent, val action: AgentAction, caption: String, view: ViewInterface)
  extends JMenuItem(htmlString(agent, caption)) {
    addActionListener(new Listener())
    override def menuSelectionChanged(isIncluded: Boolean) {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent else null)
      view.signalViewUpdate()
    }

    class Listener extends ActionListener {
      def actionPerformed(e: ActionEvent) {
        val item = e.getSource.asInstanceOf[AgentMenuItem]
        val observer = viewManager.world.observer
        def update() {
          view.signalViewUpdate()
        }
        item.action match {
          case Inspect =>
            viewManager.workspace.inspectAgent(item.agent, 3)
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
  }

}
