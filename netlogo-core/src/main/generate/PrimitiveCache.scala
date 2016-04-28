// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.objectweb.asm.ClassReader

/**
 * Cache ClassReader objects to speed up bytecode inlining.  Instead of reading in the
 * bytes for each of the _prim classes each time, using a getResourceAsStream() call, we keep a
 * cache of the ClassReader objects, which maps classnames to the corresponding ClassReaders.
 * This resulted in a good speedup, though not as much as I had hoped for.
 * ~Forrest (7/9/2006)
 */
object PrimitiveCache {
  private val cache = new collection.mutable.HashMap[String, ClassReader]
  def getClassReader(c: Class[_]) =
    synchronized { // be threadsafe - ST 2/25/08
      def read = {
        val in = Thread.currentThread
          .getContextClassLoader
          .getResourceAsStream(c.getName.replace('.', '/') + ".class")
        require(in != null)
        try new ClassReader(in)
        finally in.close()
      }
      cache.getOrElseUpdate(c.getName, read)
    }
}
