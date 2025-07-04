// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.scalatest.funsuite.AnyFunSuite

import LibrariesTab.addExtsToSource

class LibrariesTabTests extends AnyFunSuite {
  test("adds new extensions statement to empty source") {
    val source = ""
    val requiredExts = Set("fetch")
    val expected = "extensions [fetch]\n"
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }

  test("adds new extensions statement to code without one already") {
    val source = "to go end"
    val requiredExts = Set("fetch")
    val expected =
      """extensions [fetch]
        |to go end""".stripMargin
    assertResult(expected.split("\r?\n").sameElements(addExtsToSource(source, requiredExts)))
  }

  test("adds extensions to existing simple extensions statement") {
    val source =
      """extensions [array]
        |to go end""".stripMargin
    val requiredExts = Set("fetch", "import-a")
    val expected =
      """extensions [fetch import-a array]
        |to go end""".stripMargin
    assertResult(expected)(addExtsToSource(source, requiredExts))
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
        |[fetch import-a ;ignore
        |array ;ignore ]
        |]
        |;ignore
        |to go end""".stripMargin
    assertResult(expected)(addExtsToSource(source, requiredExts))
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
        |[fetch import-a  ; ignore
        |  array ; ignore ]
        |]
        |; ignore
        |to go
        |  show []
        |end""".stripMargin
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }

}
