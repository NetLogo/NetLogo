// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core

case class Reference(kind: core.AgentKind, vn: Int, original: Instruction)
