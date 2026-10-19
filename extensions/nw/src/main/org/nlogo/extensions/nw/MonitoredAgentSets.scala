// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw

import org.nlogo.agent.{ Agent, AgentSet, ArrayAgentSet, Link, TreeAgentSet, Turtle, World }
import org.nlogo.api.SimpleChangeEvent
import org.nlogo.core.Listener

class AgentSetChangeSubscriber(agentSet: TreeAgentSet, onNotify: () => Unit)
  extends Listener[SimpleChangeEvent.type] {
  agentSet.simpleChangeEventPublisher.subscribe(this)
  def unsubscribe(): Unit = agentSet.simpleChangeEventPublisher.unsubscribe(this)
  override def handle(e: SimpleChangeEvent.type): Unit = {
    onNotify.apply()
  }
}

trait MonitoredAgentSet[A <: Agent] {
  def hasChanged: Boolean
  val agentSet: AgentSet
}

trait MonitoredTreeAgentSet[A <: Agent] extends MonitoredAgentSet[A] {
  override val agentSet: TreeAgentSet
  def world : World
  val breedName = agentSet.printName
  var hasChanged = false
  protected val changeSubscriber = new AgentSetChangeSubscriber(agentSet, () => hasChanged = true)
  def unsubscribe(): Unit = changeSubscriber.unsubscribe()
}

class MonitoredTurtleTreeAgentSet(override val agentSet: TreeAgentSet, val world: World)
  extends MonitoredTreeAgentSet[Turtle]

class MonitoredLinkTreeAgentSet(override val agentSet: TreeAgentSet, val world: World)
  extends MonitoredTreeAgentSet[Link]

trait MonitoredArrayAgentSet[A <: Agent] extends MonitoredAgentSet[A] {
  val agentSet: ArrayAgentSet
  val count = agentSet.count

  def hasChanged: Boolean = count != agentSet.count
}

class MonitoredTurtleArrayAgentSet(override val agentSet: ArrayAgentSet)
  extends MonitoredArrayAgentSet[Turtle]

class MonitoredLinkArrayAgentSet(override val agentSet: ArrayAgentSet)
  extends MonitoredArrayAgentSet[Link]
