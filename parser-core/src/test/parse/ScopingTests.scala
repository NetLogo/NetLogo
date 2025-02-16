// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.CompilerException


import FrontEndTests.extensionManager
import org.scalatest.funsuite.AnyFunSuite

class ScopingTests extends AnyFunSuite with BaseParserTest {
  def duplicateName(s: String, err: String) = {
    val e = intercept[CompilerException] {
      FrontEnd.frontEnd(s, extensionManager = extensionManager)
    }
    assertResult(err)(e.getMessage)
  }
  test("lambda argument shadows primitive name") {
    runFailure("__ignore [[turtles] -> 2]", "There is already a primitive reporter called TURTLES", 11, 18)
  }
  test("unbracketed lambda argument shadows primitive name") {
    runFailure("__ignore [turtles -> 2]", "There is already a primitive reporter called TURTLES", 10, 17)
  }
  test("lambda argument duplicate name") {
    runFailure("__ignore [[bar bar] -> 2]", "There is already a local variable here called BAR", 15, 18)
  }
  test("lambda argument duplicate nested name") {
    runFailure("__ignore [[bar] -> [[bar] -> 2]]", "There is already a local variable here called BAR", 21, 24)
  }
  test("unbracketed lambda argument duplicate nested name") {
    runFailure("__ignore [bar -> [bar -> 2]]", "There is already a local variable here called BAR", 18, 21)
  }
  test("lambda argument shadows local variable") {
    runFailure("let baz 7 __ignore [[baz] -> 3]", "There is already a local variable here called BAZ", 21, 24)
  }
  test("unbracketed lambda argument shadows local variable") {
    runFailure("let baz 7 __ignore [baz -> 3]", "There is already a local variable here called BAZ", 20, 23)
  }
  test("lambda argument shadows procedure name") {
    runFailure("let baz [[baz] -> 3]", "There is already a local variable here called BAZ", 10, 13)
  }
  test("unbracketed lambda argument shadows procedure name") {
    runFailure("let baz [baz -> 3]", "There is already a local variable here called BAZ", 9, 12)
  }
  test("lambda argument shadows procedure variable") {
    runFailure("__ignore [[bar] -> 2]", "There is already a local variable here called BAR", 11, 14, "to foo [bar] ")
  }
  // https://github.com/NetLogo/NetLogo/issues/348
  test("let of lambda variable") {
    runFailure("foreach [1] [[x] -> let x 0 ]", "There is already a local variable here called X", 24, 25)
  }
  test("LetSameNameAsCommandProcedure2") {
    duplicateName("to b let a 5 end  to a end",
      "There is already a procedure called A")
  }
  test("LetSameNameAsReporterProcedure2") {
    duplicateName("to b let a 5 end  to-report a report 10 end",
      "There is already a procedure called A")
  }
  test("LetNameSameAsEnclosingCommandProcedureName") {
    duplicateName("to bazort let bazort 5 end",
      "There is already a procedure called BAZORT")
  }
  test("LetNameSameAsEnclosingReporterProcedureName") {
    duplicateName("to-report bazort let bazort 5 report bazort end",
      "There is already a procedure called BAZORT")
  }
  test("LetNameSameAsBreedVariableName") {
    duplicateName("breed [mice mouse] mice-own [fur] to foo let fur 5 end",
      "There is already a MICE-OWN variable called FUR")
  }
  test("BreedDuplicateName") {
    duplicateName("breed [xs xs]",
      "There is already a breed called XS")
  }
  test("BreedOnlyOneName") {
    duplicateName("breed [xs]",
      "Breed declarations must have plural and singular. BREED [XS] has only one name.")
  }
  test("LinkBreedOnlyOneName") {
    duplicateName("directed-link-breed [xs]",
      "Breed declarations must have plural and singular. DIRECTED-LINK-BREED [XS] has only one name.")
  }
  test("BreedPrimSameNameAsBuiltInPrim") {
    duplicateName("breed [strings string]",
      "Defining a breed [STRINGS STRING] redefines IS-STRING?, a primitive reporter")
  }
  test("BreedPrimSameAsProcedure") {
    duplicateName("breed [mice mouse] to-report mice-at report nobody end",
      "There is already a breed reporter called MICE-AT")
  }
  test("SameLocalVariableTwice1") {
    duplicateName("to a1 locals [b b] end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice2") {
    duplicateName("to a2 [b b] end",
      "There is already a local variable called B here")
  }
  test("SameLocalVariableTwice3") {
    duplicateName("to a3 let b 5 let b 6 end",
      "There is already a local variable here called B")
  }
  test("SameLocalVariableTwice4") {
    duplicateName("to a4 locals [b] let b 5 end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice5") {
    duplicateName("to a5 [b] locals [b] end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice6") {
    duplicateName("to a6 [b] let b 5 end",
      "There is already a local variable here called B")
  }
  test("SameNameAsExtensionPrim") {
    duplicateName("to foo:bar end",
      "There is already an extension command called FOO:BAR")
  }
}
