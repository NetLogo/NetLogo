// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import java.lang.StringBuilder

class ReplacementTests extends FunSuite {
  test("simple") {
    val sb = new StringBuilder("the quick brown fox")
    var offset = 0
    offset = new Replacement(4, 4, "", "bis").replace(sb, offset)
    expectResult(3)(offset)
    offset = new Replacement(16, 19, "fox", "pancake").replace(sb, offset)
    expectResult(7)(offset)
    expectResult("the bisquick brown pancake")(sb.toString)
  }
  test("error 1") {
    intercept[Replacement.FailedException] {
      new Replacement(2, 4, "cd", "").replace(new StringBuilder("abczefg"), 0)
    }
  }
  test("error 2") {
    intercept[Replacement.FailedException] {
      new Replacement(2, 100, "cd", "").replace(new StringBuilder("abcdefg"), 0)
    }
  }
}
