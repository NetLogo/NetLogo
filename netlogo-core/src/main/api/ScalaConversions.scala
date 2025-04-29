// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.ExtensionObject
import java.{lang => jl}
import org.nlogo.core.{ LogoList, Nobody }

object ScalaConversions {

  // implicits so if you want, you can write
  // "x.toLogoObject" instead of "toLogoObject(x)" and
  // "xs.toLogoList" instead of "toLogoList(xs)"
  implicit class RichAny(val a: Any) {
    def toLogoObject = ScalaConversions.toLogoObject(a)
  }
  implicit class RichSeq[T](val seq: Seq[T]) {
    def toLogoList = ScalaConversions.toLogoList(seq)
  }
  implicit class RichArray[T](arr: Array[T]) {
    def toLogoList = ScalaConversions.toLogoList(arr)
  }

  def toLogoList[T](seq: Seq[T]): LogoList =
    LogoList.fromVector(seq.map(toLogoObject).toVector)

  def toLogoList[T](arr: Array[T]): LogoList =
    toLogoList(arr.toSeq)

  def toLogoList(ll: LogoList): LogoList =
    LogoList.fromVector(ll.toVector.map(toLogoObject))

  def toLogoObject(a: Any): AnyRef = a match {

    // booleans
    case x: Boolean => Boolean.box(x)

    // chars and strings
    case x: String => x
    case x: Char => x.toString
    case x: jl.Character => x.toString

    // numbers
    case x: Byte => Double.box(x)
    case x: Short => Double.box(x)
    case x: Int => Double.box(x)
    case x: Float => Double.box(x)
    case x: Double => Double.box(x)
    case x: Long => Double.box(x.toDouble)

    // netlogo types
    case x: ExtensionObject => x
    case x: Agent => x
    case x: AgentSet => x
    case Nobody => Nobody

    case ll: LogoList => toLogoList(ll)
    // Seqs turn into LogoList. their elements are recursively converted.
    // also recurse into LogoLists.
    case a: Array[_] => toLogoList(a)
    case s: Seq[_] => toLogoList(s)

    // unconvertible type
    case _ =>
      throw new IllegalArgumentException(
        "don't know how to convert: " + a)
  }

}
