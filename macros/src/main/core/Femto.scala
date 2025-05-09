// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.quoted.*

object Femto {

  inline def scalaSingleton[T](name: String): T = ${ dynamicCompanionObject[T]('name) }

  inline def get[T](name: String, args: Any*): T = ??? // ${ dynamicConstructor[T]('name, 'args) }

  def dynamicCompanionObject[T](name: Expr[String])(using Type[T])(using q: Quotes): Expr[T] = {
    import q.reflect.*
    Ref(Symbol.classSymbol(name.valueOrAbort + "$").companionModule).asExprOf[T]
  }

  def dynamicConstructor[T](name: Expr[String], args: Expr[Seq[Any]])(using Type[T])(using q: Quotes): Expr[T] = {
    import q.reflect.*

    val clazz = Symbol.classSymbol(name.valueOrAbort)
    val ctor  = clazz.primaryConstructor
    New(TypeTree.ref(clazz)).select(ctor).appliedTo(args.asTerm).asExprOf[T]
    // TODO: I think I should be using `appliedToArgs`, but I'm not sure how to turn `args` inside out
  }

}
