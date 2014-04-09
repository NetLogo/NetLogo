// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.{ api, core }
import org.nlogo.util.SlowTest
import org.nlogo.workspace.Checksummer

class TestImportExport extends FixtureSuite with SlowTest {

  def getUniqueFilename() = {
    new java.io.File("tmp/TestImportExport").mkdir()
    val result = "tmp/TestImportExport/" + System.nanoTime + ".csv"
    delete(result)
    result
  }

  def delete(path: String): Boolean =
    new java.io.File(path).delete()

  def roundTripHelper(setup: String,
                      model: String = "",
                      worldSize: Int = 0)(implicit fixture: Fixture) {
    import fixture._
    val filename = getUniqueFilename()

    // get ready
    ModelCreator.open(workspace, core.WorldDimensions.square(worldSize), model)
    testCommand("random-seed 378234"); // just some number I made up

    // run the setup commands, run export-world, and slurp the resulting export into a string
    testCommand(setup)
    testCommand("export-world \"" + filename + "\"")
    val export1 = org.nlogo.api.FileIO.file2String(filename)

    // alter the state of the random number generator
    testCommand("repeat 500 [ __ignore random 100 ]")

    // reimport the export we just created
    testCommand("ca")
    testReporter("count turtles", "0")
    testCommand("import-world \"" + filename + "\"")
    assert(delete(filename))

    testCommand("export-world \"" + filename + "\"")

    // new slurp the second export into a string
    val export2 = org.nlogo.api.FileIO.file2String(filename)
    assert(delete(filename))

    // the two strings exports be equal except for the date
    assertResult(dropLines(export1, 3))(
      dropLines(export2, 3))
  }

  def dropLines(s: String, n: Int): String =
    io.Source.fromString(s).getLines.drop(n).mkString("\n")

  /// tests that use roundTripHelper

  test("RoundTripEmpty") { implicit fixture =>
    roundTripHelper("")
  }

  test("RoundTripTicks1") { implicit fixture =>
    roundTripHelper("reset-ticks")
  }

  test("RoundTripTicks2") { implicit fixture =>
    roundTripHelper("reset-ticks tick")
  }

  test("RoundTripTicks3") { implicit fixture =>
    roundTripHelper("reset-ticks tick clear-ticks")
  }

  test("RoundTripSimple") { implicit fixture =>
    roundTripHelper("crt 30 [ set heading who * 90 fd who ]",
      worldSize = 5)
  }

  test("RoundTripComplexNewFormat") { implicit fixture =>
    roundTripHelper("setup true", COMPLEX_SOURCE, worldSize = 3)
  }

  test("RoundTripSpecialCharacter") { implicit fixture =>
    // 8211 and 8212 are some arbitrary unicode values. we use numbers since we don't want non-ASCII
    // characters in the source files -- ST 2/14/07
    roundTripHelper("ask one-of patches [ set plabel \""
      + 8211.toChar
      + 8212.toChar
      + "\" ]")
  }

  test("RoundTripAllTurtlesAllPatches1") { implicit fixture =>
    roundTripHelper("set x turtles set y patches",
      "globals [x y]")
  }

  test("AgentsStoredInAgentVariables") { implicit fixture =>
    roundTripHelper("cro 4 [ create-links-with other turtles ]\n" +
      "ask turtle 0 [ set label one-of other turtles ]\n" +
      "ask turtle 1 [ set label one-of patches ]\n" +
      "ask turtle 2 [ set label sort turtles ]\n" +
      "ask turtle 3 [ set label sort patches ]")
  }

  test("BreededTurtlesStoredInAgentVariables") { implicit fixture =>
    roundTripHelper("create-ordered-mice 2 [ create-links-with other mice ]\n" +
      "ask turtle 0 [ set label one-of other mice ]\n" +
      "ask turtle 1 [ set label sort mice ]",
      "breed [mice mouse]")
  }

  // ticket #934
  test("LinksStoredInAgentVariables") { implicit fixture =>
    roundTripHelper("cro 2 [ create-links-with other turtles ]\n" +
      "ask turtle 0 [ set label one-of links ]\n" +
      "ask turtle 1 [ set label sort links]\n")
  }

