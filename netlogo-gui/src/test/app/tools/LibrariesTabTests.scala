// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.scalatest.FunSuite

import LibrariesTab.addExtsToSource

class LibrariesTabTests extends FunSuite {
  test("adds new extensions statement to empty source") {
    val source = ""
    val requiredExts = Set("fetch")
    val expected = "extensions [fetch]\n"
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }

  test("adds new extensions statement to code without one already") {
    val source = "to go end"
    val requiredExts = Set("fetch")
    val expected = "extensions [fetch]\nto go end"
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }

  test("adds extensions to existing simple extensions statement") {
    val source = "extensions [array]\nto go end"
    val requiredExts = Set("fetch", "import-a")
    val expected = "extensions [fetch import-a array]\nto go end"
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }

  test("adds extensions to existing complex extensions statement") {
    val source = "; ignore\n   extensions ;ignore [\n;ignore\n[;ignore\narray ;ignore ]\n]\n;ignore\nto go end"
    val requiredExts = Set("fetch", "import-a")
    val expected = "; ignore\n   extensions ;ignore [\n;ignore\n[fetch import-a ;ignore\narray ;ignore ]\n]\n;ignore\nto go end"
    assertResult(expected)(addExtsToSource(source, requiredExts))
  }
}
