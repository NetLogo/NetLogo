package org.nlogo.util

// In Scala 2.7 scala.collection.jcl ("Java Compatibility Library") was huge, so in order not to
// bloat NetLogoLite.jar, we have this object which defines only the conversions on Java collections
// that we really need.

// In Scala 2.8, scala.collection.jcl is gone and instead there is scala.collection.JavaConversions
// which is lot more lightweight.  So arguably we don't need this anymore, though I kind of like
// that our conversions disallow mutation, which JavaConversions supports.

// In Scala 2.8.1, scala.collection.JavaConverters was added, which is like JavaConversions
// but requires an explicit .asScala or .asJava call every time you want to convert.  If we
// change over, we should consider using JavaConverters instead of JavaConversions.

// All of the conversions here return unmodifiable views of the original collections, no copying.

object JCL {
  implicit def toScalaIterable[T](i: java.lang.Iterable[T]): scala.Iterable[T] =
    new scala.Iterable[T] {
      def iterator = new scala.Iterator[T] {
        val it = i.iterator; def next() = it.next(); def hasNext = it.hasNext
      }
    }
  implicit def toScalaIterator[T](it: java.util.Iterator[T]): scala.Iterator[T] =
    new scala.Iterator[T] {
      def next() = it.next(); def hasNext = it.hasNext
    }
  implicit def toScalaIterable[T](e: java.util.Enumeration[T]): scala.Iterable[T] =
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
  implicit def toScalaSeq[T](list: java.util.List[T]): scala.Seq[T] =
    new scala.Seq[T] {
      def apply(idx: Int) = list.get(idx)
      def length = list.size
      def iterator = new scala.Iterator[T] {
        val it = list.iterator; def next() = it.next(); def hasNext = it.hasNext
      }
    }
  implicit def toScalaMap[A,B](m: java.util.Map[A,B]): collection.immutable.Map[A,B] =
    new collection.immutable.Map[A,B] {
      def get(key: A): Option[B] = Option(m.get(key))
      def iterator = toScalaIterable(m.keySet).iterator.map(key => (key, m.get(key)))
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
