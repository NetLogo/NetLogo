// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.scalatest.FunSuite

import java.io.{ ByteArrayInputStream, DataInputStream }

class DiffBufferTests extends FunSuite {
  test("empty -> byte array") {
    val out = new DiffBuffer
    val is = new DataInputStream(new ByteArrayInputStream(out.toByteArray))
    expect(DiffBuffer.EMPTY)(is.readShort)
    expect(0)(is.available)
  }
}
