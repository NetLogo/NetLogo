// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ ByteArrayInputStream, DataInputStream }

class DiffBufferTests extends AnyFunSuite {
  test("empty -> byte array") {
    val out = new DiffBuffer
    val is = new DataInputStream(new ByteArrayInputStream(out.toByteArray))
    assertResult(DiffBuffer.EMPTY)(is.readShort)
    assertResult(0)(is.available)
  }
}
