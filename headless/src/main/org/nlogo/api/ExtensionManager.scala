// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ List => JList }

/**
 * <code>ExtensionManager</code> provides some methods that can be used in runOnce
 */
trait ExtensionManager {

  /**
   * Stores an object for the extension that can be retrieved in runOnce if the extension is loaded
   * again.  This is useful if the extension has initialization it needs to perform that should only
   * happen once, even if the extension is loaded multiple times.
   *
   * @param obj the object to be stored
   */
  def storeObject(obj: AnyRef)

  /** @return the stored object */
  def retrieveObject: AnyRef

  @throws(classOf[CompilerException])
  def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject

  @throws(classOf[CompilerException])
  def readFromString(src: String): AnyRef

  /**
   * Instructs any loaded extensions to unload. Should be called previous
   * to a new model load.
   */
  def reset()

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
   * See the top of {@link org.nlogo.workspace.ExtensionManager} for full details.
   */
  def startFullCompilation()

  /**
   * Instructs any extensions which haven't been re-imported during the
   * current compile to shut down. Should be called during each full
   * re-compile.
   *
   * See the top of {@link org.nlogo.workspace.ExtensionManager} for full details.
   */
  def finishFullCompilation()

  /** Returns true if any extensions have been imported in the current model. */
  def anyExtensionsLoaded: Boolean

  /** uses java.lang.Iterable for easy access from Java */
  def loadedExtensions: java.lang.Iterable[ClassManager]

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
  @throws(classOf[CompilerException])
  def importExtension(jarPath: String, errors: ErrorSource)

  def resolvePath(path: String): String

  def resolvePathAsURL(path: String): String

  def dumpExtensions: String

  def dumpExtensionPrimitives(): String

  @throws(classOf[java.io.IOException])
  def getSource(filename: String): String

  def addToLibraryPath(classManager: AnyRef, directory: String)

  @throws(classOf[ExtensionException])
  def getFile(path: String): File

  def getJarPaths: JList[String]

  def getExtensionNames: JList[String]

  def profilingEnabled: Boolean

}
