// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.front

import org.scalatest.FunSuite
import org.nlogo.agent.{AgentSet, Patch, Turtle, World}
import org.nlogo.api.{CompilerException, Dump, ExtensionManager, ExtensionObject, LogoList}
import org.nlogo.util.MockSuite
import org.nlogo.parse

// Even though LiteralParser is in parse, we can't test it fully without
// a LiteralAgentParser, and that's in the agent package, so these tests
// are in compile.front not parse because of that runtime dependency.  (It would
// be nice to separate the tests that require a LiteralAgentParser from
// the ones that don't...) - ST 5/3/13

class TestLiteralParser extends FunSuite with MockSuite {

  def defaultWorld = {
    val world = new World
    world.createPatches(-10, 10, -10, 10)
    world.realloc()
    world
  }

  def toLiteral(input: String,
                 world: World = defaultWorld,
                 extensionManager: ExtensionManager = null): AnyRef =
    FrontEnd.literalParser(world, extensionManager)
      .getLiteralValue(FrontEnd.tokenizer.tokenize(input).map(parse.Namer0))

  def toLiteralList(input: String, world: World = defaultWorld): LogoList = {
    val tokens = FrontEnd.tokenizer.tokenize(input).map(parse.Namer0)
    val (result, _) = FrontEnd.literalParser(world, null).parseLiteralList(tokens.next(), tokens)
    result
  }

  def testError(input: String, error: String, world: World = defaultWorld) {
    val e = intercept[CompilerException] {
      toLiteral(input, world)
    }
    assertResult(error)(e.getMessage)
  }
  def testListError(input: String, error: String, world: World = defaultWorld) {
    val e = intercept[CompilerException] {
      toLiteralList(input, world)
    }
    assertResult(error)(e.getMessage)
  }

  test("booleans") {
    assertResult(java.lang.Boolean.TRUE)(toLiteral("true"))
    assertResult(java.lang.Boolean.FALSE)(toLiteral("false"))
  }
  test("literalInt") { assertResult(Double.box(4))(toLiteral("4")) }
  test("literalIntWhitespace") { assertResult(Double.box(4))(toLiteral("  4\t")) }
  test("literalIntParens") { assertResult(Double.box(4))(toLiteral(" (4)\t")) }
  test("literalIntParens2") { assertResult(Double.box(4))(toLiteral(" ((4)\t)")) }
  test("literalIntBadParens") { testError("((4)", "Expected a closing parenthesis.") }
  test("literalIntBadParens2") { testError("((4)))", "Extra characters after literal.") }
  test("largeLiteral1") { testError("9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeLiteral2") { testError("-9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeLiteral3") { testError("9007199254740993", "9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("largeLiteral4") { testError("-9007199254740993", "-9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("literalString") { assertResult("hi there")(toLiteral("\"hi there\"")) }
  test("literalList") { assertResult("[1 2 3]")(Dump.logoObject(toLiteralList("[1 2 3]"))) }
  test("literalList2") { assertResult("[1 [2] 3]")(Dump.logoObject(toLiteralList("[1 [2] 3]"))) }
  test("literalList3") { assertResult("[[1 2 3]]")(Dump.logoObject(toLiteralList("[[1 2 3]]"))) }
  test("literalList4") { assertResult("[1 hi true]")(Dump.logoObject(toLiteralList("[1 \"hi\" true]"))) }
  test("literalList5") { assertResult("[[1 hi true]]")(Dump.logoObject(toLiteral("([([1 \"hi\" true])])"))) }
  test("parseLiteralList") { assertResult("[1 2 3]")(Dump.logoObject(toLiteralList("[1 2 3]"))) }
  test("parseLiteralList2a") { assertResult("[1 [2] 3]")(Dump.logoObject(toLiteralList("[1 [2] 3]"))) }
  test("parseLiteralList2b") { assertResult("[[1] [2] [3]]")(Dump.logoObject(toLiteralList("[[1] [2] [3]]"))) }
  test("parseLiteralList3") { assertResult("[[1 2 3]]")(Dump.logoObject(toLiteralList("[[1 2 3]]"))) }
  test("parseLiteralList4") { assertResult("[1 hi true]")(Dump.logoObject(toLiteralList("[1 \"hi\" true]"))) }
  test("parseAgentNoWorld") { testError("{turtle 3}", "Can only have literal agents and agentsets if importing.", world = null) }
  test("parseAgentSetNoWorld") { testError("{all-turtles}", "Can only have literal agents and agentsets if importing.", world = null) }
  test("parsePatch") {
    val result = toLiteral("{patch 1 3}").asInstanceOf[Patch]
    assertResult("(patch 1 3)")(Dump.logoObject(result))
  }
  test("parseTurtle") {
    val result = toLiteral("{turtle 3}").asInstanceOf[Turtle]
    assertResult("(turtle 3)")(Dump.logoObject(result))
  }
  test("parseTurtles") {
    val input = "{turtles 1 2 3}"
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult(input)(Dump.agentset(result, true))
  }
  test("parsePatches") {
    val input = "{patches [1 2] [3 4]}"
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult(input)(Dump.agentset(result, true))
  }
  test("parseAllTurtles") {
    val input = "{all-turtles}"
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult(input)(Dump.agentset(result, true))
  }
  test("parseAllPatches") {
    val input = "{all-patches}"
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult(input)(Dump.agentset(result, true))
  }
  test("badAgent") { testError("{foobar}", "FOOBAR is not an agentset") }
  test("badLiteral") { testError("foobar", "Expected a literal value.") }
  test("badLiteralReporter") { testError("round", "Expected a literal value.") }

  mockTest("extension literal") {
    val manager = mock[ExtensionManager]
    expecting {
      one(manager).readExtensionObject("foo", "", "bar baz")
    }
    when {
      val input = "{{foo: bar baz}}"
      val result = toLiteral(input, extensionManager = manager)
      assert(result.isInstanceOf[ExtensionObject])
    }
  }

}
