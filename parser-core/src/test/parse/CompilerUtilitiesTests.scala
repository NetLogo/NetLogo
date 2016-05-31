// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.core._,
  StructureDeclarations.Procedure

class CompilerUtilitiesTests extends FunSuite {

  /// helpers

  val src = "globals [glob1] " +
    "to foo end to-report bar [] report 5 end"

  val (proceduresMap, program) = {
    val (procedures, structureResults) = FrontEnd.frontEnd(src)
    val proceduresMap =
      collection.immutable.ListMap(
        procedures.map { p => p.procedure.name -> p.procedure }: _*)
    (proceduresMap, structureResults.program)
  }

  // this null isn't ideal, but as long as this only tests .isReporter, it doesn't really affect anything
  def compilerUtilities: CompilerUtilitiesInterface = CompilerUtilities

  def isReporter(s: String) =
    compilerUtilities.isReporter(s, program,
      proceduresMap, new DummyExtensionManager)

  /// tests for isReporter

  val reporters = Seq("3", "[]", "[", "((5))", "timer", "glob1", "bar")
  val nonReporters = Seq("", ";", " ; ", "ca", "((ca))", "foo",
                         "5984783478344387487348734", "notaprim")

  for(x <- reporters)
    test("is a reporter: '" + x + "'") {
      assert(isReporter(x))
    }
  for(x <- nonReporters)
    test("isn't a reporter: '" + x + "'") {
      assert(!isReporter(x))
    }
}
