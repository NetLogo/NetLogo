// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.PrimitiveCommand

/**
 * Interface for NetLogo extension commands. Commands are primitives that
 * do not return a value.  All new commands must implement this interface.
 */
trait Command extends PrimitiveCommand {

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
  def perform(args: Array[Argument], context: Context): Unit

}
