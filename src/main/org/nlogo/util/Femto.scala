// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

// Sometimes we need to do "dependency injection" (here, taken merely as "using reflection to avoid
// a compile-time dependency") in low-level packages that aren't allowed to use PicoContainer
// because PicoContainer would make the applet jars 280K heavier. So we have this ultra low rent
// alternative. - ST 3/2/09, 11/21/09, 7/9/12

object Femto {
  def get[T](interfaceClass: Class[T], implementationClassName: String, args: Array[AnyRef]): T = {
    val clazz = Class.forName(implementationClassName)
    val constructors =
      clazz.getConstructors.filter(_.getParameterTypes.size == args.size)
    assert(constructors.size == 1)
    constructors.head.newInstance(args:_*).asInstanceOf[T]
  }
  def scalaSingleton[T](interfaceClass: Class[T], implementationClassName: String): T =
    Class.forName(implementationClassName + "$")
      .getField( "MODULE$" ).get(null).asInstanceOf[T]
}
