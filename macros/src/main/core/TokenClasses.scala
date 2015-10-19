// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Resource.getResource
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

// A brief note: you will see that the methods in this class have a tremendous amount in common.
// However, it is hard to abstract out functionality without getting compiler warnings/errors because all types are scoped
// to the particular context in question. Feel free to experiment. I'm sure I haven't yet tried all possibilities and there
// may be a way to get it working, especially as scala marches forward. RG 2/25/15
object TokenClasses {
  val FileEntries: Seq[(String, String, String)] =
    getResource("/system/tokens-core.txt")
      .getLines()
      .toSeq
      .map {
        case l: String =>
          val List(tpe, primName, className) = l.split(' ').toList
          (tpe, primName.toUpperCase, className)
      }

  def compiledReporters[T](packagePrefix: String): Map[String, () => T] =
    macro compileReportersInternal[T]
  def compiledCommands[T](packagePrefix: String): Map[String, () => T] =
    macro compileCommandsInternal[T]

  def packageConstructors[T](packagePrefix: String): Map[String, (String => T)] =
    macro packageConstructorsInternal[T]

  def reverseProcedureMap: Map[String, Seq[String]] =
    macro reverseProcedureMapInternal

  def reverseProcedureMapInternal(c: BlackBoxContext): c.Tree = {
    import c.universe._
    val allProcedureClassNames = FileEntries.map(_._3)
    val reverseMap =
      allProcedureClassNames.zip(
        allProcedureClassNames.map(pcn => FileEntries.filter(_._3 == pcn).map(_._2)))
    val treeMap = reverseMap.map {
      case (procedureClass: String, allProcedureNames: Seq[String]) =>
        val procedureNameList = q"Seq[String](..${allProcedureNames})"
        q"$procedureClass -> $procedureNameList"
    }
    q"Map(..$treeMap)"
  }


  def compileReportersInternal[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${packageName: String}" =>
        val mapElems = FileEntries.collect {
          case ("R", commandName, className) => (commandName, className)
        }.map {
          case (key, className) =>
            val klass = c.mirror.staticClass(s"$packageName.$className")
            q"$key -> (() => new $klass())"
        }.toList
        q"Map(..$mapElems)"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to compileReporters")
    }
  }

  def compileCommandsInternal[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${packageName: String}" =>
        val mapElems = FileEntries.collect {
          case ("C", commandName, className) => (commandName, className)
        }.map {
          case (key, className) =>
            val klass = c.mirror.staticClass(s"$packageName.$className")
            q"$key -> (() => new $klass())"
        }.toList
        q"Map(..$mapElems)"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to compileReporters")
    }
  }

  def packageConstructorsInternal[T: c.WeakTypeTag](c: BlackBoxContext)(
    packagePrefix: c.Tree)(implicit expectedType: c.WeakTypeTag[T]): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${ packageName: String }" =>
        try {
          val pkg = c.mirror.staticPackage(packageName).typeSignature.decls
          val subpkgs = pkg.filter(_.isPackage).flatMap(_.typeSignature.decls)
          val constructorClosures = (pkg ++ subpkgs).collect {
            case cp: ClassSymbol => (cp, cp.toType)
          }.filter {
            case (cp: ClassSymbol, t: Type) =>
              cp.isClass && ! cp.isModule && ! cp.isTrait && ! cp.isAbstract && t <:< expectedType.tpe &&
                cp.typeSignature.decls.exists {
                  case m: MethodSymbol if m.isConstructor =>
                    m.paramLists.exists(params => params.length == 1 &&
                      params.head.typeSignature =:= typeOf[String])
                  case _ => false
                }
          }.map {
            case (cp: ClassSymbol, _) =>
              q"""${cp.fullName} -> ((s: String) => new $cp(s))"""
          }
          q"Map(..$constructorClosures)"
        } catch {
          case ex: ScalaReflectionException => c.abort(c.enclosingPosition, s"Invalid package: $packageName")
        }
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to poolForPackage")
    }
  }
}
