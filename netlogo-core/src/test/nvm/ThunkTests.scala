// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.scalatest.funsuite.AnyFunSuite

class ThunkTests extends AnyFunSuite {
  test("thunks") {
    var x = 0
    val thunk = new Thunk[Int]() { def compute() = { x += 1 ; x } }
    assert(x === 0)
    assert(thunk.value === 1)
    assert(x === 1)
    // once forced, it remembers its value
    assert(thunk.value === 1)
    assert(x === 1)
  }
}
