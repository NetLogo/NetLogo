// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

// For doing "dependency injection" (here, taken merely as "using reflection to avoid
// a compile-time dependency")

object Femto {
  def get[T, U](interfaceClass: Class[T], implementationClassName: String, args: Array[U]): T = {
    val clazz = Class.forName(implementationClassName)
    val constructors =
      clazz.getConstructors.filter(_.getParameterTypes.size == args.size)
    assert(constructors.size == 1)
    constructors.head
      .newInstance(args.asInstanceOf[Array[AnyRef]]: _*)
      .asInstanceOf[T]
  }
  def scalaSingleton[T](interfaceClass: Class[T], implementationClassName: String): T =
    Class.forName(implementationClassName + "$")
      .getField( "MODULE$" ).get(null).asInstanceOf[T]
}
