// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

object Femto {
  def scalaSingleton[T](klass: String): T = macro dynamicConstructor[T]

  def dynamicConstructor[T: c.WeakTypeTag](c: BlackBoxContext)(klass: c.Tree): c.Tree = {
    import c.universe._
    klass match {
      case q"${ objectName: String }" =>
        val pkg = c.mirror.staticModule(objectName)
        q"$pkg"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to scalaSingleton")
    }
  }
}
