// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.ExtensionObject
import org.nlogo.core.{ DummyExtensionManager => CoreDummyExtensionManager }

// This isn't a singleton because in testing contexts it's sometimes useful
// to override a few methods. - ST 11/5/11

class DummyExtensionManager extends CoreDummyExtensionManager with ExtensionManager {
  override def storeObject(obj: AnyRef) { }
  override def retrieveObject: AnyRef = unsupported
  override def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject = unsupported
  override def readFromString(src: String): AnyRef = unsupported
  override def activeModel = unsupported
  override def reset() = unsupported
  override def loadedExtensions = java.util.Collections.emptyList[ClassManager]
  override def dumpExtensions: String = unsupported
  override def dumpExtensionPrimitives(): String = unsupported
  def extensionCommandNames: Set[String] = Set.empty[String]
  def extensionReporterNames: Set[String] = Set.empty[String]
  def addInstrumentation[A](name: String, instrument: A, klass: Class[A]): Unit = unsupported
  def removeInstrumentation[A](name: String, klass: Class[A]): Unit = unsupported
  private def unsupported = throw new UnsupportedOperationException
}
