// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler
private object Instantiator {
  def newInstance[T](clazz: Class[_ <: T], args: AnyRef*) =
    clazz.getConstructor(args.map(_.getClass): _*)
         .newInstance(args: _*)
         .asInstanceOf[T]
}
