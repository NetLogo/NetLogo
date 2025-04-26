// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.LogoList
import collection.immutable.VectorBuilder

// for use from Java, or when efficiency is paramount - ST 2/25/11
class LogoListBuilder {
  private val b = new VectorBuilder[AnyRef]
  def add(obj: AnyRef): Unit = {
    b += obj
  }
  def addAll(objs: scala.Iterable[_ <: AnyRef]): Unit = {
    b ++= objs
  }
  def addAll(objs: java.lang.Iterable[_ <: AnyRef]): Unit = {
    val it = objs.iterator
    while(it.hasNext)
      b += it.next()
  }
  def toLogoList = LogoList.fromVector(b.result())
}
