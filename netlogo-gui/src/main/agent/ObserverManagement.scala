// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, LogoException }

import World.Zero

trait ObserverManagement extends WorldKernel {
  val observer: Observer = createObserver()
  val observers: AgentSet = AgentSet.fromAgent(observer)

  protected def createObserver(): Observer

  def getVariablesArraySize(observer: Observer): Int = program.globals.size

  @throws(classOf[AgentException])
  @throws(classOf[LogoException])
  def setObserverVariableByName(varName: String, value: Object): Unit = {
    val index = observer.variableIndex(varName.toUpperCase)
    if (index != -1)
      observer.setObserverVariable(index, value)
    else
      throw new IllegalArgumentException(s""""${varName}" not found""")
  }

  def getObserverVariableByName(varName: String): AnyRef = {
    val index = observer.variableIndex(varName.toUpperCase)
    if (index >= 0)
      observer.variables(index)
    else
      throw new IllegalArgumentException(s""""${varName}" not found""")
  }

  def wrappedObserverX(x: Double): Double = {
    try {
      topology.wrapX(x - topology.followOffsetX)
    } catch {
      case e: AgentException =>
        org.nlogo.api.Exceptions.ignore(e)
        x
    }
  }

  def wrappedObserverY(y: Double): Double = {
    try {
      topology.wrapY(y - topology.followOffsetY)
    } catch {
      case e: AgentException =>
        org.nlogo.api.Exceptions.ignore(e);
        y
    }
  }

  def followOffsetX: Double = observer.followOffsetX
  def followOffsetY: Double = observer.followOffsetY


  def clearGlobals(): Unit = {
    var j = program.interfaceGlobals.size
    while (j < observer.variables.length) {
      try {
        val con = Option(observer.variableConstraint(j))
        observer.setObserverVariable(j, con.map(_.defaultValue).getOrElse(Zero))
      } catch {
        case ex: AgentException => throw new IllegalStateException(ex)
        case ex: LogoException  => throw new IllegalStateException(ex)
      }
      j += 1
    }
  }
}
