// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Container, Frame }
import java.awt.event.{ ActionEvent,  WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, BorderFactory, JDialog, LayoutFocusTraversalPolicy }

import scala.collection.JavaConverters._

import org.nlogo.agent.{ Agent, Link, Observer, Turtle }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.window.{ Event, Events => WindowEvents }

class AgentMonitorWindow(agentKind: AgentKind, _agent: Agent, radius: Double,
                         manager: AgentMonitorManager, parent: Frame)
extends JDialog(parent)
with Event.LinkChild
with WindowEvents.PeriodicUpdateEvent.Handler
with WindowEvents.PatchesCreatedEvent.Handler
with WindowEvents.LoadBeginEvent.Handler
{

  private val monitor = {
    agentKind match {
      case AgentKind.Observer => new ObserverMonitor(this)
      case AgentKind.Turtle   => new TurtleMonitor(this)
      case AgentKind.Patch    => new PatchMonitor(this)
      case AgentKind.Link     => new LinkMonitor(this)
    }
  }

  private val wasShiftDownOnCloseBox = false // var aab
  private var dead = false
  private var lastAliveTitle: String = null

  override def getLinkParent = manager
  override def getParent = parent  // parent frame

  monitor.setAgent(_agent, radius)
  if(!System.getProperty("os.name").startsWith("Mac"))
    getRootPane.setBorder(BorderFactory.createRaisedBevelBorder)
  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(monitor, BorderLayout.CENTER)
  setFocusTraversalPolicy(
    new LayoutFocusTraversalPolicy {
      override def getFirstComponent(focusCycleRoot: Container) =
        monitor.commandLine.textField
    })
  // Add check for shift-key to close all windows
  addWindowListener(
    new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        if(wasShiftDownOnCloseBox) manager.closeAll()
        else close()
      }})
  SwingUtils.addEscKeyAction(
    getRootPane, new AbstractAction {
      def actionPerformed(e: ActionEvent) {
        close()
      }})
  agentChangeNotify(null)
  pack()

  override def requestFocus() {
    monitor.requestFocus()
  }

  def agent = monitor.agent

  def radius(radius: Double) {
    monitor.radius(radius)
  }

  def close() {
    setVisible(false)
    monitor.close()
    manager.remove(this)
    dispose()
    Event.rehash()
  }

  def refresh() {
    if(!dead && agent != null && agent.id == -1) {
      dead = true
      setTitle(getUpdatedTitle)
    }
    val oldPref = monitor.getPreferredSize
    monitor.refresh()
    val newPref = monitor.getPreferredSize
    if(oldPref != newPref)
      pack()
    if(getUpdatedTitle != getTitle)
      setTitle(getUpdatedTitle)
  }

def getUpdatedTitle = {
    monitor.agentKind match {
      case AgentKind.Observer => I18N.gui.get("tools.agentMonitor.window.globals")
      case AgentKind.Turtle if agent == null => I18N.gui.get("tools.agentMonitor.window.noTurtle")
      case AgentKind.Turtle if agent.id == -1 => I18N.gui.getN("tools.agentMonitor.window.dead", lastAliveTitle)
      case AgentKind.Turtle =>
        lastAliveTitle = agent.toString
        lastAliveTitle
      case AgentKind.Link if agent == null => I18N.gui.get("tools.agentMonitor.window.noLink")
      case AgentKind.Link if agent.id == -1 => I18N.gui.getN("tools.agentMonitor.window.dead", lastAliveTitle)
      case AgentKind.Link =>
        lastAliveTitle = agent.toString
        lastAliveTitle
      case AgentKind.Patch if agent == null => I18N.gui.get("tools.agentMonitor.window.noPatch")
      case AgentKind.Patch => agent.toString
    }
  }

  // KLUDGE
  def agentChangeNotify(oldAgent: Agent) {
    dead = agent != null && agent.id == -1
    setTitle(getUpdatedTitle)
    manager.agentChangeNotify(this, oldAgent)
    pack()
  }

  def handle(e: WindowEvents.LoadBeginEvent) { close() }
  def handle(e: WindowEvents.PeriodicUpdateEvent) { refresh() }
  def handle(e: WindowEvents.PatchesCreatedEvent) {
    if(!agent.isInstanceOf[Observer])
      close()
  }

  class ObserverMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window) {
    override def agentKind = AgentKind.Observer
    override def repaintPrompt() { }
    override def vars = {
      val allGlobals = workspace.world.program.globals
      allGlobals
        .drop(workspace.world.program.interfaceGlobals.size)
        .toList.asJava
    }
  }

  class TurtleMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window){
    override def agentKind = AgentKind.Turtle
    override def repaintPrompt() { }
    override def vars = {
      val turtleVars = workspace.world.program.turtlesOwn
      val allVars: Seq[String] =
        Option(agent) match {
          case Some(t: Turtle) if t.getBreed != workspace.world.turtles =>
            turtleVars ++ workspace.world.program.breeds.get(t.getBreed.printName).map(_.owns).getOrElse(Seq())
          case _ => turtleVars
        }
      allVars.asJava
    }
  }

  class PatchMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window) {
    override def agentKind = AgentKind.Patch
    override def repaintPrompt() { }
    override def vars = workspace.world.program.patchesOwn.asJava
  }

  class LinkMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window) {
    override def agentKind = AgentKind.Link
    override def repaintPrompt() { }
    override def vars = {
      val linkVars = workspace.world.program.linksOwn
      val allVars: Seq[String] =
        Option(agent) match {
          case Some(l: Link) if l.getBreed != workspace.world.links =>
            linkVars ++ workspace.world.program.linkBreeds
              .get(l.getBreed.printName)
              .map(_.owns)
              .getOrElse(Seq())
          case _ => linkVars
        }
      allVars.asJava
    }
  }

}
