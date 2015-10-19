// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait Dialect {
  def is3D:           Boolean
  def agentVariables: AgentVariableSet
  def tokenMapper:    TokenMapperInterface
}

case object NetLogoCore extends Dialect {
  val is3D           = false
  val agentVariables = AgentVariables
  val tokenMapper    = DefaultTokenMapper
}

trait AgentVariableSet {
  def getImplicitObserverVariables: Seq[String]
  def getImplicitTurtleVariables: Seq[String]
  def getImplicitPatchVariables: Seq[String]
  def getImplicitLinkVariables: Seq[String]
}
