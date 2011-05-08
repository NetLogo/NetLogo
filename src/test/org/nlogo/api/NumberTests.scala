package org.nlogo.api

import org.scalatest.FunSuite
import Number.parse

class NumberTests extends FunSuite {
  for(input <- List("", "-", ".", "-.", ".-"))
    test(input) {
      assert(parse(input).isLeft)
    }
}
