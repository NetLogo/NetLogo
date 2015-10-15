// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.ExtensionObject
import java.util.{ List => JList }

import org.nlogo.core.{ ExtensionObject, ExtensionManager => CoreManager, File}

/**
 * <code>ExtensionManager</code> provides some methods that can be used in runOnce
 */
trait ExtensionManager extends CoreManager {

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

  def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject

  def readFromString(src: String): AnyRef

  /**
   * Instructs any loaded extensions to unload. Should be called previous
   * to a new model load.
   */
  def reset()

  /** uses java.lang.Iterable for easy access from Java */
  def loadedExtensions: java.lang.Iterable[ClassManager]

  def resolvePathAsURL(path: String): String

  def dumpExtensions: String

  def dumpExtensionPrimitives(): String

  def addToLibraryPath(classManager: AnyRef, directory: String)

  @throws(classOf[ExtensionException])
  def getFile(path: String): File

  def getJarPaths: JList[String]

  def getExtensionNames: JList[String]

  def profilingEnabled: Boolean

}
