// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface for NetLogo reporters. Reporters are primitives that
 * return a value.  All new reporters must implement this interface.
 */
trait Reporter extends Primitive {

  /**
   * Executes this <code>Reporter</code>. Called by NetLogo when this <code>Reporter</code> is
   * called in a running NetLogo model.
   *
   * @param args    The <code>Argument</code>s that were included with
   *                the command in the NetLogo code.  (May be a <code>Reporter</code> or a constant.)
   * @param context The current <code>Context</code> allows access to NetLogo internal methods.
   * @return the object to be reported
   * @throws ExtensionException
   */
  @throws(classOf[ExtensionException])
  def report(args: Array[Argument], context: Context): AnyRef

}
