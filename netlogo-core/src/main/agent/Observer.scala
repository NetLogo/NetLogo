// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ api, core },
  core.Program,
  api.{ AgentFollowingPerspective, Perspective }

class Observer(_world: World) extends Agent(_world)
with api.Observer with OrientatableObserver with Constraints {

  def kind = core.AgentKind.Observer

  def shape = ""

  override def getVariable(vn: Int) = variables(vn)

  private var varNames: Array[String] = new Array[String](0)

  override def variableName(vn: Int) = varNames(vn)

  def variableIndex(name: String): Int = varNames.indexOf(name)

  @throws(classOf[api.AgentException])
  override def setVariable(vn: Int, value: AnyRef): Unit = {
    assertConstraint(vn, value)
    variables(vn) = value
    world.notifyWatchers(this, vn, value)
  }

  override def realloc(oldProgram: Program, newProgram: Program): Agent = {
    val forRecompile = oldProgram != null
    val oldvars = variables
    val size = newProgram.globals.size
    val newvars = Array.fill[AnyRef](size)(World.Zero)
    if (oldvars != null && forRecompile)
      for (i <- 0 until (oldvars.length min oldProgram.globals.size)) {
        val name = oldProgram.globals(i)
        val newpos = newProgram.globals.indexOf(name)
        if (newpos != -1)
          newvars(newpos) = oldvars(i)
      }
    _variables = newvars
    varNames = newProgram.globals.toArray[String]
    clearConstraints()
    null
  }

  def targetAgent: api.Agent =
    perspective match {
      case Perspective.Observe          => null
      case a: AgentFollowingPerspective => a.targetAgent
      case Perspective.Watch(a)         => a
    }

  def followDistance: Int =
    perspective match {
      case a: AgentFollowingPerspective => a.followDistance
      case _                            => 5
    }

  def followOffsetX: Double = perspective match {
    case _: AgentFollowingPerspective =>
      oxcor - (world.minPxcor - 0.5 + world.worldWidth / 2.0)
    case _ =>
      0.0
    }

  def followOffsetY: Double = perspective match {
    case _: AgentFollowingPerspective =>
      oycor - (world.minPycor - 0.5 + world.worldHeight / 2.0)
    case _ =>
      0.0
    }

  @throws(classOf[api.AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch =
    world.getPatchAt(dx, dy)

  override def toString = "observer"
  override def classDisplayName = "observer"

  val agentBit = AgentBit(core.AgentKind.Observer)

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
  override def setTurtleVariable(vn: Int, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setTurtleVariable(vn: Int, value: Double): Unit = {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setBreedVariable(name: String, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't set a turtle variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setPatchVariable(vn: Int, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't set a patch variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setPatchVariable(vn: Int, value: Double): Unit = {
    throw new api.AgentException(
      "the observer can't set a patch variable without specifying which turtle")
  }

  @throws(classOf[api.AgentException])
  override def setLinkVariable(vn: Int, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setLinkVariable(vn: Int, value: Double): Unit = {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setLinkBreedVariable(name: String, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't access a link variable without specifying which link")
  }

  @throws(classOf[api.AgentException])
  override def setTurtleOrLinkVariable(varName: String, value: AnyRef): Unit = {
    throw new api.AgentException(
      "the observer can't access a turtle or link variable without specifying which agent")
  }
}
