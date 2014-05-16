// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.{ Agent, Perspective, Turtle }
import org.nlogo.gl.render.PickListener
import org.nlogo.window.SyntaxColors
import org.nlogo.awt.Colors.colorize
import java.awt.event.{ ActionEvent, ActionListener }
import java.util.{ List => JList }
import javax.swing.JPopupMenu
import org.nlogo.agent.Observer

class Picker(view: View) extends PickListener with ActionListener {

  def pick(mousePt: java.awt.Point, agents: JList[Agent]) {

    val menu = new org.nlogo.swing.WrappingPopupMenu()

    val editItem = new javax.swing.JMenuItem("Edit...")
    editItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          new org.nlogo.window.Events.EditWidgetEvent(
            view.viewManager.workspace.viewWidget.settings)
          .raise(view)
        }})
    menu.add(editItem)

    menu.add(new javax.swing.JPopupMenu.Separator)

    val copyItem = new javax.swing.JMenuItem("Copy View")
    copyItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          java.awt.Toolkit.getDefaultToolkit.getSystemClipboard.setContents(
            new org.nlogo.awt.ImageSelection(view.exportView), null)}})
    menu.add(copyItem)

    val exportItem = new javax.swing.JMenuItem("Export View...")
    exportItem.addActionListener(
      new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          view.viewManager.workspace.doExportView(view.viewManager)
        }})
    menu.add(exportItem)

    menu.add(new javax.swing.JPopupMenu.Separator)

    val inspectGlobalsItem = new javax.swing.JMenuItem("inspect globals")
    inspectGlobalsItem.addActionListener(
      new ActionListener {
        override def actionPerformed(p1: ActionEvent) = {
          view.viewManager.workspace.inspectAgent(classOf[Observer])
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
    if (view.viewManager.world.observer.atHome3D) {
      resetItem.setEnabled (false)
      resetItem.setText("reset-perspective")
    }

    var last: Class[_] = null
    import collection.JavaConverters._
    for(agent <- agents.asScala) {
      if (last == null || !last.isInstance(agent)) {
        menu.add(new javax.swing.JPopupMenu.Separator)
        last = agent.getClass
      }
      if (agent.isInstanceOf[Turtle]) {
        val submenu = new AgentMenu(agent)
        submenu.add(new AgentMenuItem(agent, Inspect, "inspect"))
        submenu.add(new javax.swing.JPopupMenu.Separator())
        submenu.add(new AgentMenuItem(agent, Watch, "watch"))
        submenu.add(new AgentMenuItem(agent, Follow, "follow"))
        submenu.add(new AgentMenuItem(agent, Ride, "ride"))
        menu.add(submenu)
      }
      else
        menu.add(new AgentMenuItem(agent, Inspect, "inspect"))
    }
    if(menu.getSubElements.nonEmpty)
      // move the menu over just a bit from the mouse point, it tends to
      // get in the way in 3D ev 5/12/06
      menu.show(view.canvas,
                mousePt.getX.toInt + 15,
                mousePt.getY.toInt + 15)
  }

  /// context menu

  private class AgentMenu(agent: Agent) extends javax.swing.JMenu(agent.toString) {
    var action: AgentAction = null
    override def menuSelectionChanged(isIncluded: Boolean) {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent
                                 else null)
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

  private class AgentMenuItem(val agent: Agent, val action: AgentAction, caption: String)
  extends javax.swing.JMenuItem(htmlString(agent, caption)) {
    addActionListener(Picker.this)
    override def menuSelectionChanged(isIncluded: Boolean) {
      super.menuSelectionChanged(isIncluded)
      view.renderer.outlineAgent(if (isIncluded) agent else null)
      view.signalViewUpdate()
    }
  }

  def actionPerformed(e: ActionEvent) {
    val item = e.getSource.asInstanceOf[AgentMenuItem]
    val observer = view.viewManager.world.observer
    def update() {
      view.signalViewUpdate()
    }
    item.action match {
      case Inspect =>
        view.viewManager.workspace.inspectAgent(item.agent, 3)
      case Follow =>
        observer.setPerspective(Perspective.Follow, item.agent)
        val distance = (item.agent.asInstanceOf[Turtle].size * 5).toInt
        observer.followDistance(1 max distance min 100)
        update()
      case Ride =>
        observer.setPerspective(Perspective.Ride, item.agent)
        observer.followDistance(0)
        update()
      case Watch =>
        observer.home()
        observer.setPerspective(Perspective.Watch, item.agent)
    }
  }

}
