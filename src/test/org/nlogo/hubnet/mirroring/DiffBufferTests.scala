// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.scalatest.FunSuite

import java.io.{ ByteArrayInputStream, DataInputStream, IOException }

class DiffBufferTests extends FunSuite {
  test("empty -> byte array") {
    val out = new DiffBuffer
    val is = new DataInputStream(new ByteArrayInputStream(out.toByteArray))
    expect(DiffBuffer.EMPTY)(is.readShort)
    expect(0)(is.available)
  }
}
