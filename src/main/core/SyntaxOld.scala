// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 *  Legacy convenience methods for constructing Syntax objects.
 *  Should be shrunk and eventually eliminated entirely.
 *  (SyntaxJ remains for use from Java.)
 */

import Syntax._

object SyntaxOld {
  def commandSyntax(right: Array[Int], agentClassString: String = "OTPL", switches: Boolean = false) =
    Syntax.commandSyntax(right = right.toList,
      agentClassString = agentClassString,
      switches = switches)
  def reporterSyntax(right: Array[Int], ret: Int, agentClassString: String = "OTPL") =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret,
      agentClassString = agentClassString)
}
