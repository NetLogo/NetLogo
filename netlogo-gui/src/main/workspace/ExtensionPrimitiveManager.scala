// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.Primitive

class ExtensionPrimitiveManager(val name: String) extends org.nlogo.api.PrimitiveManager {
  private[workspace] val importedPrimitives = collection.mutable.HashMap[String, Primitive]()
  var autoImportPrimitives = false
  def addPrimitive(name: String, prim: Primitive) {
    importedPrimitives.put(name.toUpperCase, prim)
  }
  /**
   * Returns the names of all imported primitives.
   */
  def getPrimitiveNames(): java.util.Iterator[String] = {
    import collection.JavaConverters._
    importedPrimitives.keySet.iterator.asJava
  }
  /**
   * Returns the primitive associated with a name.
   * @param name  the name of the primitive
   * @return the primitive associated with <code>name</code>, or <code>null</code> if
   * there isn't one
   */
  def getPrimitive(name: String): Primitive =
    importedPrimitives.get(name).orNull
}
