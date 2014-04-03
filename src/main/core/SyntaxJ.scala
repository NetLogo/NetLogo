// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 *  Legacy convenience methods for constructing Syntax objects,
 *  for use by extensions written in Java.
 */

import Syntax._

object SyntaxJ {

  /** Returns a Syntax object for commands with no arguments. */
  def commandSyntax() =
    Syntax.commandSyntax()

  /**
   * Returns a <code>Syntax</code> for commands with one or more right arguments.
   *
   * @param right an array of Type flags that are to be to the right of the Primitive
   */
  def commandSyntax(right: Array[Int]) =
    Syntax.commandSyntax(right = right.toList)

  /**
   * Returns a <code>Syntax</code> for commands with a variable number of arguments.
   *
   * @param right  an array of Type flags that are to be to the right of the primitive
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def commandSyntax(right: Array[Int], dfault: Int) =
    Syntax.commandSyntax(right = right.toList,
      defaultOption = Some(dfault))

  def commandSyntax(switches: Boolean) =
    Syntax.commandSyntax(switches = switches)

  def commandSyntax(agentClassString: String, switches: Boolean) =
    Syntax.commandSyntax(agentClassString = agentClassString,
      switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], switches: Boolean) =
    Syntax.commandSyntax(right = right.toList,
      switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String) =
    Syntax.commandSyntax(right = right.toList,
      agentClassString = agentClassString)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String, switches: Boolean) =
    Syntax.commandSyntax(right = right.toList,
      agentClassString = agentClassString,
      switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], agentClassString: String, blockAgentClassString: String, switches: Boolean) =
    Syntax.commandSyntax(right = right.toList,
      agentClassString = agentClassString,
      blockAgentClassString = blockAgentClassString,
      switches = switches)

  // for use by commands
  def commandSyntax(right: Array[Int], dfault: Int, agentClassString: String, blockAgentClassString: String, switches: Boolean) =
    Syntax.commandSyntax(right = right.toList,
      defaultOption = Some(dfault),
      agentClassString = agentClassString,
      blockAgentClassString = blockAgentClassString,
      switches = switches)

  // for use by constants and no-argument reporters
  def reporterSyntax(ret: Int, agentClassString: String) =
    Syntax.reporterSyntax(ret = ret,
      agentClassString = agentClassString)

  // for use by infix reporters
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int, isRightAssociative: Boolean) =
    Syntax.reporterSyntax(left = left,
      right = right.toList,
      ret = ret,
      precedence = precedence,
      isRightAssociative = isRightAssociative)

  // for use by prefix reporters
  def reporterSyntax(right: Array[Int], ret: Int, agentClassString: String, blockAgentClassString: String) =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret,
      agentClassString = agentClassString,
      blockAgentClassString = blockAgentClassString)

  // for use by prefix reporters
  def reporterSyntax(right: Array[Int], ret: Int, agentClassString: String) =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret,
      agentClassString = agentClassString)

  // for use by variadic reporters when min is different than default
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int, minimum: Int) =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret,
      defaultOption = Some(dfault),
      minimumOption = Some(minimum))

  // for use by reporters that take a reporter block
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int, isRightAssociative: Boolean,
                     agentClassString: String, blockAgentClassString: String) =
    Syntax.reporterSyntax(left = left,
      right = right.toList,
      ret = ret,
      precedence = precedence,
      isRightAssociative = isRightAssociative,
      agentClassString = agentClassString,
      blockAgentClassString = blockAgentClassString)

  /**
   * Returns a <code>Syntax</code> for reporters with no arguments
   *
   * @param ret the return type
   */
  def reporterSyntax(ret: Int) =
    Syntax.reporterSyntax(ret = ret)

  /**
   * Returns a <code>Syntax</code> for reporters with infix arguments.
   *
   * @param left
   * @param right
   * @param ret        the return type
   * @param precedence
   */
  def reporterSyntax(left: Int, right: Array[Int], ret: Int, precedence: Int) =
    Syntax.reporterSyntax(left = left,
      right = right.toList,
      ret = ret,
      precedence = precedence)

  /**
   * Returns a <code>Syntax</code> for reporters with one or more right arguments
   *
   * @param right an array of Type flags that are to the be right of the Primitive
   * @param ret   the return type
   */
  def reporterSyntax(right: Array[Int], ret: Int) =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret)

  /**
   * Returns a <code>Syntax</code> for reporters with a variable number of
   * arguments.
   *
   * @param right  an array of Type flags that are to the be right of the primitive
   * @param ret    the return type
   * @param dfault the default number of arguments if no parenthesis are used.
   */
  def reporterSyntax(right: Array[Int], ret: Int, dfault: Int) =
    Syntax.reporterSyntax(right = right.toList,
      ret = ret,
      defaultOption = Some(dfault))

}
