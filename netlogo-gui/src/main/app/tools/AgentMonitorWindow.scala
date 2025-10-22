// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Container, Frame }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, JDialog, LayoutFocusTraversalPolicy }

import scala.jdk.CollectionConverters.SeqHasAsJava

import org.nlogo.agent.{ Agent, Link, Observer, Turtle }
import org.nlogo.analytics.Analytics
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ NetLogoIcon, Utils => SwingUtils }
import org.nlogo.theme.ThemeSync
import org.nlogo.window.{ Event, Events => WindowEvents }

class AgentMonitorWindow(val agentKind: AgentKind, _agent: Agent, radius: Double,
                         manager: AgentMonitorManager, parent: Frame)
  extends JDialog(parent) with Event.LinkChild with WindowEvents.PeriodicUpdateEvent.Handler
  with WindowEvents.PatchesCreatedEvent.Handler with WindowEvents.LoadBeginEvent.Handler with ThemeSync
  with NetLogoIcon {

  private val monitor = {
    agentKind match {
      case AgentKind.Observer => new ObserverMonitor(this)
      case AgentKind.Turtle   => new TurtleMonitor(this)
      case AgentKind.Patch    => new PatchMonitor(this)
      case AgentKind.Link     => new LinkMonitor(this)
    }
  }

  private var dead = false
  private var lastAliveTitle: String = null

  override def getLinkParent = manager
  override def getParent = parent  // parent frame

  monitor.setAgent(_agent, radius)
  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(monitor, BorderLayout.CENTER)
  setFocusTraversalPolicy(
    new LayoutFocusTraversalPolicy {
      override def getFirstComponent(focusCycleRoot: Container) =
        monitor.commandLine.textField
    })
  SwingUtils.addEscKeyAction(
    getRootPane, new AbstractAction {
      def actionPerformed(e: ActionEvent): Unit = {
        close()
      }})
  setTitle(getUpdatedTitle)
  // not sure why the second `pack()` is needed, but without it patch inspectors
  // can show up with their fields initially hidden.  -Jeremy B December 2021
  pack()
  pack()

  // Make sure the window is fully removed
  addWindowListener(
    new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        close()
      }})

  override def requestFocus(): Unit = {
    monitor.requestFocus()
  }

  def agent = monitor.agent

  def radius(radius: Double): Unit = {
    monitor.radius(radius)
  }

  def close(): Unit = {
    setVisible(false)
    monitor.close()
    manager.remove(this)
    dispose()
    Event.rehash()
  }

  def refresh(): Unit = {
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
  def agentChangeNotify(oldAgent: Agent): Unit = {
    dead = agent != null && agent.id == -1
    setTitle(getUpdatedTitle)
    monitor.setPrompt()
    manager.agentChangeNotify(this, oldAgent)
    pack()
  }

  def handle(e: WindowEvents.LoadBeginEvent): Unit = { close() }
  def handle(e: WindowEvents.PeriodicUpdateEvent): Unit = { refresh() }
  def handle(e: WindowEvents.PatchesCreatedEvent): Unit = {
    if(!agent.isInstanceOf[Observer])
      close()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible) {
      monitor.agentKind match {
        case AgentKind.Observer => Analytics.globalsMonitorOpen()
        case AgentKind.Turtle => Analytics.turtleMonitorOpen()
        case AgentKind.Patch => Analytics.patchMonitorOpen()
        case AgentKind.Link => Analytics.linkMonitorOpen()
      }
    }

    pack()

    super.setVisible(visible)
  }

  class ObserverMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window) {
    override def agentKind = AgentKind.Observer
    override def repaintPrompt(): Unit = { }
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
    override def repaintPrompt(): Unit = { }
    override def vars = {
      val turtleVars = workspace.world.program.turtlesOwn
      val allVars: Seq[String] =
        Option(this.agent) match {
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
    override def repaintPrompt(): Unit = { }
    override def vars = workspace.world.program.patchesOwn.asJava
  }

  class LinkMonitor(window: JDialog)
  extends AgentMonitor(manager.workspace, window) {
    override def agentKind = AgentKind.Link
    override def repaintPrompt(): Unit = { }
    override def vars = {
      val linkVars = workspace.world.program.linksOwn
      val allVars: Seq[String] =
        Option(this.agent) match {
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

  override def syncTheme(): Unit = {
    monitor.syncTheme()
  }
}
