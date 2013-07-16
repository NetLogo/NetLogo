// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

// For doing "dependency injection" (here, taken merely as "using reflection to avoid
// a compile-time dependency")

object Femto {
  def get[T](className: String, args: Any*): T =
    get(Class.forName(className), args: _*)
  def get[T](clazz: Class[_], args: Any*): T = {
    val constructors =
      clazz.getConstructors.filter(_.getParameterTypes.size == args.size)
    assert(constructors.size == 1)
    constructors.head
      .newInstance(args.map(_.asInstanceOf[AnyRef]): _*)
      .asInstanceOf[T]
  }
  def scalaSingleton[T](className: String): T =
    Class.forName(className + "$")
      .getField("MODULE$").get(null).asInstanceOf[T]
}
