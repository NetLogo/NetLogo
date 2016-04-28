// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

// Loads a class directly from bytecode created at runtime.
//
// After compilation, don't keep any old references to the CustomClassLoader objects around, so the
// dynamically created classes can be garbage collected.
//
// ( Class objects are only free for garbage collection after there are
//   no longer any of their instances in memory, and their ClassLoader
//   has been garbage collected as well.  e.g. See:
//   http://forum.java.sun.com/thread.jspa?threadID=445034&messageID=2014125
//   for a little discussion about this. ) ~Forrest(7/17/2006)

// Making this work with the NetLogo-Mathematica link is fragile.  According to
// http://reference.wolfram.com/mathematica/JLink/tutorial/CallingJavaFromMathematica.html J/Link
// uses a custom class loader, and I guess that has something to do with it.  I really don't
// understand the details. Anyway, the moral is, if you change this, be sure and test it with
// Mathematica, because apparently harmless seeming changes may break it.  - ST 4/16/09

class CustomClassLoader(normalClassLoader: ClassLoader) extends ClassLoader {
  var className: String = null
  var bytecode: Array[Byte] = null
  override def loadClass(name: String): Class[_] =
    if (className == name)
      super.defineClass(className, bytecode, 0, bytecode.length)
    else normalClassLoader.loadClass(name)
  def loadBytecodeClass(className: String, bytecode: Array[Byte]) = {
    this.className = className
    this.bytecode = bytecode
    loadClass(className)
  }
}
