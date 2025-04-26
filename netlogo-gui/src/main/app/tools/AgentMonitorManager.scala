// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Window

import scala.collection.mutable

import org.nlogo.agent.Agent
import org.nlogo.core.AgentKind
import org.nlogo.swing.Tiler
import org.nlogo.theme.ThemeSync
import org.nlogo.window.{ Event, GUIWorkspace }

class AgentMonitorManager(val workspace: GUIWorkspace) extends Event.LinkChild with Event.LinkParent with ThemeSync {
  private val monitorWindows      = mutable.Map[Agent, AgentMonitorWindow]()
  private val emptyMonitorWindows = mutable.Map[AgentKind, AgentMonitorWindow]()
  private val monitorList         = collection.mutable.Buffer[Agent]()

  def closeAll(): Unit = {
    monitorWindows.values.foreach( (w) => {
      w.setVisible(false)
      w.dispose()
    })
    monitorWindows.clear()

    emptyMonitorWindows.values.foreach( (w) => {
      w.setVisible(false)
      w.dispose()
    })
    emptyMonitorWindows.clear()

    org.nlogo.window.Event.rehash()
    monitorList.clear()
  }

  def closeTopMonitor(): Unit = {
    for (topMonitor <- monitorList.headOption.map(monitorWindows)) {
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
    emptyMonitorWindows.values.foreach( (w) => list += w )
    list.toArray
  }

  def agentChangeNotify(window: AgentMonitorWindow, oldAgent: Agent): Unit = {
    if (oldAgent == null) {
      if (window.agent != null) {
        emptyMonitorWindows.remove(window.agent.kind)
      }
    } else {
      monitorWindows -= oldAgent
      monitorList    -= oldAgent
    }
    if (window.agent != null) {
      monitorWindows.put(window.agent, window)
      monitorList.prepend(window.agent)
    }
  }

  def remove(window: AgentMonitorWindow): Unit = {
    if (window.agent != null) {
      monitorWindows -= window.agent
      monitorList    -= window.agent
    } else {
      emptyMonitorWindows.remove(window.agentKind)
    }
  }

  private def newWindow(agentKind: AgentKind, agent: Agent, radius: Double): AgentMonitorWindow = {
    val window = new AgentMonitorWindow(agentKind, agent, radius, this, workspace.getFrame)

    val otherWindows = new java.util.ArrayList[Window]()
    monitorWindows.values.foreach(otherWindows.add(_))
    emptyMonitorWindows.values.foreach(otherWindows.add(_))
    window.setLocation(Tiler.findEmptyLocation(otherWindows, window))

    if (agent != null) {
      monitorWindows.put(agent, window)
      monitorList.prepend(window.agent)
    } else {
      emptyMonitorWindows.put(agentKind, window)
    }

    window
  }

  def inspect(agentKind: AgentKind, a0: Agent, radius: Double): Unit = {
    val agent = if (a0 == null && agentKind == AgentKind.Observer) {
      workspace.world.observer
    } else {
      a0
    }

    val window = if (agent != null) {
      monitorWindows.getOrElse(agent, newWindow(agentKind, agent, radius))
    } else {
      emptyMonitorWindows.getOrElse(agentKind, newWindow(agentKind, agent, radius))
    }

    window.syncTheme()

    window.radius(radius)
    window.setVisible(true)
    org.nlogo.window.Event.rehash()
    if (agent == null && agentKind != AgentKind.Observer) {
      window.requestFocus()
    }
  }

  def stopInspecting(agent: Agent): Unit = {
    if (agent != null) {
      monitorWindows.get(agent).collect {
        case window: AgentMonitorWindow =>
          window.setVisible(false)
          window.dispose
          remove(window)
      }
    }
  }

  def stopInspectingDeadAgents(): Unit = {
    monitorWindows.keys.foreach { case agent =>
      if (agent.id == -1) {
        stopInspecting(agent)
      }
    }
  }

  def showAll(): Unit = { showOrHideAll(show = true) }
  def hideAll(): Unit = { showOrHideAll(show = false) }

  private def showOrHideAll(show: Boolean): Unit = {
    monitorWindows.values.foreach(monitor => {
      monitor.syncTheme()
      monitor.setVisible(show)
    })
    emptyMonitorWindows.values.foreach(_.setVisible(show))
  }

  def areAnyVisible(): Boolean = {
    monitorWindows.values.exists(_.isVisible()) || emptyMonitorWindows.values.exists(_.isVisible())
  }

  def refresh(): Unit = {
    monitorWindows.values.foreach(_.refresh())
  }

  override def syncTheme(): Unit = {
    monitorWindows.values.foreach(_.syncTheme())
    emptyMonitorWindows.values.foreach(_.syncTheme())
  }
}
