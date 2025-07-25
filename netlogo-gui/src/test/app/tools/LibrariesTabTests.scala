// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.nlogo.api.DummyExtensionManager
import org.nlogo.core.{ Femto, FrontEndInterface, NetLogoCore, Token }

import org.scalatest.funsuite.AnyFunSuite

import LibrariesTab.addExtsToSource

class LibrariesTabTests extends AnyFunSuite {
  private val frontEnd = Femto.scalaSingleton[FrontEndInterface]("org.nlogo.parse.FrontEnd")
  private val extensionManager = new DummyExtensionManager

  private def tokenizeSource(source: String): Iterator[Token] =
    frontEnd.tokenizeWithWhitespace(source, NetLogoCore, extensionManager)

  test("adds new extensions statement to empty source") {
    val source = ""
    val requiredExts = Set("fetch")
    val expected = "extensions [ fetch ]\n"
    assert(addExtsToSource(source, requiredExts, tokenizeSource).contains(expected))
  }

  test("adds new extensions statement to code without one already") {
    val source = "to go end"
    val requiredExts = Set("fetch")
    val expected =
      """extensions [ fetch ]
        |to go end""".stripMargin
    assert(addExtsToSource(source, requiredExts, tokenizeSource).contains(expected))
  }

  test("adds extensions to existing simple extensions statement") {
    val source =
      """extensions [ array ]
        |to go end""".stripMargin
    val requiredExts = Set("fetch", "import-a")
    val expected =
      """extensions [ fetch import-a array ]
        |to go end""".stripMargin
    assert(addExtsToSource(source, requiredExts, tokenizeSource).contains(expected))
  }

  test("adds extensions to existing complex extensions statement") {
    val source =
      """; ignore
        |   extensions ;ignore [
        |;ignore
        |[;ignore
        |array ;ignore ]
        |]
        |;ignore
        |to go end""".stripMargin
    val requiredExts = Set("fetch", "import-a")
    val expected =
      """; ignore
        |   extensions ;ignore [
        |;ignore
        |[ fetch import-a ;ignore
        |array ;ignore ]
        |]
        |;ignore
        |to go end""".stripMargin
    assert(addExtsToSource(source, requiredExts, tokenizeSource).contains(expected))
  }

  test("adds extensions to complex statement with other blocks in code") {
    val source =
      """; ignore
        |extensions ; ignore [
        |; ignore
        |[ ; ignore
        |  array ; ignore ]
        |]
        |; ignore
        |to go
        |  show []
        |end""".stripMargin
    val requiredExts = Set("fetch", "import-a")
    val expected =
      """; ignore
        |extensions ; ignore [
        |; ignore
        |[ fetch import-a ; ignore
        |  array ; ignore ]
        |]
        |; ignore
        |to go
        |  show []
        |end""".stripMargin
    assert(addExtsToSource(source, requiredExts, tokenizeSource).contains(expected))
  }

  test("adds extensions to existing directive in middle of line") {
    val source = "globals [ a b c ] extensions [ array ] breed [ cats cat ]"
    val expected = "globals [ a b c ] extensions [ arduino array ] breed [ cats cat ]"

    assert(addExtsToSource(source, Set("arduino"), tokenizeSource).contains(expected))
  }

  test("adds extensions to existing directive with other poorly formatted directives") {
    val source = "extensions [ array ]\nturtles-own\n[\n]"
    val expected = "extensions [ arduino array ]\nturtles-own\n[\n]"

    assert(addExtsToSource(source, Set("arduino"), tokenizeSource).contains(expected))
  }

  test("rejects source with incomplete extensions directive") {
    val source = "extensions [ array \n"

    assert(addExtsToSource(source, Set("arduino"), tokenizeSource).isEmpty)
  }

  test("rejects source with nested globals directive") {
    val source = "extensions [ array globals [ a b c ] arduino ]"

    assert(addExtsToSource(source, Set("bitmap"), tokenizeSource).isEmpty)
  }

}
