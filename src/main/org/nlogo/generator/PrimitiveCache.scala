// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generator

import org.objectweb.asm.ClassReader

/**
 * Cache ClassReader objects to speed up bytecode inlining.  Instead of reading in the
 * bytes for each of the _prim classes each time, using a getResourceAsStream() call, we keep a
 * cache of the ClassReader objects, which maps classnames to the corresponding ClassReaders.
 * This resulted in a good speedup, though not as much as I had hoped for.
 * ~Forrest (7/9/2006)
 */
private object PrimitiveCache {
  private val cache = new collection.mutable.HashMap[String, ClassReader]
  def getClassReader(c: Class[_]) =
    synchronized { // be threadsafe - ST 2/25/08
      def read = {
        val in = {

          val name       = c.getName.replace('.', '/') + ".class"
          val fromThread = Thread.currentThread.getContextClassLoader.getResourceAsStream(name)

          // Due to a regression in the JRE (http://bugs.sun.com/view_bug.do?bug_id=8017776),
          // try harder to get ahold of the `JNLPClassLoader` in WebStart --JAB (7/22/13)
          if (fromThread != null)
            fromThread
          else
            this.getClass.getClassLoader.getResourceAsStream(name)

        }
        require(in != null)
        try new ClassReader(in)
        finally in.close()
      }
      cache.getOrElseUpdate(c.getName, read)
    }
}
