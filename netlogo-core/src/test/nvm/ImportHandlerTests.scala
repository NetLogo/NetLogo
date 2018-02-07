// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.scalatest.FunSuite

import org.nlogo.{ api, agent, core },
  agent.{ AgentSet, World2D, DummyLiteralParser },
  api.{ DummyExtensionManager, Dump },
  core.{ ExtensionObject, TokenDSL }

class ImportHandlerTests extends FunSuite {
  import TokenDSL._

  test("parses agent and agent set literals") {
    val result = importHandler.parseLiteralAgentOrAgentSet(
      tokenIterator(id("all-turtles"), `}`), DummyLiteralParser)
      .asInstanceOf[AgentSet]
    assertResult("{all-turtles}")(Dump.agentset(result, true))
  }

  test("parses extension literals"){
    val result = importHandler.parseExtensionLiteral(ex("{{ext:obj foo bar baz}}"))
      .asInstanceOf[DummyExtensionObject]
    assert(result.extname  == "ext")
    assert(result.typeName == "obj")
    assert(result.value    == "foo bar baz")
  }

  test("returns a string when asked to parse an improperly formatted extension literal") {
    assertResult("{{foo}}")(importHandler.parseExtensionLiteral(ex("{{foo}}")))
  }

  def defaultWorld: api.World = {
    val world = new World2D()
    world.createPatches(-10, 10, -10, 10)
    world.realloc()
    world
  }

  class DummyExtensionObject(val extname: String, val typeName: String, val value: String) extends ExtensionObject {
    override def dump(readable: Boolean, exporting: Boolean, reference: Boolean): String = ???
    override def recursivelyEqual(obj: AnyRef): Boolean = ???
    override def getNLTypeName: String = ???
    override def getExtensionName: String = ???
  }

  def extensionManager = new DummyExtensionManager() {
    override def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject =
      new DummyExtensionObject(extname, typeName, value)
  }

  def importHandler = new ImportHandler(defaultWorld, extensionManager)
}
