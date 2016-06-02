// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.scalatest.FunSuite
import org.nlogo.core.Let

class ContextTests extends FunSuite {
  test("let 1") {
    val c = new Context(null, null, 0, null, null)
    val let = new Let
    c.let(let, "foo")
    assert(c.getLet(let) === "foo")
    c.setLet(let, "bar")
    assert(c.getLet(let) === "bar")
  }
  test("let 2") {
    val c = new Context(null, null, 0, null, null)
    val (let1, let2) = (new Let, new Let)
    c.let(let1, "foo")
    assert(c.getLet(let1) === "foo")
    c.let(let2, "bar")
    assert(c.getLet(let1) === "foo")
    assert(c.getLet(let2) === "bar")
    c.setLet(let1, "one")
    c.setLet(let2, "two")
    assert(c.getLet(let1) === "one")
    assert(c.getLet(let2) === "two")
  }
}
