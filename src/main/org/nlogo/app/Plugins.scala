package org.nlogo.app

import org.nlogo.util.Pico
import java.awt.Component

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
      val loader = new java.net.URLClassLoader(
        Array(jar.toURI.toURL),
        Thread.currentThread.getContextClassLoader) {
          def load(x: String) = findClass(x)  // findClass is protected
        }
      val className = "org.nlogo.review.ReviewTab" // zzz TODO don't hardcode
      pico.addComponent(className, loader.load(className))
      val tabName = "Review" // zzz TODO don't hardcode
      val component = pico.getComponent(className).asInstanceOf[java.awt.Component]
      (tabName, component)
    }
}
