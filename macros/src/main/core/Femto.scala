// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

object Femto {
  def scalaSingleton[T](klass: String): T = macro dynamicCompanionObject[T]

  def get[T](klass: String, args: Any*): T = macro dynamicConstructor[T]

  def dynamicConstructor[T: c.WeakTypeTag](c: BlackBoxContext)(klass: c.Tree, args: c.Expr[Any]*): c.Tree = {
    import c.universe._
    klass match {
      case q"${ objectName: String }" =>
        val staticClass = c.mirror.staticClass(objectName)
        q"new $staticClass(..$args)"
      case other =>
        q"""throw new IllegalStateException("Must supply a string literal to scalaSingleton")"""
    }
  }

  def dynamicCompanionObject[T: c.WeakTypeTag](c: BlackBoxContext)(klass: c.Tree): c.Tree = {
    import c.universe._
    klass match {
      case q"${ objectName: String }" =>
        val pkg = c.mirror.staticModule(objectName)
        q"$pkg"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to scalaSingleton")
    }
  }
}
