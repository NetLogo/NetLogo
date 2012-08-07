// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api;

/**
 * Interface specifies the main class of a NetLogo extension.  All NetLogo extensions must include a
 * class that implements this interface.  Registers primitives with NetLogo and handles extension
 * initialization and deconstruction.
 *
 * For example:
 * <pre>
 * public class FibonacciExtension extends org.nlogo.api.DefaultClassManager
 * {
 *     public void load(org.nlogo.api.PrimitiveManager primManager)
 *     {
 *         primManager.addPrimitive("first-n-fibs", new Fibonacci());
 *     }
 * }
 * </pre>
 */

import java.util.{ List => JList }

trait ClassManager {

  /** Initializes the extension. This is called once per NetLogo instance.
   * (In the NetLogo GUI, it is called on the AWT event thread.
   */
  @throws(classOf[ExtensionException])
  def runOnce(em: ExtensionManager)

  /**
   * Loads the primitives in the extension. This is called each time a model that uses this
   * extension is compiled.
   *
   * @param primManager The manager to transport the primitives to NetLogo
   */
  @throws(classOf[ExtensionException])
  def load(primManager: PrimitiveManager)

  /**
   * Cleans up the extension. This is called once before <code>load</code> is called and once
   * before NetLogo is closed or another model is opened.
   */
  @throws(classOf[ExtensionException])
  def unload(em: ExtensionManager)

  /**
   * Return a new NetLogo ExtensionObject
   *
   * @param reader   An interface that allows the extension to read NetLogo objects
   * @param typeName The type of ExtensionObject to be returned
   * @param value    The string representation of the object
   */
  @throws(classOf[ExtensionException])
  @throws(classOf[CompilerException])
  def readExtensionObject(reader: ExtensionManager, typeName: String, value: String): ExtensionObject

  /**
   * Write any state needed to restore the world.
   *
   * @return StringBuilder containing all the data to export. If the StringBuilder is empty no section is written.
   */
  def exportWorld: java.lang.StringBuilder

  /**
   * Reload any state saved in an export world file
   *
   * @param lines   A list of lines exported by this extension the lines are broken up into an array delimited by commas
   * @param reader  An interface that allows the extension to read NetLogo objects
   * @param handler An interface that allows the extensions to report non-fatal errors during the import
   */
  @throws(classOf[ExtensionException])
  def importWorld(lines: JList[Array[String]], reader: ExtensionManager, handler: ImportErrorHandler)

  /**
   * Clear any stored state
   */
  def clearAll()

  def additionalJars: JList[String]

}
