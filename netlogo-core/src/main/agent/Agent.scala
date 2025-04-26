// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  org.nlogo.{ core, api },
    core.{ I18N, Program },
    api.{ AgentException, Agent => ApiAgent }

import scala.collection.immutable.Set

abstract class Agent(world: World) extends AgentJ(world) with ApiAgent with Comparable[Agent] {
  def world = _world

  // We have setter weirdness for "id" and "variables" primarily because these
  // methods are called frequently in java classes (the agent subclasses).
  def id = _id
  def id_=(newId: Long) = {
    _id = newId
  }
  def setId(newId: Long) = {
    _id = newId
  }

  def variables = _variables
  def variables_=(v: Array[AnyRef]) = {
    _variables = v
  }
  def setVariables(v: Array[AnyRef]) = {
    _variables = v
  }

  private[agent] def agentKey: AnyRef = Double.box(_id.toDouble)

  override def compareTo(a: Agent): Int =
    id compareTo a.id

  @throws(classOf[AgentException])
  private[agent] def realloc(oldProgram: Program, newProgram: Program): Agent

  def getVariable(vn: Int): AnyRef

  /**
   * Returns the name of the variable with the given index. Works for built-in, *-own, and breed variables.
   * @param vn the index of the variable
   */
  def variableName(vn: Int): String

  @throws(classOf[AgentException])
  def setVariable(vn: Int, value: AnyRef): Unit

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
  def setTurtleVariable(vn: Int, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def setTurtleVariable(vn: Int, value: Double): Unit

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def setLinkVariable(vn: Int, value: Double): Unit

  @throws(classOf[AgentException])
  def setBreedVariable(name: String, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def setLinkBreedVariable(name: String, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def setPatchVariable(vn: Int, value: Double): Unit

  @throws(classOf[AgentException])
  def setTurtleOrLinkVariable(varName: String, value: AnyRef): Unit

  @throws(classOf[AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch

  def classDisplayName: String

  def agentBit: Int

  @throws(classOf[AgentException])
  private[agent] def wrongTypeForVariable(name: String, expectedClass: Class[_], value: AnyRef): Unit = {
    throw new AgentException(I18N.errors.getN("org.nlogo.agent.Agent.wrongTypeOnSetError",
        classDisplayName, name, api.Dump.typeName(expectedClass), api.Dump.logoObject(value)))
  }
}

object Agent {
  // this method needs to be in scala for some jankiness in agent.Turtle
  private[agent] def turtleSet(t: Turtle): Set[Turtle] = Set[Turtle](t)
}
