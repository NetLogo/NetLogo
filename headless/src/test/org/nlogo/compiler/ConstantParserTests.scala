// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.agent.{AgentSet, Patch, Turtle, World}
import org.nlogo.api.{CompilerException, Dump, ExtensionManager, ExtensionObject, LogoList}
import org.nlogo.util.MockSuite

class ConstantParserTests extends FunSuite with MockSuite {

  def defaultWorld = {
    val world = new World
    world.createPatches(-10, 10, -10, 10)
    world.realloc()
    world
  }

  def toConstant(input: String,
                 world: World = defaultWorld,
                 extensionManager: ExtensionManager = null): AnyRef =
    new ConstantParser(world, extensionManager)
      .getConstantValue(Compiler.Tokenizer2D.tokenize(input).iterator)
  def toConstantList(input: String, world: World = defaultWorld): LogoList = {
    val tokens = Compiler.Tokenizer2D.tokenize(input).iterator
    new ConstantParser(world, null).parseConstantList(tokens.next(), tokens)
  }

  def testError(input: String, error: String, world: World = defaultWorld) {
    val e = intercept[CompilerException] {
      toConstant(input, world)
    }
    expectResult(error)(e.getMessage)
  }
  def testListError(input: String, error: String, world: World = defaultWorld) {
    val e = intercept[CompilerException] {
      toConstantList(input, world)
    }
    expectResult(error)(e.getMessage)
  }

  test("booleans") {
    expectResult(java.lang.Boolean.TRUE)(toConstant("true"))
    expectResult(java.lang.Boolean.FALSE)(toConstant("false"))
  }
  test("constantInt") { expectResult(Double.box(4))(toConstant("4")) }
  test("constantIntWhitespace") { expectResult(Double.box(4))(toConstant("  4\t")) }
  test("constantIntParens") { expectResult(Double.box(4))(toConstant(" (4)\t")) }
  test("constantIntParens2") { expectResult(Double.box(4))(toConstant(" ((4)\t)")) }
  test("constantIntBadParens") { testError("((4)", "Expected a closing parenthesis.") }
  test("constantIntBadParens2") { testError("((4)))", "Extra characters after constant.") }
  test("largeConstant1") { testError("9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeConstant2") { testError("-9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeConstant3") { testError("9007199254740993", "9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("largeConstant4") { testError("-9007199254740993", "-9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("constantString") { expectResult("hi there")(toConstant("\"hi there\"")) }
  test("constantList") { expectResult("[1 2 3]")(Dump.logoObject(toConstantList("[1 2 3]"))) }
  test("constantList2") { expectResult("[1 [2] 3]")(Dump.logoObject(toConstantList("[1 [2] 3]"))) }
  test("constantList3") { expectResult("[[1 2 3]]")(Dump.logoObject(toConstantList("[[1 2 3]]"))) }
  test("constantList4") { expectResult("[1 hi true]")(Dump.logoObject(toConstantList("[1 \"hi\" true]"))) }
  test("constantList5") { expectResult("[[1 hi true]]")(Dump.logoObject(toConstant("([([1 \"hi\" true])])"))) }
  test("parseConstantList") { expectResult("[1 2 3]")(Dump.logoObject(toConstantList("[1 2 3]"))) }
  test("parseConstantList2a") { expectResult("[1 [2] 3]")(Dump.logoObject(toConstantList("[1 [2] 3]"))) }
  test("parseConstantList2b") { expectResult("[[1] [2] [3]]")(Dump.logoObject(toConstantList("[[1] [2] [3]]"))) }
  test("parseConstantList3") { expectResult("[[1 2 3]]")(Dump.logoObject(toConstantList("[[1 2 3]]"))) }
  test("parseConstantList4") { expectResult("[1 hi true]")(Dump.logoObject(toConstantList("[1 \"hi\" true]"))) }
  test("parseAgentNoWorld") { testError("{turtle 3}", "Can only have constant agents and agentsets if importing.", world = null) }
  test("parseAgentSetNoWorld") { testError("{all-turtles}", "Can only have constant agents and agentsets if importing.", world = null) }
  test("parsePatch") {
    val result = toConstant("{patch 1 3}").asInstanceOf[Patch]
    expectResult("(patch 1 3)")(Dump.logoObject(result))
  }
  test("parseTurtle") {
    val result = toConstant("{turtle 3}").asInstanceOf[Turtle]
    expectResult("(turtle 3)")(Dump.logoObject(result))
  }
  test("parseTurtles") {
    val input = "{turtles 1 2 3}"
    val result = toConstant(input).asInstanceOf[AgentSet]
    expectResult(input)(Dump.agentset(result, true))
  }
  test("parsePatches") {
    val input = "{patches [1 2] [3 4]}"
    val result = toConstant(input).asInstanceOf[AgentSet]
    expectResult(input)(Dump.agentset(result, true))
  }
  test("parseAllTurtles") {
    val input = "{all-turtles}"
    val result = toConstant(input).asInstanceOf[AgentSet]
    expectResult(input)(Dump.agentset(result, true))
  }
  test("parseAllPatches") {
    val input = "{all-patches}"
    val result = toConstant(input).asInstanceOf[AgentSet]
    expectResult(input)(Dump.agentset(result, true))
  }
  test("badAgent") { testError("{foobar}", "FOOBAR is not an agentset") }
  test("badConstant") { testError("foobar", "Expected a constant.") }
  test("badConstantReporter") { testError("round", "Expected a constant.") }

  mockTest("extension literal") {
    val manager = mock[ExtensionManager]
    expecting {
      one(manager).readExtensionObject("foo", "", "bar baz")
    }
    when {
      val input = "{{foo: bar baz}}"
      val result = toConstant(input, extensionManager = manager)
      assert(result.isInstanceOf[ExtensionObject])
    }
  }

}

class ConstantParser3DTests extends FunSuite {
  val world = new org.nlogo.agent.World3D
  world.createPatches(-10, 10, -10, 10, -10, 10)
  world.realloc()
  def toConstant(input: String): Object =
    new ConstantParser(world, null).getConstantValue(Compiler.Tokenizer3D.tokenize(input).iterator)
  test("parsePatch") {
    val result = toConstant("{patch 1 3 4}").asInstanceOf[Patch]
    expectResult("(patch 1 3 4)")(
      Dump.logoObject(result))
  }
}
