// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface for NetLogo extension commands. Commands are primitives that
 * do not return a value.  All new commands must implement this interface.
 */
trait Command extends Primitive {

  /**
   * Specifies whether NetLogo should "switch" to another turtle after running this command.
   * Generally, this should be true if this <code>Command</code> affects the state of the world
   * (changing a variable, changing graphics, etc.)
   *
   * @return <code> true </code> if a switch should occur, <code> false </code> otherwise.
   */
  def getSwitchesBoolean: Boolean

  /**
   * Executes this <code>Command</code>. Called by NetLogo when this <code>Command</code> is called at run-time.
   *
   * @param args    the <code>Argument</code>s that were passed to
   *                the command.  (May be a <code>Reporter</code> or a constant.)
   *                To evaluate arguments, use the typesafe methods in the <code>Argument</code> interface.
   * @param context the current <code>Context</code> allows access to NetLogo internal methods
   * @throws ExtensionException (if an extension-related problem occurs)
   * @throws LogoException      (if one of the evaluated arguments throws a LogoException)
   */
  @throws(classOf[ExtensionException])
  def perform(args: Array[Argument], context: Context)

}
