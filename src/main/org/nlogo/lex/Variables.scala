// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api.AgentVariables

object Variables {
  val variables = Set() ++
    AgentVariables.getImplicitObserverVariables ++
    AgentVariables.getImplicitTurtleVariables ++
    AgentVariables.getImplicitPatchVariables ++
    AgentVariables.getImplicitLinkVariables
  def isVariable(s: String) =
    variables.contains(s.toUpperCase)
}
