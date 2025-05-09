// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.quoted.*

import Resource.getResource

// A brief note: you will see that the methods in this class have a tremendous amount in common.
// However, it is hard to abstract out functionality without getting compiler warnings/errors because all types are scoped
// to the particular context in question. Feel free to experiment. I'm sure I haven't yet tried all possibilities and there
// may be a way to get it working, especially as scala marches forward. RG 2/25/15
object TokenClasses {

  case class Entry(primName: String, className: String, isReporter: Boolean)

  private val fileEntries: Seq[Entry] =
    getResource("/system/tokens-core.txt")
      .getLines()
      .toSeq
      .map {
        case l: String =>
          l.split(' ') match {
            case Array("R", primName, className) => Entry(primName.toUpperCase, className,  true)
            case Array("C", primName, className) => Entry(primName.toUpperCase, className, false)
            case _                               => throw new IllegalStateException
          }
      }

  inline def   compiledReporters[T](pckage: String): Map[String, ()     => T] = ${    compileReportersInternal[T]('pckage) }
  inline def    compiledCommands[T](pckage: String): Map[String, ()     => T] = ${     compileCommandsInternal[T]('pckage) }
  inline def packageConstructors[T](pckage: String): Map[String, String => T] = ${ packageConstructorsInternal[T]('pckage) }
  inline def reverseProcedureMap:                    Map[String, Seq[String]] = ${ reverseProcedureMapInternal             }

  private def compileReportersInternal[T](packagePrefix: Expr[String])
                                         (using Type[T])(using q: Quotes): Expr[Map[String, () => T]] = {

    import q.reflect.*

    //'{
    //  fileEntries.collect {
    //    case (primName, className, true) =>
    //      val lambda =
    //        (() => ${
    //          val clazz = Symbol.classSymbol(${$packagePrefix} + "." + ${className})
    //          val ctor  = clazz.primaryConstructor
    //          //New(TypeTree.ref(clazz)).select(ctor).appliedToNone.asExprOf[T]
    //        })
    //      primName -> lambda
    //  }.toMap
    //}

    //'{
    //  fileEntries.collect {
    //    case (primName, className, true) =>
    //      primName -> (() => Class.forName(s"${$packagePrefix}.${className}").newInstance.asInstanceOf[T])
    //  }.toMap
    //}

    '{ Map[String, () => T]() }

  }

  private def compileCommandsInternal[T](packagePrefix: Expr[String])
                                        (using Type[T])(using q: Quotes): Expr[Map[String, () => T]] = {

    import q.reflect.*

    '{ Map[String, () => T]() }

    //'{
    //  fileEntries.collect {
    //    case (primName, className, false) =>
    //      primName -> (() => Class.forName(s"${$packagePrefix}.${className}").newInstance.asInstanceOf[T])
    //  }.toMap
    //}

    //val packageName = packagePrefix.valueOrAbort

    //val mappings =
    //  fileEntries.collect {
    //    case CommandEntry(primName, className) =>
    //      val clazz = Symbol.classSymbol(s"${packageName}.${className}")
    //      val ctor  = clazz.primaryConstructor
    //      '{ key -> (() => new clazz()) }
    //      //'{ primName -> (() => New(TypeTree.ref(clazz)).select(ctor).appliedToNone.asExprOf[T]) }
    //      //'{ primName -> (() => New(TypeTree.ref(clazz)).select(ctor).appliedToNone.asExprOf[T]) }
    //      //primName -> (() => Class.forName(className).newInstance.asInstanceOf[T])
    //  }

    //'{ Map(${ Varargs[(String, () => T)](mappings) }*) }


//   def compileCommandsInternal[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree): c.Tree = {
//     import c.universe._
//     packagePrefix match {
//       case q"${packageName: String}" =>
//         val mapElems = FileEntries.collect {
//           case ("C", commandName, className) => (commandName, className)
//         }.map {
//           case (key, className) =>
//             val klass = c.mirror.staticClass(s"$packageName.$className")
//             q"$key -> (() => new $klass())"
//         }.toList
//         q"Map(..$mapElems)"
//       case _ => c.abort(c.enclosingPosition, "Must supply a string literal to compileReporters")
//     }

  }

  private def packageConstructorsInternal[T](packagePrefix: Expr[String])
                                            (using Type[T])(using q: Quotes): Expr[Map[String, String => T]] = {

    import q.reflect.*

    //val pckage     = Symbol.requiredPackage(packagePrefix.valueOrAbort).declarations
    //val subPckages = pckage.filter(_.isPackageDef).flatMap(_.declarations)

    //(pckage ++ subPckages).flatMap {
    //  case decl: Symbol if decl.isClassDef && !decl.isAbstractType =>
    //    decl.declarations.find(d => d.isClassConstructor && d.signature.paramSigs.length == 1).fold(Nil) {
    //      ctor =>
    //        val lambda = ((s: String) => New(TypeTree.ref(decl)).select(ctor).appliedToArgs(List(Expr(s).asTerm)).asExprOf[T])
    //        List(decl.fullName -> lambda)
    //    }
    //  case _ =>
    //    Nil
    //}.toMap

    '{ Map[String, String => T]() }

    //val packageName = packagePrefix.valueOrAbort

    //val pkg     = c.mirror.staticPackage(packageName).typeSignature.decls
    //val subpkgs = pkg.filter(_.isPackage).flatMap(_.typeSignature.decls)
    //
    //val constructorClosures =
    //  (pkg ++ subpkgs).collect {
    //    case cp: ClassSymbol => (cp, cp.toType)
    //  }.filter {
    //    case (cp: ClassSymbol, t: Type) =>
    //      cp.isClass && ! cp.isModule && ! cp.isTrait && ! cp.isAbstract && t <:< expectedType.tpe &&
    //        cp.typeSignature.decls.exists {
    //          case m: MethodSymbol if m.isConstructor =>
    //            m.paramLists.exists(params => params.length == 1 &&
    //              params.head.typeSignature =:= typeOf[String])
    //          case _ => false
    //        }
    //    case _ =>
    //      throw new IllegalStateException
    //  }.map {
    //    case (cp: ClassSymbol, _) =>
    //      cp.fullName -> ((s: String) => new $cp(s))
    //    case _ =>
    //      throw new IllegalStateException
    //  }

  }

  private def reverseProcedureMapInternal(using Quotes): Expr[Map[String, Seq[String]]] = {
    val reverseMap = fileEntries.map { case Entry(pn, cn, _) => cn -> pn }.groupBy(_._1).view.mapValues(_.map(_._2)).toMap
    Expr(reverseMap)
  }

}
