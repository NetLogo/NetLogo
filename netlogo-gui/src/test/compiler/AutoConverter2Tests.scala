// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.core.Breed
import org.nlogo.nvm.DummyWorkspace

class AutoConverter2Tests extends FunSuite {
  val workspace = new DummyWorkspace
  val p = workspace.world.program
  val newProgram = p.copy(
    interfaceGlobals = p.interfaceGlobals :+ "GLOB1",
    breeds = p.breeds + ("FROGS" -> Breed("FROGS", "FROG")))
  workspace.world.program(newProgram)
  def tester(version: String, before: String, after: String, subprogram: Boolean = true) {
    val converter = new AutoConverter2(workspace, false)(Compiler.Tokenizer2D)
    assertResult(after)(converter.convert(before, subprogram, false, "NetLogo " + version))
  }
  test("valuesFrom0") {
    tester("3.1", "print values-from patches [5]", "print [5] of patches") }
  test("valuesFrom1a") {
    tester("3.1", "print values-from (patches) [5]", "print [5] of (patches)") }
  test("valuesFrom1b") {
    tester("3.1", "print values-from patches[5]", "print [5] of patches") }
  test("valuesFrom2") {
    tester("3.1", "if empty? values-from patches [5] [ ]", "if empty? [5] of patches [ ]") }
  test("valuesFrom3") {
    tester("3.1", "if max values-from patches [5] > 15 [ ]", "if max [5] of patches > 15 [ ]") }
  test("valueFrom1") {
    tester("3.1", "print value-from current_dest [color]", "print [color] of current_dest") }
  test("valuesFrom4") { // check it doesn't mess up stuff it can't handle - ST 2/20/07
    tester("3.1", "print value-from one-of patches [value-from one-of patches [one-of neighbors]]",
      "print [value-from one-of patches [one-of neighbors]] of one-of patches")
  }
  test("histogramFrom") {
    tester("3.1", "histogram-from turtles [xcor]", "histogram [xcor] of turtles") }
  test("randomIntOrFloat1") {
    tester("3.1", "print random-or-random-float 5", "print random 5") }
  test("randomIntOrFloat2") {
    tester("3.1", "print random-or-random-float 5.0", "print random-float 5.0") }
  test("randomIntOrFloat3") {
    tester("3.1", "print random-or-random-float glob1", "print random-or-random-float glob1") }
}
