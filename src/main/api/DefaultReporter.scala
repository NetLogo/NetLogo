// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Syntax

/**
 * Partial implementation of Reporter provides common implementations of some methods.  Implements
 * every method except <code>report(...)</code>.
 */
abstract class DefaultReporter extends Reporter {

  /**
   * Indicates that this primitive can be used by any agent.
   *
   * @return <code>"OTPL"</code>
   */
  override def getAgentClassString = "OTPL"

  /**
   * Indicates that this reporter takes no arguments and returns any type.
   *
   * @return <code>Syntax.reporterSyntax(Syntax.WildcardType)</code>
   */
  override def getSyntax = Syntax.reporterSyntax(ret = Syntax.WildcardType)
}
