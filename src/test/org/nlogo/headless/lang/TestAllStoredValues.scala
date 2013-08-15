// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api,
  api.{ Observer, Turtle, Patch, Link, LogoList }

class TestAllStoredValues extends FixtureSuite {
  test("all agents included") { implicit fixture =>
    fixture.testCommand("crt 10 [ create-links-with other turtles ]")
    val world = fixture.workspace.world
    val vals = world.allStoredValues.toStream
    assertResult(1) {
      vals.collect{case o: Observer => o}.size
    }
    // squared because the turtles also appear in the end1 and end2 variables of the links
    assertResult(world.turtles.count * world.turtles.count) {
      vals.collect{case t: Turtle => t}.size
    }
    assertResult(world.patches.count) {
      vals.collect{case p: Patch => p}.size
    }
    assertResult(world.links.count) {
      vals.collect{case l: Link => l}.size
    }
  }
  test("sample agent variable included") { implicit fixture =>
    val world = fixture.workspace.world
    assert(!world.allStoredValues.contains("foo"))
    fixture.testCommand("ask one-of patches [ set plabel \"foo\" ]")
    assert(world.allStoredValues.contains("foo"))
  }
  test("inside nested list") { implicit fixture =>
    fixture.testCommand("ask one-of patches [ set plabel [[[[\"foo\"]]]] ]")
    val vals = fixture.workspace.world.allStoredValues.toStream
    assertResult(4) {
      vals.collect{case l: LogoList => l}.size
    }
    assertResult(1) {
      vals.collect{case "foo" => true}.size
    }
  }
}
