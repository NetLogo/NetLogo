// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.AgentKind

sealed trait ModelOperation

case class UpdateVariable(name: String, agentKind: AgentKind, who: Int, expectedValue: AnyRef, updateValue: AnyRef)
  extends ModelOperation
