package org.nlogo.extensions.nw.gephi

import org.nlogo.extensions.nw.NetworkExtension

object GephiUtils {
  // Gephi uses OpenIDE's Lookup system, who's default Lookup uses the current context class loader.
  // However, this is not the extension's class loader, which is what we want it to use. This is the
  // best solution I could come up. Would love to find another one.
  // BCH 1/14/2015
  def withNWLoaderContext[T] = withClassLoaderContext[T](classOf[NetworkExtension].getClassLoader)

  def withClassLoaderContext[T](loader: ClassLoader)(body: => T) = {
    val oldLoader = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(loader)
      body
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }
}
