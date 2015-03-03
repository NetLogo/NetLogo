// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Window
import org.nlogo.agent.{Agent, Observer, Turtle, Patch, Link}
import org.nlogo.swing.Tiler
import org.nlogo.window.GUIWorkspace

class AgentMonitorManager(val workspace: GUIWorkspace)
extends org.nlogo.window.Event.LinkChild
with org.nlogo.window.Event.LinkParent
{

  private val monitorWindows = collection.mutable.Map[Agent, AgentMonitorWindow]()
  private var emptyTurtleMonitorWindow: AgentMonitorWindow = null
  private var emptyPatchMonitorWindow: AgentMonitorWindow = null
  private var emptyLinkMonitorWindow: AgentMonitorWindow = null
  private val monitorList = collection.mutable.Buffer[Agent]()

  def closeAll() {
    for(win <- monitorWindows.values) {
      win.setVisible(false)
      win.dispose()
    }
    if(emptyTurtleMonitorWindow != null) {
      emptyTurtleMonitorWindow.setVisible(false)
      emptyTurtleMonitorWindow.dispose()
      emptyTurtleMonitorWindow = null
    }
    if(emptyPatchMonitorWindow != null) {
      emptyPatchMonitorWindow.setVisible(false)
      emptyPatchMonitorWindow.dispose()
      emptyPatchMonitorWindow = null
    }
    if(emptyLinkMonitorWindow != null) {
      emptyLinkMonitorWindow.setVisible(false)
      emptyLinkMonitorWindow.dispose()
      emptyLinkMonitorWindow = null
    }
    monitorWindows.clear()
    org.nlogo.window.Event.rehash()
    monitorList.clear()
  }

  def closeTopMonitor() {
    for(topMonitor <- monitorList.headOption.map(monitorWindows)) {
      topMonitor.setVisible(false)
      topMonitor.dispose()
      remove(topMonitor)
    }
  }

  /// Event.LinkChild -- lets us get events out to rest of app
  def getLinkParent = workspace

  /// Event.LinkParent -- lets events pass through us to MonitorWindows
  def getLinkChildren = {
    val list = collection.mutable.ListBuffer[AnyRef](monitorWindows.values.toSeq: _*)
    if(emptyTurtleMonitorWindow != null)
      list += emptyTurtleMonitorWindow
    if(emptyPatchMonitorWindow != null)
      list += emptyPatchMonitorWindow
    if(emptyLinkMonitorWindow != null)
      list += emptyLinkMonitorWindow
    list.toArray
  }

  def agentChangeNotify(window: AgentMonitorWindow, oldAgent: Agent) {
    if(oldAgent != null) {
      monitorWindows -= oldAgent
      monitorList -= oldAgent
    }
    else if(window.agent != null) {
      if(window eq emptyTurtleMonitorWindow)
        emptyTurtleMonitorWindow = null
      if(window eq emptyPatchMonitorWindow)
        emptyPatchMonitorWindow = null
      if(window eq emptyLinkMonitorWindow)
        emptyLinkMonitorWindow = null
    }
    if(window.agent != null) {
      monitorWindows.put(window.agent, window)
      monitorList.prepend(window.agent)
    }
  }

  def remove(window: AgentMonitorWindow) {
    if(window.agent != null) {
      monitorWindows -= window.agent
      monitorList -= window.agent
    }
    if(window eq emptyTurtleMonitorWindow)
      emptyTurtleMonitorWindow = null
    if(window eq emptyPatchMonitorWindow)
      emptyPatchMonitorWindow = null
    if(window eq emptyLinkMonitorWindow)
      emptyLinkMonitorWindow = null
  }

  def inspect(agentClass: Class[_ <: Agent], a0: Agent, radius: Double) {
    val frame = workspace.getFrame
    var window: AgentMonitorWindow = null
    var agent = a0
    if(agent == null && (agentClass eq classOf[Observer]))
      agent = workspace.world.observer()
    if(agent != null)
      window = monitorWindows.get(agent).orNull
    else if(agentClass eq classOf[Turtle])
      window = emptyTurtleMonitorWindow
    else if(agentClass eq classOf[Patch])
      window = emptyPatchMonitorWindow
    else if(agentClass eq classOf[Link])
      window = emptyLinkMonitorWindow
    if(window == null) {
      if(agentClass eq classOf[Observer])
        window = new AgentMonitorWindow(classOf[Observer], agent, radius, this, frame)
      else if(agentClass == classOf[Turtle]) {
        window = new AgentMonitorWindow(classOf[Turtle], agent, radius, this, frame)
        if(agent == null)
          emptyTurtleMonitorWindow = window
      }
      else if(agentClass eq classOf[Patch]) {
        window = new AgentMonitorWindow(classOf[Patch], agent, radius, this, frame)
        if(agent == null)
          emptyPatchMonitorWindow = window
      }
      else if(agentClass eq classOf[Link]) {
        window = new AgentMonitorWindow(classOf[Link], agent, radius, this, frame)
        if(agent == null)
          emptyLinkMonitorWindow = window
      }
      val otherWindows = new java.util.ArrayList[Window]()
      otherWindows.addAll{
        import collection.JavaConverters._
        monitorWindows.values.toList.asJava
      }
      if(emptyTurtleMonitorWindow != null)
        otherWindows.add(emptyTurtleMonitorWindow)
      if(emptyPatchMonitorWindow != null)
        otherWindows.add(emptyPatchMonitorWindow)
      if(emptyLinkMonitorWindow != null)
        otherWindows.add(emptyLinkMonitorWindow)
      window.setLocation(Tiler.findEmptyLocation(otherWindows, window))
    }
    else window.radius(radius)
    window.setVisible(true)
    org.nlogo.window.Event.rehash()
    if(agent == null && (agentClass ne classOf[Observer]))
      window.requestFocus()
    else
      org.nlogo.awt.EventQueue.invokeLater(
        new Runnable() { def run() { frame.requestFocus() }})
  }

  def stopInspecting(agent: Agent) {
    if(agent != null) {
      monitorWindows.get(agent).collect {
        case window: AgentMonitorWindow =>
          window.setVisible(false)
          window.dispose
          remove(window)
      }
    }
  }

  def stopInspectingDeadAgents() {
    monitorWindows.keys.foreach { case agent =>
      if(agent.id == -1)
        stopInspecting(agent)
    }
  }

  def showAll() { showOrHideAll(show = true) }
  def hideAll() { showOrHideAll(show = false) }

  private def showOrHideAll(show: Boolean) {
    for(window <- monitorWindows.values)
      window.setVisible(show)
    if(emptyPatchMonitorWindow != null)
      emptyPatchMonitorWindow.setVisible(show)
    if(emptyTurtleMonitorWindow != null)
      emptyTurtleMonitorWindow.setVisible(show)
    if(emptyLinkMonitorWindow != null)
      emptyLinkMonitorWindow.setVisible(show)
  }

}
