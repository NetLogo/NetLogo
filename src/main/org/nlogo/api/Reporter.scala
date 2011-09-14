package org.nlogo.api

/**
 * Interface for NetLogo reporters. Reporters are primitives that
 * return a value.  All new reporters must implement this interface.
 * <p>Note that NetLogo will not call your contructor directly, it will
 * call <code>newInstance(String)</code> instead.
 */
trait Reporter extends Primitive {

  /**
   * Returns a new instance of this <code>Reporter</code>.  Called by NetLogo
   * every time this <code>Reporter</code> is encountered during compilation.
   *
   * @param name the name that was found in the code (without the JAR identifer)
   * @return the <code>Reporter</code> to be called during runtime
   */
  def newInstance(name: String): Reporter

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
  @throws(classOf[LogoException])
  def report(args: Array[Argument], context: Context): AnyRef

}
