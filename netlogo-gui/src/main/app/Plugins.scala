// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ APIVersion, Pico }
import java.awt.Component
import java.net.{URL, URLClassLoader}

/** Loads plugins. Plugins are instantiated using PicoContainer so that arbitrary constructor
 * parameters can be injected.  (Currently a plugin is always an extra tab, but we plan to
 * generalize so e.g. BehaviorSpace will be a plugin too, maybe HubNet, etc.) - ST 6/8/11
 */

object Plugins {
  def load(pico: Pico): Seq[(String, Component)] =
    for {
      dirs <- Option(new java.io.File("plugins").listFiles).toSeq
      dir <- dirs
      if dir.isDirectory
      name = dir.getName
      jar = new java.io.File(dir, name + ".jar")
      if jar.exists
    } yield {
      val url = new java.net.URL("jar", "", "file:" + jar.getAbsolutePath + "!/");
      val jarConnection = url.openConnection().asInstanceOf[java.net.JarURLConnection]
      val manifest = jarConnection.getManifest()
      require(manifest != null, "manifest not found")
      val attributes = manifest.getMainAttributes()
      require(attributes.getValue("NetLogo-API-Version") == APIVersion.version,
              "NetLogo-API-Version in manifest must be " + APIVersion.version)
      val tabName = Option(attributes.getValue("Tab-Name")).getOrElse(
        sys.error("Tab-Name not found in manifest"))
      val className = Option(attributes.getValue("Class-Name")).getOrElse(
        sys.error("Class-Name not found in manifest"))
      val loader = new PluginClassLoader(url)
      pico.addComponent(className, loader.load(className))
      val component = pico.getComponent(className).asInstanceOf[java.awt.Component]
      (tabName, component)
    }

    class PluginClassLoader(url: URL) extends URLClassLoader(Array(url), Thread.currentThread.getContextClassLoader) {
      def load(x: String) = findClass(x)  // findClass is protected
    }

}
