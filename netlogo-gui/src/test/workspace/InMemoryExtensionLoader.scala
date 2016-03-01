// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URL

import org.nlogo.api.ClassManager
import ExtensionManager._
import ExtensionManagerException._

class InMemoryExtensionLoader(prefix: String, classManager: ClassManager) extends ExtensionLoader {
  def locateExtension(extensionName: String): Option[URL] =
    if (extensionName == prefix)
      Some(new URL(s"file:/tmp/extension/$prefix"))
    else
      None

  def extensionData(extensionName: String, url: URL): ExtensionData = {
    val currentVer: String = org.nlogo.api.APIVersion.version
    if (extensionName == prefix && url.toString.endsWith(s"/$extensionName"))
      new ExtensionData(extensionName, url, extensionName, classManager.getClass.getCanonicalName, Some(currentVer), 0)
    else
      throw new ExtensionManagerException(NoManifest)
  }

  def extensionClassLoader(fileURL: URL, parent: ClassLoader): ClassLoader = parent

  def extensionClassManager(classLoader: ClassLoader, data: ExtensionData): ClassManager = classManager
}
