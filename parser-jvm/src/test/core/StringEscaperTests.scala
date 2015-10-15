// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite

class StringEscaperTests extends FunSuite {
  test("escape empty") {
    assert("" === StringEscaper.escapeString(""))
  }
  test("unescape empty") {
    assert("" === StringEscaper.unescapeString(""))
  }
  test("unescape trailing backslash") {
    assert("\\" === StringEscaper.unescapeString("\\"))
  }
  test("unescape everything") {
    assert("\n\r\t\"\\" === StringEscaper.unescapeString("\\n\\r\\t\\\"\\\\"))
  }
  test("escape everything") {
    assert("\\n\\r\\t\\\"\\\\" === StringEscaper.escapeString("\n\r\t\"\\"))
  }
  test("invalid escape") {
    val ex = intercept[IllegalArgumentException] {
      StringEscaper.unescapeString("\\b")
    }
    assert(ex.getMessage === "invalid escape sequence: \\b")
  }
}
