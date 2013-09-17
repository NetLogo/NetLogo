package org.nlogo.tortoise.api
package wrapper

import
  scala.js.annotation.expose

import
  org.nlogo.tortoise.{ adt, engine },
    adt.{ AnyJS, JSW },
    engine.{ Agent, Patch, Turtle }

trait AgentWrapper extends Wrapper {
  override type ValueType <: Agent
  @expose override def toString: String = value.toString
}

class TurtleWrapper(override val value: Turtle) extends AgentWrapper {
  override type ValueType = Turtle
  @expose def getTurtleVariable(n: Int):         AnyJS = value.getTurtleVariable(n)
  @expose def setTurtleVariable(n: Int, v: JSW): Unit  = value.setTurtleVariable(n, v)
  @expose def getPatchVariable(n: Int):          AnyJS = value.getPatchVariable(n)
  @expose def setPatchVariable(n: Int, v: JSW):  Unit  = value.setPatchVariable(n, v)
}

class PatchWrapper(override val value: Patch) extends AgentWrapper {
  override type ValueType = Patch
  @expose def getPatchVariable(n: Int):          AnyJS = value.getPatchVariable(n)
  @expose def setPatchVariable(n: Int, v: JSW):  Unit  = value.setPatchVariable(n, v)
}
