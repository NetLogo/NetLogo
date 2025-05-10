// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.quoted.*

object Femto {

  inline def scalaSingleton[T](name: String): T = ${ dynamicCompanionObject[T]('name) }

  inline def get[T](name: String, args: Any*): T = ${ dynamicConstructor[T]('name, 'args) }

  private def dynamicCompanionObject[T](name: Expr[String])(using Type[T])(using q: Quotes): Expr[T] = {
    import q.reflect.*
    Ref(Symbol.classSymbol(name.valueOrAbort + "$").companionModule).asExprOf[T]
  }

  private def dynamicConstructor[T: Type](name: Expr[String], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[T] = {
    import quotes.reflect.*

    val clazz = Symbol.requiredClass(name.valueOrAbort)
    val ctor = Select(New(TypeIdent(clazz)), clazz.primaryConstructor)

    val args = argsExpr match {
      case Varargs(as) => as.map(_.asTerm).toList
      case _ => List()
    }

    Apply(ctor, args).asExprOf[T]
  }

}
