// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Partial implementation of Command provides common implentations of some methods.  Implements
 * every method except <code>Command.perform(...)</code>.
 */
abstract class DefaultCommand extends Command with TokenHolder {

  /**
   * Indicates that this primitive can be used by any agent.
   *
   * @return <code>"OTPL"</code>
   */
  def getAgentClassString = getSyntax.agentClassString

  /**
   * Indicates that this command takes no arguments.
   *
   * @return <code>Syntax.commandSyntax()</code>
   */
  override def getSyntax = org.nlogo.core.Syntax.commandSyntax()

  /**
   * Indicates that NetLogo does not need to switch agents after executing this command.
   */
  override def getSwitchesBoolean = false

}
