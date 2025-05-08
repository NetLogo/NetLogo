// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// For doing "dependency injection" (here, taken merely as "using reflection to avoid
// a compile-time dependency")

object Femto {
  def get[T](className: String, args: Any*): T =
    get(Class.forName(className), args*)
  def get[T](clazz: Class[?], args: Any*): T = {
    val constructors =
      clazz.getConstructors.filter(_.getParameterTypes.size == args.size)
    assert(constructors.size == 1)
    constructors.head
      .newInstance(args.map(_.asInstanceOf[AnyRef])*)
      .asInstanceOf[T]
  }

  // used by java to avoid trouble with scala generics
  def getJ[T](clazz: Class[?], implementationClassName: String, args: Array[AnyRef]): T = {
    val runtimeClazz = Class.forName(implementationClassName)
    val constructors =
      runtimeClazz.getConstructors.filter(_.getParameterTypes.size == args.size)
    assert(constructors.size == 1)
    constructors.head.newInstance(args*).asInstanceOf[T]
  }

  def scalaSingleton[T](className: String): T =
    Class.forName(className + "$")
      .getField("MODULE$").get(null).asInstanceOf[T]
}
