// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.AgentException

abstract class Agent(val world: World)
extends api.Agent with Comparable[Agent] {

  private[agent] var _id = 0L
  def id = _id

  private[agent] var _variables: Array[AnyRef] = null
  def variables = _variables

  private[agent] def agentKey: AnyRef = Double.box(_id)

  // implement Comparable
  override def compareTo(a: Agent): Int =
    id compareTo a.id

  private[agent] def realloc(forRecompile: Boolean)

  def getVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def setVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def getTurtleVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getBreedVariable(name: String): AnyRef

  @throws(classOf[AgentException])
  def getLinkBreedVariable(name: String): AnyRef

  @throws(classOf[AgentException])
  def getLinkVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getPatchVariable(vn: Int): AnyRef

  @throws(classOf[AgentException])
  def getTurtleOrLinkVariable(varName: String): AnyRef

  @throws(classOf[AgentException])
  def setTurtleVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setTurtleVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setBreedVariable(name: String, value: AnyRef)

  @throws(classOf[AgentException])
  def setLinkBreedVariable(name: String, value: AnyRef)

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: AnyRef)

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: Double)

  @throws(classOf[AgentException])
  def setTurtleOrLinkVariable(varName: String, value: AnyRef)

  @throws(classOf[AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch

  def classDisplayName: String

  def agentBit: Int

  @throws(classOf[AgentException])
  private[agent] def wrongTypeForVariable(name: String, expectedClass: Class[_], value: AnyRef) {
    throw new AgentException(api.I18N.errors.getN("org.nlogo.agent.Agent.wrongTypeOnSetError",
        classDisplayName, name, api.Dump.typeName(expectedClass), api.Dump.logoObject(value)))
  }

}
