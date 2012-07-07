// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * An abstract, partial implementation of ClassManager that implements
 * <code>runOnce()</code> and <code>unload()</code> with empty methods.
 * @see ClassManager
 */

import java.util.{ List => JList }

abstract class DefaultClassManager extends ClassManager {

  /** Empty implementation. */
  @throws(classOf[ExtensionException])
  override def runOnce(em: ExtensionManager) { }

  /**
   * Loads the primitives in the extension. This is called once per model compilation.
   *
   * @param primManager The manager to transport the primitives to NetLogo
   */
  @throws(classOf[ExtensionException])
  override def load(primManager: PrimitiveManager)

  /** Empty implementation. */
  @throws(classOf[ExtensionException])
  override def unload(em: ExtensionManager) { }

  /** Default exports nothing, returns empty builder. */
  override def exportWorld: java.lang.StringBuilder =
    new java.lang.StringBuilder

  /** Default loads nothing. */
  @throws(classOf[ExtensionException])
  override def importWorld(lines: JList[Array[String]], reader: ExtensionManager, handler: ImportErrorHandler) { }

  /** Default does nothing. */
  override def clearAll() { }

  /** Default defines no extension objects, thus, we cannot read any extension objects. */
  @throws(classOf[ExtensionException])
  @throws(classOf[CompilerException])
  override def readExtensionObject(em: ExtensionManager, typeName: String, value: String): ExtensionObject =
    throw new IllegalStateException("readExtensionObject not implemented for " + this)

  override def additionalJars: JList[String] =
    new java.util.ArrayList[String]

}
