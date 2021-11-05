// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.funsuite.AnyFunSuite

class NumberParserTests extends AnyFunSuite {
  for(input <- List("", "-", ".", "-.", ".-"))
    test(s"isLeft $input") {
      assert(NumberParser.parse(input).isLeft)
    }

  test("in comma-delimited-decimal regions, number-like strings starting with '.' do not error") {
    assert(NumberParser.parse(".5*").isLeft)
  }
}
