// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class StringUtilsTests extends FunSuite {
  test("escape empty") {
    assert("" === StringUtils.escapeString(""))
  }
  test("unescape empty") {
    assert("" === StringUtils.unescapeString(""))
  }
  test("unescape trailing backslash") {
    assert("\\" === StringUtils.unescapeString("\\"))
  }
  test("unescape everything") {
    assert("\n\r\t\"\\" === StringUtils.unescapeString("\\n\\r\\t\\\"\\\\"))
  }
  test("escape everything") {
    assert("\\n\\r\\t\\\"\\\\" === StringUtils.escapeString("\n\r\t\"\\"))
  }
  test("invalid escape") {
    val ex = intercept[IllegalArgumentException] {
      StringUtils.unescapeString("\\b")
    }
    assert(ex.getMessage === "invalid escape sequence: \\b")
  }
}
