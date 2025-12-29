// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.util.Locale

import org.nlogo.api.{ AgentException, LogoException, Perspective }
import org.nlogo.log.LogManager

import World.Zero

trait ObserverManagement extends WorldKernel {
  val observer: Observer = createObserver()
  val observers: AgentSet = AgentSet.fromAgent(observer)

  protected def createObserver(): Observer

  def getVariablesArraySize(observer: Observer): Int = program.globals.size

  @throws(classOf[AgentException])
  @throws(classOf[LogoException])
  def setObserverVariableByName(varName: String, value: Object): Unit = {
    val index = observer.variableIndex(varName.toUpperCase(Locale.ENGLISH))
    if (index != -1) {
      val oldValue = observer.getVariable(index)
      observer.setVariable(index, value)
      LogManager.globalChanged(varName, value, oldValue)
    } else {
      throw new IllegalArgumentException(s""""${varName}" not found""")
    }
  }

  def getObserverVariableByName(varName: String): AnyRef = {
    val index = observer.variableIndex(varName.toUpperCase(Locale.ENGLISH))
    if (index >= 0)
      observer.variables(index)
    else
      throw new IllegalArgumentException(s""""${varName}" not found""")
  }

  def wrappedObserverX(x: Double): Double = {
    try {
      if (observer.perspective.isInstanceOf[Perspective.Follow] && !topology.xWraps) {
        topology.wrapX(x - topology.followOffsetX) - observer.oxcor
      } else {
        topology.wrapX(x - topology.followOffsetX)
      }
    } catch {
      case e: AgentException =>
        org.nlogo.api.Exceptions.ignore(e)
        x
    }
  }

  def wrappedObserverY(y: Double): Double = {
    try {
      if (observer.perspective.isInstanceOf[Perspective.Follow] && !topology.yWraps) {
        topology.wrapY(y - topology.followOffsetY) - observer.oycor
      } else {
        topology.wrapY(y - topology.followOffsetY)
      }
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
        val con = Option(observer.constraint(j))
        observer.setVariable(j, con.map(_.defaultValue).getOrElse(Zero))
      } catch {
        case ex: AgentException => throw new IllegalStateException(ex)
        case ex: LogoException  => throw new IllegalStateException(ex)
      }
      j += 1
    }
  }

  def clearObserverPosition(): Unit = {
    observer.updatePosition()
    observer.resetPerspective()
  }
}
