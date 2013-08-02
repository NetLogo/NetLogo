// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface provides access to arguments passed to the <code>perform</code> or <code>report</code>
 * methods of a primitive at run-time.
 *
 * <code>Arguments</code> are created by NetLogo and passed to the <code>perform</code> or
 * <code>report</code> methods of your primitive.
 *
 * @see Command#perform(Argument[], Context)
 * @see Reporter#report(Argument[], Context)
 */
trait Argument {

  /**
   * Returns the argument as an <code>Object</code> without type checking.
   *
   * @throws ExtensionException
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def get: AnyRef

  /**
   * Returns the argument as an <code>org.nlogo.api.AgentSet</code>.
   *
   * @throws ExtensionException if the argument is not an <code>AgentSet</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getAgentSet: AgentSet

  /**
   * Returns the argument as an <code>Agent</code>.
   *
   * @throws ExtensionException if the argument is not an <code>Agent</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getAgent: Agent

  /**
   * Returns the argument as a boxed <code>java.lang.Boolean</code>
   *
   * @throws ExtensionException if the argument is not a <code>Boolean</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getBoolean: java.lang.Boolean

  /**
   * Returns the value of the argument as an unboxed <code>boolean</code>.
   *
   * @throws ExtensionException if the argument is not a <code>boolean</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getBooleanValue: Boolean

  /**
   * Returns the value of the argument as an unboxed <code>int</code>.
   * Any fractional part is discarded.
   *
   * @throws ExtensionException if the argument is not a number.
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getIntValue: Int

  /**
   * Returns the value of the argument as an unboxed <code>double</code>.
   *
   * @throws ExtensionException if the argument is not a number.
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getDoubleValue: Double

  /**
   * Returns the argument as a <code>org.nlogo.api.LogoList</code>
   *
   * @throws ExtensionException if the argument is not a <code>LogoList</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getList: LogoList

  /**
   * Returns the argument as an <code>org.nlogo.api.Patch</code>
   *
   * @throws ExtensionException if the argument is not a <code>Patch</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getPatch: Patch

  /**
   * Returns the argument as a <code>String</code>
   *
   * @throws ExtensionException if the argument cannot be cast to a <code>String</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getString: String

  /**
   * Returns the argument as a <code>org.nlogo.api.Turtle</code>.
   *
   * @throws ExtensionException if the argument is not a <code>Turtle</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getTurtle: Turtle

  /**
   * Returns the argument as a <code>org.nlogo.api.Link</code>.
   *
   * @throws ExtensionException if the argument is not a <code>Link</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getLink: Link

  /**
   * Returns the argument as a <code>org.nlogo.api.ReporterTask</code>.
   *
   * @throws ExtensionException if the argument is not a <code>ReporterTask</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getReporterTask: ReporterTask

  /**
   * Returns the argument as a <code>org.nlogo.api.CommandTask</code>.
   *
   * @throws ExtensionException if the argument is not a <code>CommandTask</code>
   * @throws LogoException      if a LogoException occurred while evaluating this argument
   */
  @throws(classOf[ExtensionException])
  def getCommandTask: CommandTask

}
