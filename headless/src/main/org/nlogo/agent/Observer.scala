// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api

class Observer(_world: World) extends Agent(_world)
with api.Observer with Constraints {

  def kind = api.AgentKind.Observer

  override def getVariable(vn: Int) = variables(vn)

  @throws(classOf[api.AgentException])
  override def setVariable(vn: Int, value: AnyRef) {
    assertConstraint(vn, value)
    variables(vn) = value
  }

  override def realloc(forRecompile: Boolean) {
    val oldvars = variables
    val size = world.program.globals.size
    val newvars = Array.fill[AnyRef](size)(World.ZERO)
    if (oldvars != null && forRecompile)
      for (i <- 0 until (oldvars.length min world.oldProgram.globals.size)) {
        val name = world.oldProgram.globals(i)
        val newpos = world.observerOwnsIndexOf(name)
        if (newpos != -1)
          newvars(newpos) = oldvars(i)
      }
    _variables = newvars
    clearConstraints()
  }

  private var _perspective: api.Perspective = api.Perspective.Observe
  override def perspective = _perspective
  def perspective_=(p: api.Perspective) { _perspective = p }

  var targetAgent: api.Agent = null

  var oxcor: Double = 0
  var oycor: Double = 0

  def followOffsetX: Double = perspective match {
    case api.Perspective.Follow | api.Perspective.Ride =>
      oxcor - (world.minPxcor - 0.5 + world.worldWidth / 2.0)
    case _ =>
      0.0
    }

  def followOffsetY: Double = perspective match {
    case api.Perspective.Follow | api.Perspective.Ride =>
      oycor - (world.minPycor - 0.5 + world.worldHeight / 2.0)
    case _ =>
      0.0
    }

  def setPerspective(perspective: api.Perspective, agent: api.Agent) {
    _perspective = perspective
    targetAgent = agent
  }

  def setPerspective(perspective: api.Perspective) {
    _perspective = perspective
  }

  def resetPerspective() {
    setPerspective(api.Perspective.Observe, null)
  }

  @throws(classOf[api.AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch =
    world.getPatchAt(dx, dy)

  override def toString = "observer"
  override def classDisplayName = "observer"

  val agentBit = AgentBit(api.AgentKind.Observer)

  override def alpha = 0

  // how many observers can dance on the head of a pin?
  override def size = 0

  @throws(classOf[api.AgentException])
  override def getTurtleVariable(vn: Int) =
    throw new api.AgentException(
      "the observer can't access a turtle variable without specifying which turtle")

  @throws(classOf[api.AgentException])
  override def getTurtleOrLinkVariable(varName: String) =
    throw new api.AgentException(
      "the observer can't access a turtle or link variable without specifying which agent")

  @throws(classOf[api.AgentException])
  override def getBreedVariable(name: String) =
    throw new api.AgentException(
      "the observer can't access a turtle variable without specifying which turtle")

  @throws(classOf[api.AgentException])
  override def getLinkVariable(vn: Int) =
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")

  @throws(classOf[api.AgentException])
  override def getLinkBreedVariable(name: String) =
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")

  @throws(classOf[api.AgentException])
  override def getPatchVariable(vn: Int) =
    throw new api.AgentException(
      "the observer can't access a patch variable without specifying which patch")

  @throws(classOf[api.AgentException])
  override def setTurtleVariable(vn: Int, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setTurtleVariable(vn: Int, value: Double) {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setBreedVariable(name: String, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setPatchVariable(vn: Int, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't set a patch variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setPatchVariable(vn: Int, value: Double) {
    throw new api.AgentException(
      "the observer can't set a patch variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setLinkVariable(vn: Int, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setLinkVariable(vn: Int, value: Double) {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setLinkBreedVariable(name: String, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setTurtleOrLinkVariable(varName: String, value: AnyRef) {
    throw new api.AgentException(
      "the observer can't access a turtle or link variable without specifying which agent")
  }

}
