// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite

class NumberParserTests extends FunSuite {
  for(input <- List("", "-", ".", "-.", ".-"))
    test(s"isLeft $input") {
      assert(NumberParser.parse(input).isLeft)
    }
}
