package org.nlogo.util

import org.scalatest.FunSuite

object SerializableThings {
  
  @serializable case class GoodClassThatSetsBitMap(var x: Int) {
    var y = 0

    @throws(classOf[java.io.IOException])
    private def writeObject(out: java.io.ObjectOutputStream) {
      out.writeInt(x)
      out.writeInt(y)
    }

    @throws(classOf[java.io.IOException])
    @throws(classOf[ClassNotFoundException])
    private def readObject(in: java.io.ObjectInputStream) {
      getClass.getField("bitmap$0").set(this, -1)
      x = in.readInt()
      assert(x == x) // call the x() accessor
      y = in.readInt()
      assert(y == y) // call the y() accessor
      ()
    }
  }
  
  @serializable case class BadClassThatDoesNotSetBitMap(var x: Int) {
    var y = 0

    @throws(classOf[java.io.IOException])
    private def writeObject(out: java.io.ObjectOutputStream) {
      out.writeInt(x)
      out.writeInt(y)
    }

    @throws(classOf[java.io.IOException])
    @throws(classOf[ClassNotFoundException])
    private def readObject(in: java.io.ObjectInputStream) {
      x = in.readInt()
      assert(x == x) // call the x() accessor
      y = in.readInt()
      assert(y == y) // call the y() accessor
      ()
    }
  }
}

class TestSerialization extends FunSuite {

  import TestUtils._
  import SerializableThings._

  test("demonstrate scala serializable + initialization order weirdness"){
    val xy = new GoodClassThatSetsBitMap(x=6)
    assert(xy.isSerializable)

    val xy2 = BadClassThatDoesNotSetBitMap(x=6)
    intercept[UninitializedFieldError]{
      assert(xy2.isSerializable)
    }
  }
}
