// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api

case class Reference(kind: api.AgentKind, vn: Int, original: Instruction)
