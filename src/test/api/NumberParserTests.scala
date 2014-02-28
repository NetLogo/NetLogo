// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite
import NumberParser.parse

class NumberParserTests extends FunSuite {
  for(input <- List("", "-", ".", "-.", ".-"))
    test(input) {
      assert(parse(input).isLeft)
    }
}
