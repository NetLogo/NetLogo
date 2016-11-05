// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

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
  def fromSeq(s: Seq[AnyRef]) =
    new LogoList(s.toVector)
  implicit def toIterator(ll:LogoList): Iterator[AnyRef] = ll.scalaIterator
}

class LogoList protected (protected val iseq: IndexedSeq[AnyRef])
  extends IndexedSeq[AnyRef] with Serializable {

  def scalaIterator = iseq.iterator
  override def toVector = iseq.toVector

  override def length: Int = iseq.length

  override def apply(idx: Int): AnyRef = iseq(idx)

  override def iterator: collection.Iterator[AnyRef] =
    iseq.iterator
  def get(index: Int) = iseq(index)
  override def size = iseq.size
  def javaIterator: java.util.Iterator[AnyRef] =
    new Iterator(iseq)
  def javaIterable: java.lang.Iterable[AnyRef] =
    new Iterable(iseq)
  def toJava: java.util.AbstractSequentialList[AnyRef] =
    new JavaList(iseq, size)
  def listIterator(i: Int): java.util.ListIterator[AnyRef] =
    new Iterator(iseq.drop(i))
  def add(index: Int, obj: AnyRef) = unsupported

  /// public methods for prims. input validity checking is caller's job

  def first = iseq.head
  def fput(obj: AnyRef) = LogoList.fromSeq(obj +: iseq)
  def lput(obj: AnyRef) = LogoList.fromSeq(iseq :+ obj)
  override def reverse = LogoList.fromSeq(iseq.reverse)
  def replaceItem(index: Int, obj: AnyRef) = LogoList.fromSeq(iseq.updated(index, obj))
  def logoSublist(start: Int, stop: Int) = LogoList.fromSeq(iseq.slice(start, stop))
  def butFirst = LogoList.fromSeq(iseq.tail)
  def butLast = LogoList.fromSeq(iseq.init)
  def removeItem(index: Int) = LogoList.fromSeq(iseq.patch(index, Nil, 1))

  override def toString = iseq.mkString("[", ", ", "]")

  /// Iterator class

  private class Iterable(iseq: IndexedSeq[AnyRef]) extends java.lang.Iterable[AnyRef] {
    val iterator = new Iterator(iseq)
  }
  private class Iterator(iseq: IndexedSeq[AnyRef]) extends java.util.ListIterator[AnyRef] {
    private val it = iseq.iterator
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

  private class JavaList(iseq: IndexedSeq[AnyRef], override val size: Int) extends java.util.AbstractSequentialList[AnyRef] {
    override def listIterator(index: Int): java.util.ListIterator[AnyRef] =
      new Iterator(iseq.drop(index))
  }

  private def unsupported = throw new UnsupportedOperationException
}
