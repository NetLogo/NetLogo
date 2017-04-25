// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util

import scala.collection
import scala.language.implicitConversions

object LogoList {
  val Empty = new LogoList(Vector[AnyRef]())
  def apply(objs: AnyRef*) = new LogoList(Vector[AnyRef]() ++ objs)
  def fromJava(objs: java.lang.Iterable[_ <: AnyRef]) = {
    import collection.JavaConverters._
    fromIterator(objs.iterator.asScala)
  }
  def fromIterator(it: scala.Iterator[_ <: AnyRef]) =
    new LogoList(it.toVector)
  def fromVector(v: Vector[AnyRef]) =
    new LogoList(v)
  implicit def toIterator(ll:LogoList): Iterator[AnyRef] = ll.scalaIterator
}

class LogoList private (private val v: Vector[AnyRef])
  extends IndexedSeq[AnyRef] with Serializable {

  def scalaIterator = v.iterator
  override def toVector = v

  override def length: Int = v.length

  override def apply(idx: Int): AnyRef = v(idx)

  override def iterator: collection.Iterator[AnyRef] =
    v.iterator
  def get(index: Int) = v(index)
  override def size = v.size
  def javaIterator: java.util.Iterator[AnyRef] =
    new Iterator(v)
  def javaIterable: java.lang.Iterable[AnyRef] =
    new Iterable(v)
  def toJava: java.util.AbstractSequentialList[AnyRef] =
    new JavaList(v, size)
  def listIterator(i: Int): java.util.ListIterator[AnyRef] =
    new Iterator(v.drop(i))
  def add(index: Int, obj: AnyRef) = unsupported

  /// public methods for prims. input validity checking is caller's job

  def first = v.head
  def fput(obj: AnyRef) = new LogoList(obj +: v)
  def lput(obj: AnyRef) = new LogoList(v :+ obj)
  override def reverse = new LogoList(v.reverse)
  def replaceItem(index: Int, obj: AnyRef) =
    new LogoList(v.updated(index, obj))
  def insertItem(index: Int, obj: AnyRef, elt: AnyRef) =
    new LogoList(v.patch(index, Seq(elt), 0))
  def logoSublist(start: Int, stop: Int) =
    new LogoList(v.slice(start, stop))
  def butFirst = new LogoList(v.tail)
  def butLast = new LogoList(v.init)
  def removeItem(index: Int) =
    new LogoList(v.patch(index, Nil, 1))

  override def toString = v.mkString("[", ", ", "]")

  /// Iterator class

  private class Iterable(v: Vector[AnyRef]) extends java.lang.Iterable[AnyRef] {
    val iterator = new Iterator(v)
  }
  private class Iterator(v: Vector[AnyRef]) extends java.util.ListIterator[AnyRef] {
    private val it = v.iterator
    override def hasNext = it.hasNext
    override def hasPrevious = unsupported
    override def next = it.next
    override def add(obj: Object) = unsupported
    override def set(obj: Object) = unsupported
    override def previousIndex = unsupported
    override def nextIndex = unsupported
    override def previous = unsupported
    override def remove = unsupported
  }

  private class JavaList(v: Vector[AnyRef], override val size: Int) extends java.util.AbstractSequentialList[AnyRef] {
    override def listIterator(index: Int): java.util.ListIterator[AnyRef] =
      new Iterator(v.splitAt(index)._2)
  }

  private def unsupported = throw new UnsupportedOperationException

}