  // more ticket #934
  test("BreededLinksStoredInAgentVariables") { implicit fixture =>
    roundTripHelper("cro 2 [ create-shipments-to other turtles ]\n" +
      "ask turtle 0 [ set label one-of shipments ]\n" +
      "ask turtle 1 [ set label sort shipments]\n",
      "directed-link-breed [shipments shipment]")
  }

  test("RoundTripAllTurtlesAllPatches2") { implicit fixture =>
    import fixture._
    // the bug we're testing for is elusive and may only appear if we actually change the world size
    // around - ST 12/21/04
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [x y]")
    testCommand("crt 10 set x turtles set y patches")
    testCommand("export-world \"" + filename + "\"")
    testCommand("resize-world -6 6 -6 6")
    testReporter("count patches", "169")
    testCommand("import-world \"" + filename + "\"")
    assert(delete(filename))
    testReporter("x = turtles", "true")
    testReporter("y = patches", "true")
  }

  test("ImportDrawing") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("random-seed 2843")
    testCommand("crt 10 [ pd  set pen-size random 5 ]")
    testCommand("ask turtles [ fd random 5 ]")
    val realColors = workspace.renderer.trailDrawer.colors
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    val importedColors = workspace.renderer.trailDrawer.colors

    assertResult(realColors.size)(importedColors.size)

