// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.collection.immutable.ListMap

trait Dialect extends LowPriorityDialect {
  def is3D:           Boolean
  def agentVariables: AgentVariableSet
  def tokenMapper:    TokenMapperInterface
}

object Dialect extends LowPriorityDialect

trait LowPriorityDialect {
  implicit val dialect = NetLogoCore
}

case object NetLogoCore extends Dialect {
  val is3D           = false
  val agentVariables = AgentVariables
  val tokenMapper    = DefaultTokenMapper
}

trait AgentVariableSet {
  type SyntaxType = Int
  def implicitObserverVariableTypeMap: ListMap[String, SyntaxType]
  def implicitTurtleVariableTypeMap:   ListMap[String, SyntaxType]
  def implicitPatchVariableTypeMap:    ListMap[String, SyntaxType]
  def implicitLinkVariableTypeMap:     ListMap[String, SyntaxType]
}
