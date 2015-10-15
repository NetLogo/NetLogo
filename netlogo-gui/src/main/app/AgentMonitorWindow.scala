// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app


import org.nlogo.agent.{Agent, Link, Observer, Patch, Turtle}

import scala.collection.JavaConverters._

class AgentMonitorWindow(agentClass: Class[_ <: Agent], _agent: Agent, radius: Double,
                         manager: AgentMonitorManager, parent: java.awt.Frame)
// JWindow not JFrame so we can float on top of the App window - ev 1/7/09
extends javax.swing.JWindow(parent)
with org.nlogo.window.Event.LinkChild
with org.nlogo.window.Events.PeriodicUpdateEvent.Handler
with org.nlogo.window.Events.PatchesCreatedEvent.Handler
with org.nlogo.window.Events.LoadBeginEvent.Handler
{

  private val monitor = {
    val O = classOf[Observer]; val T = classOf[Turtle]
    val P = classOf[Patch];    val L = classOf[Link]
    agentClass match {
      case O => new ObserverMonitor(this)
      case T => new TurtleMonitor(this)
      case P => new PatchMonitor(this)
      case L => new LinkMonitor(this)
    }
  }

  private val dragger = new org.nlogo.swing.WindowDragger(this)
  private var wasShiftDownOnCloseBox = false
  private var dead = false
  private var lastAliveTitle: String = null

  override def getLinkParent = manager
  override def getParent = parent  // parent frame

  monitor.setAgent(_agent, radius)
  if(!System.getProperty("os.name").startsWith("Mac"))
    getRootPane.setBorder(javax.swing.BorderFactory.createRaisedBevelBorder)
  getContentPane.setLayout(new java.awt.BorderLayout)
  getContentPane.add(monitor, java.awt.BorderLayout.CENTER)
  getContentPane.add(dragger, java.awt.BorderLayout.NORTH)
  getContentPane.add(new org.nlogo.swing.WindowResizer(this),
                     java.awt.BorderLayout.SOUTH)
  setFocusTraversalPolicy(
    new javax.swing.LayoutFocusTraversalPolicy() {
      override def getFirstComponent(focusCycleRoot: java.awt.Container) =
        monitor.commandLine.textField
    })
  // Add check for shift-key to close all windows
  addWindowListener(
    new java.awt.event.WindowAdapter() {
      override def windowClosing(e: java.awt.event.WindowEvent) {
        if(wasShiftDownOnCloseBox) manager.closeAll()
        else close()
      }})
  dragger.getCloseBox.addMouseListener(
    new java.awt.event.MouseAdapter() {
      override def mousePressed(e: java.awt.event.MouseEvent) {
        wasShiftDownOnCloseBox = e.isShiftDown
      }})
  org.nlogo.swing.Utils.addEscKeyAction(
    getRootPane, new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) {
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
    org.nlogo.window.Event.rehash()
  }

  def refresh() {
    if(!dead && agent != null && agent.id == -1) {
      dead = true
      dragger.setTitle(title)
    }
    val oldPref = monitor.getPreferredSize
    monitor.refresh()
    val newPref = monitor.getPreferredSize
    if(oldPref != newPref)
      pack()
    if(title != dragger.getTitle)
      dragger.setTitle(title)
  }

  def title = {
    val O = classOf[Observer]; val T = classOf[Turtle]
    val P = classOf[Patch];    val L = classOf[Link]
    monitor.agentClass match {
      case O => "Globals"
      case T if agent == null => "(no turtle)"
      case T if agent.id == -1 => lastAliveTitle + " (dead)"
      case T =>
        lastAliveTitle = agent.toString
        lastAliveTitle
      case L if agent == null => "(no link)"
      case L if agent.id == -1 => lastAliveTitle + " (dead)"
      case L =>
        lastAliveTitle = agent.toString
        lastAliveTitle
      case P if agent == null => "(no patch)"
      case P => agent.toString
    }
  }

  // KLUDGE
  def agentChangeNotify(oldAgent: Agent) {
    dead = agent != null && agent.id == -1
    dragger.setTitle(title)
    manager.agentChangeNotify(this, oldAgent)
    pack()
  }

  def handle(e: org.nlogo.window.Events.LoadBeginEvent) { close() }
  def handle(e: org.nlogo.window.Events.PeriodicUpdateEvent) { refresh() }
  def handle(e: org.nlogo.window.Events.PatchesCreatedEvent) {
    if(!agent.isInstanceOf[Observer])
      close()
  }

  class ObserverMonitor(window: javax.swing.JWindow)
  extends AgentMonitor(manager.workspace, window) {
    override def agentClass = classOf[Observer]
    override def repaintPrompt() { }
    override def vars = {
      val allGlobals = workspace.world.program.globals
      import collection.JavaConverters._
      allGlobals
        .drop(workspace.world.program.interfaceGlobals.size)
        .toList.asJava
    }
  }

  class TurtleMonitor(window: javax.swing.JWindow)
  extends AgentMonitor(manager.workspace, window){
    override def agentClass = classOf[Turtle]
    override def repaintPrompt() { }
    override def vars = {
      val turtleVars = workspace.world.program.turtlesOwn
      val allVars: Seq[String] =
        Option(agent) match {
          case Some(t: Turtle) if t.getBreed != workspace.world.turtles() =>
            turtleVars ++ workspace.world.program.breeds.get(t.getBreed.printName).map(_.owns).getOrElse(Seq())
          case _ => turtleVars
        }
      allVars.asJava
    }
  }

  class PatchMonitor(window: javax.swing.JWindow)
  extends AgentMonitor(manager.workspace, window) {
    override def agentClass = classOf[Patch]
    override def repaintPrompt() { }
    override def vars = workspace.world.program.patchesOwn.asJava
  }

  class LinkMonitor(window: javax.swing.JWindow)
  extends AgentMonitor(manager.workspace, window) {
    override def agentClass = classOf[Link]
    override def repaintPrompt() { }
    override def vars = {
      val linkVars = workspace.world.program.linksOwn
      val allVars: Seq[String] =
        Option(agent) match {
          case Some(t: Turtle) if t.getBreed != workspace.world.links() =>
            linkVars ++ workspace.world.program.linkBreeds.get(t.getBreed.printName).map(_.owns).getOrElse(Seq())
          case _ => linkVars
        }
      allVars.asJava
    }
  }

}
