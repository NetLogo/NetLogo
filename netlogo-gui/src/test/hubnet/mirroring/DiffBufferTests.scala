// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.scalatest.FunSuite

import java.io.{ ByteArrayInputStream, DataInputStream }

class DiffBufferTests extends FunSuite {
  test("empty -> byte array") {
    val out = new DiffBuffer
    val is = new DataInputStream(new ByteArrayInputStream(out.toByteArray))
    assertResult(DiffBuffer.EMPTY)(is.readShort)
    assertResult(0)(is.available)
  }
}
