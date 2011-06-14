package org.nlogo.util

// In the process of getting rid of this, using collection.JavaConverters instead. - ST 5/18/11
import scala.collection.JavaConverters._

object JCL {
  // annoying we have to give these next three separate names, but the Scala 2.9 compiler
  // tells us "parameterized overloaded implicit methods are not visible as view bounds" - ST 2/11/11
  implicit def iterableToScalaIterable[T](i: java.lang.Iterable[T]): scala.Iterable[T] =
    new scala.Iterable[T] {
      def iterator = new scala.Iterator[T] {
        val it = i.iterator; def next() = it.next(); def hasNext = it.hasNext
      }
    }
  implicit def iteratorToScalaIterator[T](it: java.util.Iterator[T]): scala.Iterator[T] =
    new scala.Iterator[T] {
      def next() = it.next(); def hasNext = it.hasNext
    }
  implicit def enumerationToScalaIterable[T](e: java.util.Enumeration[T]): scala.Iterable[T] =
    new scala.Iterable[T] {
      def iterator = new scala.Iterator[T] {
        def next() = e.nextElement(); def hasNext = e.hasMoreElements
      }
    }
  implicit def toJavaIterable[T](i: scala.Iterable[T]): java.lang.Iterable[T] =
    new java.lang.Iterable[T] {
      def iterator() = toJavaIterator(i.iterator)
    }
  implicit def toJavaIterator[T](i: scala.Iterator[T]): java.util.Iterator[T] =
    new java.util.Iterator[T] {
      val it = i; def next() = it.next(); def hasNext = it.hasNext;
      def remove() = throw new UnsupportedOperationException
    }
  // most of the time toScalaIterable gets us the methods we need, but some useful methods are only
  // on Seq or Map, so we have these as backups - ST 4/6/10, 8/20/10
  implicit def toScalaSeq[T](list: java.util.List[T]): scala.Seq[T] = list.asScala
  implicit def toScalaMap[A,B](m: java.util.Map[A,B]): collection.immutable.Map[A,B] =
    new collection.immutable.Map[A,B] {
      def get(key: A): Option[B] = Option(m.get(key))
      def iterator = iterableToScalaIterable(m.keySet).iterator.map(key => (key, m.get(key)))
      def +[B1 >: B](kv: (A, B1)): Map[A, B1] = throw new UnsupportedOperationException
      def -(key: A): Map[A, B] = throw new UnsupportedOperationException
      override def size = m.size
    }
  implicit def toJavaList[A](i: scala.Iterable[A]): java.util.List[A] =
    new java.util.AbstractList[A] {
      def get(n: Int) = i.iterator.drop(n).next
      def size = i.size
      override def iterator = toJavaIterator(i.iterator)
    }
  implicit def toJavaMap[A,B](m: collection.Map[A,B]): java.util.Map[A,B] =
    new java.util.AbstractMap[A,B] {
      def entrySet = new java.util.AbstractSet[java.util.Map.Entry[A,B]] {
        def size = m.size
        def iterator = toJavaIterator(
          m.iterator.map{case (k, v) =>
            new java.util.Map.Entry[A,B] {
              def getKey = k
              def getValue = v
              def setValue(v: B) = throw new UnsupportedOperationException
            }})
      }
    }
  def JavaList[A](xs: A*) = toJavaList(xs)
}
