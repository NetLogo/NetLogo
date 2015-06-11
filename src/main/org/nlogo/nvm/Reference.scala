// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.Agent
import scala.language.existentials

case class Reference(agentClass: Class[_ <: Agent], vn: Int, original: Instruction)
