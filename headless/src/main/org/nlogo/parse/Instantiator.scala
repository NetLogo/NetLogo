// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

object Instantiator {
  def newInstance[T](clazz: Class[_ <: T], args: Any*) = {
    // if this exists in the stdlib, I can't seem to find it - ST 8/2/12
    def getClassOfAny(a: Any): Class[_] = a match {
      case l: Long => classOf[Long]
      case i: Int => classOf[Int]
      case s: Short => classOf[Short]
      case b: Boolean => classOf[Boolean]
      case b: Byte => classOf[Byte]
      case f: Float => classOf[Float]
      case d: Double => classOf[Double]
      case c: Char => classOf[Char]
      case a: AnyRef => a.getClass
    }
    val boxedArgs = args.map(_.asInstanceOf[AnyRef])
    clazz.getConstructor(boxedArgs.map(getClassOfAny): _*)
         .newInstance(boxedArgs: _*)
         .asInstanceOf[T]
  }
}
