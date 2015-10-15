// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ AgentVariableSet, Dialect, DefaultTokenMapper }

object ThreeDProgram extends Dialect {
  val is3D = true;
  val agentVariables = new AgentVariableSet {
    val getImplicitObserverVariables: Seq[String] = Seq()
    val getImplicitTurtleVariables: Seq[String]   = AgentVariables.getImplicitTurtleVariables(true)
    val getImplicitPatchVariables: Seq[String]    = AgentVariables.getImplicitPatchVariables(true)
    val getImplicitLinkVariables: Seq[String]     = AgentVariables.getImplicitLinkVariables
  }
  val tokenMapper = DefaultTokenMapper // this is to allow compilation -- not correct
}
