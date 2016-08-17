// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait ExtensionManager {

  /**
   * During compilation, we reach the extensions [ ... ] block.
   * When that happens, the compiler tells the ExtensionManager that it needs to
   * forget what extensions are in the extensions [ ... ] block, by calling this method.
   *
   * The compiler will then call the importExtension method for each extension in the block.
   * Among other things, this lets the ExtensionManager know each extension that is
   * "live", or currently in the block, so that its primitives are available for use
   * elsewhere in the model.
   *
   * See the top of org.nlogo.workspace.ExtensionManager for full details.
   */
  def startFullCompilation(): Unit

  /**
   * Instructs any extensions which haven't been re-imported during the
   * current compile to shut down. Should be called during each full
   * re-compile.
   *
   * See the top of org.nlogo.workspace.ExtensionManager for full details.
   */
  def finishFullCompilation(): Unit

  /** Returns true if any extensions have been imported in the current model. */
  def anyExtensionsLoaded: Boolean

  /** Returns the identifier "name" by its imported implementation, if any, or null if not. */
  def replaceIdentifier(name: String): Primitive

  /**
   * Loads the extension contained in the jar at jarPath.
   *
   * @param jarPath the path to the extension jar. May be relative to the
   *                current model directory.
   * @param errors  the ErrorSource to use when a CompilerException needs
   *                to be thrown.
   */
  def importExtension(jarPath: String, errors: ErrorSource): Unit

  def readExtensionObject(extensionName: String, typeName: String, value: String): ExtensionObject
}