    // right now we can't test the pixels because they are not pixel for pixel the same, what we
    // import is exactly as was, however, when we copy it for one image to the drawing image the
    // colors can change ever so slightly. (we copy images because of an apple bug, see the
    // TrailDrawer for details.  ev 3/1/06
  }

  test("ExportOutputArea") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("ca")
    testCommand("output-print \"This is a test of output areas.\"");
    testCommand("export-world \"" + filename + "\"")
    val expected = Checksummer.calculateWorldChecksum(workspace)
    testCommand("import-world \"" + filename + "\"")
    val actual = Checksummer.calculateWorldChecksum(workspace)
    assertResult(expected)(actual)
  }

  test("ExportLinks") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("ca")
    testCommand("create-ordered-turtles 2 [ fd 2 ]")
    testCommand("ask turtle 0 [ create-link-to turtle 1 [ tie ] ]")
    testCommand("export-world \"" + filename + "\"")
    val expected = Checksummer.calculateWorldChecksum(workspace)
    testCommand("import-world \"" + filename + "\"")
    val actual = Checksummer.calculateWorldChecksum(workspace)
    assertResult(expected)(actual)
    testReporter("count links", "1")
    testReporter("count turtles", "2")
    testReporter("[heading] of turtle 1", "180")
    testCommand("ask turtle 0 [ rt 180 ] ")
    testReporter("[heading] of turtle 1", "0")
  }

  test("ImportInvalidSize") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean =
          {
            assert(!fatalError)
            assertResult("Error Importing Drawing")(title)
            assertResult("Invalid data length, the drawing will not be imported")(
              errorDetails)
            true
          }
      }
    testCommand("import-world \"test/import/invalid-drawing.csv\"")
  }

  test("ImportDrawingIncompleteData") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean = {
          assert(!fatalError)
          assertResult("Error Importing Drawing")(title)
          assertResult("Invalid data length, the drawing will not be imported")(
            errorDetails)
          true
        }
      }
    testCommand("import-world \"test/import/short-drawing.csv\"")
  }

  test("ImportSubject") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("export-world \"" + filename + "\"")
    testCommand("import-world \"" + filename + "\"")
    testReporter("subject", "nobody")
    assertResult(workspace.world.observer().perspective)(api.Perspective.Observe)
    testCommand("crt 1")
    testCommand("watch turtle 0")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testReporter("[who] of subject", "0")
    assertResult(workspace.world.observer().perspective)(api.Perspective.Watch)
    testCommand("crt 1")
    testCommand("follow turtle 1")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testReporter("[who] of subject", "1")
    assertResult(workspace.world.observer().perspective)(api.Perspective.Follow)
    testCommand("ride turtle 1")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testReporter("[who] of subject", "1")
    assertResult(workspace.world.observer().perspective)(api.Perspective.Ride)
  }

  test("NonExistentPlot") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String, fatalError: Boolean) = {
          assert(!fatalError)
          assertResult("Error Importing Plots")(title)
          assertResult("The plot \"plot 2\" does not exist.")(
            errorDetails)
          true
        }
      }
    testCommand("import-world \"test/import/plot-simple.csv\"")
  }

  test("NonExistentPen") { implicit fixture =>
    import fixture._
    workspace.open("test/import/plot-simple.nlogo")
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String,
                      fatalError: Boolean) =
          {
            assert(!fatalError)
            assertResult("Error Importing Plots")(title)
            assertResult("The pen \"default 1\" does not exist.")(errorDetails)
            true
          }}
    testCommand("import-world \"plot-simple.csv\"")
  }

  test("CustomPenColor") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    workspace.open("test/import/plot-custom-color.nlogo")
    testCommand("export-world \"../../" + filename + "\"")
    val export1 = org.nlogo.api.FileIO.file2String(filename)
    testCommand("ca import-world \"../../" + filename + "\"")
    testCommand("export-world \"../../" + filename + "\"")
    val export2 = org.nlogo.api.FileIO.file2String(filename)
    assertResult(dropLines(export1, 3))(
      dropLines(export2, 3))
  }

  test("ImportingTurtlesDying") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("crt 10")
    testCommand("ask turtle 9 [ die ]")
    testCommand("ask turtle 5 [ die ]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testCommand("ask turtle 4 [ die ]")
    testCommand("crt 2")
    testReporter("sort [who] of turtles", "[0 1 2 3 6 7 8 10 11]")
  }

  test("ImportingTables") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [table1 table2] extensions [ table ]")
    testCommand("set table1 table:from-list [[1 2] [\"word\" \"value\"] [3 false]]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testReporter("table:to-list table1", "[[1 2] [\"word\" \"value\"] [3 false]]")
  }

  test("ImportingTablesSameTable") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [table1 table2] extensions [ table ]")
    testCommand("set table1 table:from-list [[1 2] [3 4] [4 5]]")
    testCommand("set table2 table1")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testCommand("table:put table1 7 8")
    testReporter("table:to-list table1", "[[1 2] [3 4] [4 5] [7 8]]")
    testReporter("table:to-list table2", "[[1 2] [3 4] [4 5] [7 8]]")
  }

  test("ImportingTablesTwoTables") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [table1 table2] extensions [ table ]")
    testCommand("set table1 table:from-list [[1 2] [3 4] [4 5]]")
    testCommand("set table2 table:from-list [[1 2] [3 4] [4 5]]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"");
    testCommand("table:put table1 7 8")
    testReporter("table:to-list table1", "[[1 2] [3 4] [4 5] [7 8]]")
    testReporter("table:to-list table2", "[[1 2] [3 4] [4 5]]")
  }

  test("ImportingArrays") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [ar1 ar2] extensions [ array ]")
    testCommand("set ar1 array:from-list [1 2 3 4 \"string\" false]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testReporter("array:to-list ar1", "[1 2 3 4 \"string\" false]")
  }

  test("ImportingArraysSameArray") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [ar1 ar2] extensions [ array ]")
    testCommand("set ar1 array:from-list [ 1 2 3 ]")
    testCommand("set ar2 ar1")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testCommand("array:set ar1 2 4")
    testReporter("array:to-list ar2", "[1 2 4]");
  }

  test("ImportingArraysTwoArrays") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [ar1 ar2] extensions [ array ]")
    testCommand("set ar1 array:from-list [ 1 2 3 ]")
    testCommand("set ar2 array:from-list [ 1 2 3 ]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testCommand("array:set ar1 2 4")
    testReporter("array:to-list ar1", "[1 2 4]");
    testReporter("array:to-list ar2", "[1 2 3]");
  }

  test("ImportingArraysAndTables") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(5), "globals [ar1 ar2 t1 t2] extensions [ array table ]")
    testCommand("set ar1 array:from-list [ 1 2 3 ]")
    testCommand("set ar2 array:from-list [ 1 2 3 ]")
    testCommand("set t1 table:from-list [ [1 2] [3 4]]")
    testCommand("set t2 table:from-list [ [1 2] [3 4]]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("ca")
    testCommand("import-world \"" + filename + "\"")
    testCommand("array:set ar1 2 4")
    testReporter("array:to-list ar1", "[1 2 4]");
    testReporter("array:to-list ar2", "[1 2 3]")
    testCommand("table:put t1 7 8")
    testReporter("table:to-list t1", "[[1 2] [3 4] [7 8]]")
    testReporter("table:to-list t2", "[[1 2] [3 4]]")
  }

  /// other tests (that don't use roundTripHelper)

  test("TrailingCommas") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(35),
      org.nlogo.api.FileIO.file2String("test/import/trailing-commas.nlogo"))
    testCommand("import-world \"test/import/trailing-commas.csv\"")
  }

  test("ImportWrongOrder") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String, fatalError: Boolean) = {
            assert(fatalError)
            assertResult("Fatal Error- Incorrect Structure For Import File")(title)
            assertResult("The agents are in the wrong order in the import file. " +
              "The global variables should be first, followed by the turtles, " +
              "followed by the patches.  Found TURTLES but needed " +
              "GLOBALS\n\nThe import will now abort.")(errorDetails)
            true
          }}
    testCommand("import-world \"test/import/wrong-order.csv\"")
  }

  test("ImportSentinelName") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(10))
    testCommand("import-world \"test/import/TURTLES.csv\"")
  }

  test("ExtraFieldValue") { implicit fixture =>
    import fixture._
    ModelCreator.open(workspace, core.WorldDimensions.square(35),
      org.nlogo.api.FileIO.file2String("test/import/trailing-commas.nlogo"))
    val errorNumber = Array(0)
    workspace.importerErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler() {
        def showError(title: String, errorDetails: String, fatalError: Boolean) = {
          assert(!fatalError)
          assertResult("Warning: Too Many Values For Agent")(title)
          errorNumber(0) match {
            case 0 =>
              assertResult("Error Importing at Line 7: There are a total of "
                + "10 Global variables declared in this model "
                + "(including built-in variables).  The import-world "
                + "file has at least one agent in the GLOBALS section "
                + "with more than this number of values.\n\n"
                + "Action to be Taken: All the extra values will "
                + "be ignored for this section.")(
                errorDetails)
            case 1 =>
              assertResult("Error Importing at Line 54: There are a total of "
                + "5 Patch variables declared in this model "
                + "(including built-in variables).  The import-world "
                + "file has at least one agent in the PATCHES section "
                + "with more than this number of values.\n\n"
                + "Action to be Taken: All the extra values will "
                + "be ignored for this section.")(
                errorDetails)
            case _ =>
              fail()
          }
          errorNumber(0) += 1
          true
        }
      }
    testCommand("import-world \"test/import/extra-values.csv\"")
    assertResult(2)(errorNumber(0))
  }

  // this is a focused test with a small number of turtles
  // designed to catch one particular known bug
  test("ReproducibilityOfWhoNumberAssignment1") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(0))
    testCommand("crt 4")
    testCommand("ask turtle 0 [ die ]")
    testCommand("ask turtle 2 [ die ]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("crt 1")
    testReporter("sort [who] of turtles", "[1 3 4]")
    testCommand("import-world \"" + filename + "\"")
    testCommand("crt 1")
    testReporter("sort [who] of turtles", "[1 3 4]")
    assert(delete(filename))
  }

  // this is a focused test with a small number of turtles
  // designed to catch one particular known bug
  test("ReproducibilityOfWhoNumberAssignment2") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(0))
    testCommand("crt 4")
    testCommand("ask turtle 2 [ die ]")
    testCommand("ask turtle 0 [ die ]")
    testCommand("export-world \"" + filename + "\"")
    testCommand("crt 1")
    testReporter("sort [who] of turtles", "[1 3 4]")
    testCommand("import-world \"" + filename + "\"")
    testCommand("crt 1")
    testReporter("sort [who] of turtles", "[1 3 4]")
    assert(delete(filename))
  }

  // this is a less focused test with lots of turtles
  // that might hopefully catch new bugs
  test("ReproducibilityOfWhoNumberAssignment3") { implicit fixture =>
    import fixture._
    val filename = getUniqueFilename()
    ModelCreator.open(workspace, core.WorldDimensions.square(0))
    testCommand("crt 500")
    testCommand("ask turtles [ if random 2 = 0 [ die ] ]")
    testCommand("export-world \"" + filename + "\"");
    testCommand("crt 100")
    val actualResult = workspace.report("sum [who] of turtles")
    testCommand("import-world \"" + filename + "\"")
    testCommand("crt 100")
    val newResult = workspace.report("sum [who] of turtles")
    assertResult(actualResult)(newResult)
    assert(delete(filename))
  }

  test("utf 8 string") { implicit fixture =>
    val x = new String("A" + "\u00ea" + "\u00f1" + "\u00fc" + "C")
    roundTripHelper(setup="set t \"" + x + "\"", model="globals [t]")
  }

  ///

  val COMPLEX_SOURCE =
    "globals [ g-string-test g-list-test nobody-var one-turtle one-patch " +
      "          empty-patch-agentset empty-turtle-agentset cats-breed all-turtles all-patches ]" +
      "turtles-own [ t-foo t-bar ]" +
      "patches-own [ p-foo p-fish-breed ]" +
      "breed [ fish ] " +
      "breed [ cats ]" +
      "fish-own [ scales? fins ]" +
      "cats-own [ fur tabby? ]" +
      "to setup [ add-breed-to-list? ]" +
      "  ca" +
      "  crt 4" +
      "  [" +
      "    set t-foo \"just, a, string\\\\ with, funny stuff\\t\\\"in it\"" +
      "    set t-bar list [] [ true false \"hithere, how's it going?\" 4 10.2 ]" +
      "  ]" +
      "  ask turtle 0" +
      "  [" +
      "    setxy 7 7" +
      "    set breed fish" +
      "    set scales? true" +
      "    set fins 20.5" +
      "  ]" +
      "  ask turtle 1" +
      "  [" +
      "    setxy 14 14" +
      "    set breed cats" +
      "    set color blue" +
      "    set fur \"thick,and,well groomed\"" +
      "    set tabby? false" +
      "    fd 1" +
      "  ]" +
      "  ask turtle 2" +
      "  [" +
      "    setxy 6 5" +
      "    set label \"15,4,5,\" " +
      "  ]" +
      "  ask turtle 3" +
      "  [" +
      "    setxy 14 11" +
      "  ]" +
      "  set g-string-test \"8\\\\the\\tchili\\nand it\\rwas \\\"good\\\",,,,, by golly!! {patches [2 2] [-2 1] [-1 1] [2 0] [0 -2]},[{patches [-2 2] [2 2] [0 0] [2 -2] [1 -3]} {turtles 1 3}]\"" +
      "  set g-list-test lput g-string-test lput (turtle-set turtle 0 turtle 2 turtle 3) lput (patch-set patch 3 2 patch -3 1 patch -1 0 patch 2 0 patch -3 -3) fput (turtle 1) list [] (patch 0 0)" +
      "  " +
      "  ;; we don't want to add the fish breed if we are importing the\n " +
      "  ;; old format since it couldn't support breeds in lists.\n" +
      "  if add-breed-to-list?" +
      "  [ set g-list-test lput fish g-list-test ]" +
      "  " +
      "  set nobody-var nobody" +
      "  set one-patch (patch 0 0)" +
      "  set one-turtle (turtle 0)" +
      "  set empty-patch-agentset patches with [ pcolor = 139.2356 ]" +
      "  set empty-turtle-agentset turtles with [ who > 100000 ]" +
      "  set cats-breed cats" +
      "  set all-turtles turtles" +
      "  set all-patches patches" +
      "  ask patch -3 3" +
      "  [" +
      "    set plabel-color yellow" +
      "    set plabel 25" +
      "  ]" +
      "  ask patches with [ any? turtles-here ]" +
      "  [" +
      "    set pcolor yellow" +
      "    set p-foo nobody-var" +
      "    set p-fish-breed fish" +
      "  ]" +
      "end"
}
