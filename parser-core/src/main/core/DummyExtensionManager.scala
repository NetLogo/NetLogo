// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// This isn't a singleton because in testing contexts it's sometimes useful
// to override a few methods. - ST 11/5/11

class DummyExtensionManager extends ExtensionManager {
  override def startFullCompilation() { }
  override def finishFullCompilation() { }
  override def anyExtensionsLoaded = false
  override def replaceIdentifier(name: String): Primitive = null
  override def importExtension(jarPath: String, errors: ErrorSource) = unsupported
  override def resolvePath(path: String): String = path
  private def unsupported = throw new UnsupportedOperationException
}
