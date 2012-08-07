// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Top-level interface for primitives (commands or reporters).  Not to be implemented directly; the
 * <code>Command</code> or <code>Reporter</code> interface should be used instead.
 *
 * @see Command
 * @see Reporter
 */

trait Primitive {

  /**
   * Returns a String which specifies which agents can run this primitive.  To specify
   * observer use "O", to specify Turtle use "T", to specify Patch use "P", to specify
   * link use "L".  To use combinations, put them togther.
   *
   * Example of a primitive allowed for all agents:
   * <code>String getAgentClassString() { return "OTPL"; }</code>
   *
   * Example of a primitive allowed only for turtles:
   * <code> String getAgentClassString() { return "T"; }</code>
   *
   * @return a String specifying the acceptable agent types.
   */
  def getAgentClassString: String

  /**
   * Returns Syntax which specifies the syntax that is acceptable for this primitive.  Used by the
   * compiler for type-checking.
   *
   * @return the Syntax for the primitive.
   * @see Syntax
   */
  def getSyntax: Syntax

}
