// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.quoted.*

object Femto {

  inline def scalaSingleton[T](name: String): T = ${ dynamicCompanionObject[T]('name) }

  inline def get[T](name: String, args: Any*): T = ??? //${ dynamicConstructor[T]('name, 'args) }

  private def dynamicCompanionObject[T](name: Expr[String])(using Type[T])(using q: Quotes): Expr[T] = {
    import q.reflect.*
    Ref(Symbol.classSymbol(name.valueOrAbort + "$").companionModule).asExprOf[T]
  }

  private def dynamicConstructor[T](name: Expr[String], args: Expr[Seq[Any]])(using Type[T])(using q: Quotes): Expr[T] = {
    import q.reflect.*

    println("Hi")
    println(name.show)
    println(args.show)
    val clazz = Symbol.classSymbol(name.valueOrAbort)
    println(s"Class: ${clazz}")
    val ctor  = clazz.primaryConstructor
    println(s"Ctor: ${ctor}")
    val result = New(TypeTree.ref(clazz)).select(ctor).appliedTo(args.asTerm).asExprOf[T]

    println(result)
    result
    // TODO: I think I should be using `appliedToArgs`, but I'm not sure how to turn `args` inside out
  }

}
