// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.util.{ List => JList }

import org.scalatest.FunSuite

import org.nlogo.{ api, agent, core },
  agent.{ AgentSet, World, DummyLiteralParser },
  api.{ Dump, ExtensionManager => ApiExtensionManager , ClassManager },
  core.{ ExtensionObject, File, ErrorSource, Primitive, TokenDSL}

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
    val world = new World()
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

  def extensionManager = {
    new ApiExtensionManager {
      def storeObject(obj: AnyRef) = ???
      def retrieveObject: AnyRef = ???
      def readExtensionObject(extname: String, typeName: String, value: String): ExtensionObject = new DummyExtensionObject(extname, typeName, value)
      def readFromString(src: String): AnyRef = ???
      def reset() = ???
      def startFullCompilation() = ???
      def finishFullCompilation() = ???
      def anyExtensionsLoaded: Boolean = ???
      def loadedExtensions: java.lang.Iterable[ClassManager] = ???
      def replaceIdentifier(name: String): Primitive = ???
      def importExtension(jarPath: String, errors: ErrorSource) = ???
      def resolvePath(path: String): String = ???
      def resolvePathAsURL(path: String): String = ???
      def dumpExtensions: String = ???
      def dumpExtensionPrimitives(): String = ???
      def addToLibraryPath(classManager: AnyRef, directory: String) = ???
      def getFile(path: String): File = ???
      def getJarPaths: JList[String] = ???
      def getExtensionNames: JList[String] = ???
      def profilingEnabled: Boolean = ???
    }
  }

  def importHandler = new ImportHandler(defaultWorld, extensionManager)
}
