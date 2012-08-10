// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// This isn't a singleton because in testing contexts it's sometimes useful
// to override a few methods. - ST 11/5/11

class DummyExtensionManager extends ExtensionManager {
  private def unsupported = throw new UnsupportedOperationException
  def storeObject(obj: AnyRef) { }
  def retrieveObject(): AnyRef = unsupported
  def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject = unsupported
  def readFromString(src: String): AnyRef = unsupported
  def reset() = unsupported
  def startFullCompilation() { }
  def finishFullCompilation() { }
  def anyExtensionsLoaded() = false
  def replaceIdentifier(name: String): Primitive = null
  def importExtension(jarPath: String, errors: ErrorSource) = unsupported
  def resolvePath(path: String): String = unsupported
  def resolvePathAsURL(path: String): String = unsupported
  def dumpExtensions(): String = unsupported
  def dumpExtensionPrimitives(): String = unsupported
  def getSource(filename: String): String = unsupported
  def wrap(prim: Primitive, name: String): TokenHolder = unsupported
  def addToLibraryPath(classManager: AnyRef, directory: String) = unsupported
  def getFile(path: String) = unsupported
  def getJarPaths = unsupported
  def getExtensionNames = unsupported
  def profilingEnabled = false
}
