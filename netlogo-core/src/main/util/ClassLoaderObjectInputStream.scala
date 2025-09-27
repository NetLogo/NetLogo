// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.io.{ InputStream, ObjectInputStream, ObjectStreamClass }

case class ClassLoaderObjectInputStream(classLoader: ClassLoader,
                                        inputStream: InputStream) extends ObjectInputStream(inputStream) {
  override def resolveClass(objectStreamClass: ObjectStreamClass): Class[?] = {
    val clazz = Class.forName(objectStreamClass.getName, false, classLoader)
    if (clazz != null) clazz
    else super.resolveClass(objectStreamClass)
  }
}
