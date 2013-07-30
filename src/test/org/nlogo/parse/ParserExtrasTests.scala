// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm }

class ParserExtrasTests extends FunSuite {

  /// helpers

  val src = "globals [glob1] " +
    "to foo end to-report bar [] report 5 end"

  // It's a bit depressing we have to go through the rigmarole of making
  // dummy Procedure objects, but isReporter requires us to supply a
  // ProceduresMap. - ST 7/17/13
  val (proceduresMap, program) = {
    def dummyProcedure(p: ProcedureDefinition) =
      new nvm.Procedure(p.procedure.isReporter, p.procedure.name,
        api.Token.Eof, Seq())
    val (procedures, structureResults) = Parser.frontEnd(src)
    val proceduresMap =
      collection.immutable.ListMap(
        procedures.map(p => p.procedure.name -> dummyProcedure(p)): _*)
    (proceduresMap, structureResults.program)
  }

  def isReporter(s: String) =
    (Parser: ParserExtras).isReporter(s, program,
      proceduresMap, new api.DummyExtensionManager)

  /// tests for isReporter

  val reporters = Seq("3", "[]", "[", "((5))", "timer", "glob1", "bar")
  val nonReporters = Seq("", ";", " ; ", "ca", "((ca))", "foo",
                         "5984783478344387487348734", "gkhjfghkjfhjkg")

  for(x <- reporters)
    test("is a reporter: '" + x + "'") {
      assert(isReporter(x))
    }
  for(x <- nonReporters)
    test("isn't a reporter: '" + x + "'") {
      assert(!isReporter(x))
    }

}
