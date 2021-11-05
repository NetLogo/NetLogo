// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.funsuite.AnyFunSuite

class StringUtilsTests extends AnyFunSuite {
  test("escape empty") {
    assert("" === StringUtils.escapeString(""))
  }
  test("unescape empty") {
    assert("" === StringUtils.unEscapeString(""))
  }
  test("unescape trailing backslash") {
    assert("\\" === StringUtils.unEscapeString("\\"))
  }
  test("unescape everything") {
    assert("\n\r\t\"\\" === StringUtils.unEscapeString("\\n\\r\\t\\\"\\\\"))
  }
  test("escape everything") {
    assert("\\n\\r\\t\\\"\\\\" === StringUtils.escapeString("\n\r\t\"\\"))
  }
  test("invalid escape") {
    val ex = intercept[IllegalArgumentException] {
      StringUtils.unEscapeString("\\b")
    }
    assert(ex.getMessage === "invalid escape sequence: \\b")
  }
}
