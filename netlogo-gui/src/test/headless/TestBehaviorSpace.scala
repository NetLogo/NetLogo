// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{ OneInstancePerTest, BeforeAndAfterEach }
import org.nlogo.api.{ FileIO, LabPostProcessorInputFormat, Version }
import org.nlogo.nvm.{ LabInterface, Workspace }
import org.nlogo.util.SlowTest

class TestBehaviorSpace extends AnyFunSuite with SlowTest
with OneInstancePerTest with BeforeAndAfterEach {

  val TestProtocolsFilePath = "test/lab/protocols.xml"

  val workspaces = new collection.mutable.ListBuffer[HeadlessWorkspace]
  def newWorkspace() = {
    val w = HeadlessWorkspace.newInstance
    workspaces += w
    w
  }

  def newWorker(name: String): LabInterface.Worker = {
    val protocol =
      BehaviorSpaceCoordinator.externalProtocols(TestProtocolsFilePath)
        .flatMap(_.find(_.name == name))
        .getOrElse(throw new Exception(s"Invalid protocol: $name"))
    HeadlessWorkspace.newLab.newWorker(protocol)
  }

  override def beforeEach(): Unit = { }
  override def afterEach(): Unit = { workspaces.foreach(_.dispose()) }

  // first 6 lines of results are header lines. we'll need to discard them
  def withoutFirst6Lines(s: String) =
    s.split("\n").drop(6).mkString("", "\n", "\n")
  def slurp(path: String) =
    stripLineFeeds(FileIO.fileToString(path))
  def stripLineFeeds(s: String) =
    s.replaceAll("\r\n", "\n")

  def run2DExperiment(minX: Int, maxX: Int, minY: Int, maxY: Int, declarations: String, name: String) = {
    val workspace = newWorkspace()
    workspace.initForTesting(minX, maxX, minY, maxY, declarations)
    run("test/lab/" + name)(() => workspace, () => newWorker(name))
    workspace
  }
  def runExperiment(worldSize: Int, declarations: String, name: String,
                    wantTable: Boolean = true, wantStats: Boolean = false, wantLists: Boolean = false) = {
    val workspace = newWorkspace()
    workspace.initForTesting(worldSize, declarations)
    run("test/lab/" + name, wantTable = wantTable,
      wantStats = wantStats, wantLists = wantLists)(() => workspace, () => newWorker(name))
    workspace
  }
  def run3DExperiment(name: String): Unit = {
    val workspace = newWorkspace()
    workspace.initForTesting(0)
    run("test/lab/" + name)(() => workspace, () => newWorker(name))
  }
  def runParallelExperiment(name: String, declarations: String = ""): Unit = {
    def workspace = {
      val w = newWorkspace()
      w.initForTesting(0, declarations)
      w
    }
    // only get spreadsheet results, since parallel table results are in scrambled order - ST 3/4/09
    run("test/lab/" + name, wantTable = false,
        threads = Runtime.getRuntime.availableProcessors)(
        () => workspace, () => newWorker(name))
  }
  def runExperimentFromModel(modelPath: String, experimentName: String, filename: String, threads: Int = 1,
  wantSpreadsheet: Boolean = true, wantTable: Boolean = true, wantStats: Boolean = false, wantLists: Boolean = false): Unit = {
    val time = System.nanoTime
    new java.io.File("tmp").mkdir()
    new java.io.File("tmp/TestBehaviorSpace").mkdir()
    val tablePath = "tmp/TestBehaviorSpace/" + time + "-table.csv"
    val spreadsheetPath = "tmp/TestBehaviorSpace/" + time + "-spreadsheet.csv"
    val statsPath = "tmp/TestBehaviorSpace/" + time + "-stats.csv"
    val listsPath = "tmp/TestBehaviorSpace/" + time + "-lists.csv"
    // let's go through headless.Main here so that code gets some testing - ST 3/9/09
    if (wantStats) {
      if (wantTable && wantSpreadsheet) {
        Main.main(Array("--model", modelPath, "--experiment", experimentName,
                "--table", tablePath, "--spreadsheet", spreadsheetPath, "--stats", statsPath, "--lists", listsPath,
                "--threads", threads.toString, "--suppress-errors"))
      } else if (wantTable) {
        Main.main(Array("--model", modelPath, "--experiment", experimentName,
                "--table", tablePath, "--stats", statsPath, "--lists", listsPath,
                "--threads", threads.toString, "--suppress-errors"))
      } else if (wantSpreadsheet) {
        Main.main(Array("--model", modelPath, "--experiment", experimentName,
                "--spreadsheet", spreadsheetPath, "--stats", statsPath, "--lists", listsPath,
                "--threads", threads.toString, "--suppress-errors"))
      }
    } else {
        Main.main(Array("--model", modelPath, "--experiment", experimentName,
                  "--table", tablePath, "--spreadsheet", spreadsheetPath, "--stats", statsPath, "--lists", listsPath,
                  "--threads", threads.toString, "--suppress-errors"))
    }
    if (!wantStats && wantTable)
      assertResult(slurp(filename + "-table.csv"))(
        withoutFirst6Lines(slurp(tablePath)))
    if (!wantStats && wantSpreadsheet)
      assertResult(slurp(filename + "-spreadsheet.csv"))(
        withoutFirst6Lines(slurp(spreadsheetPath)))
    if (wantStats)
      assertResult(slurp(filename + "-stats.csv"))(
        withoutFirst6Lines(slurp(statsPath)))
    if (wantLists)
      assertResult(slurp(filename + "-lists.csv"))(
        withoutFirst6Lines(slurp(listsPath)))
  }
  // sorry this has gotten so baroque with all the closures and tuples and
  // whatnot. it should be redone - ST 8/19/09
  def run(filename: String, threads: Int = 1, wantTable: Boolean = true, wantSpreadsheet: Boolean = true,
          wantStats: Boolean = false, wantLists: Boolean = false)
          (fn: () => Workspace, fn2: () => LabInterface.Worker): Unit = {
    val dims = fn.apply().world.getDimensions
    def runHelper(fns: List[(String, (LabInterface.Worker, java.io.StringWriter) => Unit)]): Unit = {
      val worker = fn2.apply()
      val writers = fns.map(_ => new java.io.StringWriter)
      for (((_, fn), writer) <- fns zip writers)
        fn(worker, writer)
      worker.run(fn.apply(), fn, threads)
      for (((suffix, _), writer) <- fns zip writers) {
        val resultsPath = filename + suffix
        withClue(resultsPath) {
          // Compare up to 2 decimal places for numbers used in statistics to avoid rounding issues
          if (suffix == "-stats.csv") {
            val output = withoutFirst6Lines(stripLineFeeds(writer.toString))
            var i = 0
            while (i < output.length) {
              if (output(i).isDigit) {
                while (output(i) != ' ' && output(i) != '"' && output(i) != ']') {
                  i += 1
                }
              }
              i += 1
            }
          } else {
            assertResult(slurp(resultsPath))(
            withoutFirst6Lines(stripLineFeeds(writer.toString)))
          }
        }
      }
    }

    def table(worker: LabInterface.Worker, writer: java.io.StringWriter): Unit = {
      worker.addTableWriter(filename, dims, new java.io.PrintWriter(writer))
    }
    def spreadsheet(worker: LabInterface.Worker, writer: java.io.StringWriter): Unit = {
      worker.addSpreadsheetWriter(filename, dims, new java.io.PrintWriter(writer))
    }
    def stats(worker: LabInterface.Worker, writer: java.io.StringWriter): Unit = {
      if (wantTable || wantSpreadsheet) {
        worker.addStatsWriter(filename, dims, new java.io.PrintWriter(writer), {
          if (wantTable) LabPostProcessorInputFormat.Table(filename + "-table.csv")
          else LabPostProcessorInputFormat.Spreadsheet(filename + "-spreadsheet.csv")
        })
      }
    }
    def lists(worker: LabInterface.Worker, writer: java.io.StringWriter): Unit = {
      if (wantTable) {
        worker.addListsWriter(filename, dims, new java.io.PrintWriter(writer),
                              LabPostProcessorInputFormat.Table(filename + "-table.csv"))
      }
      else if (wantSpreadsheet)
      {
        worker.addListsWriter(filename, dims, new java.io.PrintWriter(writer),
                              LabPostProcessorInputFormat.Spreadsheet(filename + "-spreadsheet.csv"))
      }
    }
    if (wantStats) {
      runHelper(List(("-stats.csv", stats)))
    } else {
      runHelper(List(("-table.csv", table), ("-spreadsheet.csv", spreadsheet), ("-lists.csv", lists))
            .filter {
              case (suffix, _) =>
                suffix == "-table.csv" && wantTable ||
                  suffix == "-spreadsheet.csv" && wantSpreadsheet ||
                    suffix == "-lists.csv" && wantLists
            })
    }
  }

  test("BehaviorSpace1", SlowTest.Tag) {
    runExperiment(0, "globals [param1 param2 counter]",
      "testBehaviorSpace1")
  }
  test("BehaviorSpace2", SlowTest.Tag) {
    runExperiment(0, "globals [param1 param2 counter]",
      "testBehaviorSpace2")
  }
  test("MultipleMetrics", SlowTest.Tag) {
    runExperiment(0, "globals [counter param1]",
      "testMultipleMetrics")
  }
  test("NoMetrics1", SlowTest.Tag) {
    runExperiment(0, "globals [counter param1]",
      "testNoMetrics1")
  }
  test("NoMetrics2", SlowTest.Tag) {
    runExperiment(0, "globals [counter param1]",
      "testNoMetrics2")
  }
  test("ImmediateExit", SlowTest.Tag) {
    val workspace =
      runExperiment(0, "globals [counter foo]",
        "testImmediateExit")
    assertResult(Double.box(99))(
      workspace.report("foo"))
  }
  test("CarryoverBetweenRuns", SlowTest.Tag) {
    val workspace = newWorkspace()
    workspace.initForTesting(0, "globals [foo]")
    // no setup commands, so foo doesn't get reset
    newWorker("testCarryover")
      .run(workspace, () => workspace, 1)
    assertResult(Double.box(20))(
      workspace.report("foo"))
  }
  test("ResizingWorld1", SlowTest.Tag) {
    runExperiment(0, "", "testResizingWorld1")
  }
  test("ResizingWorld2", SlowTest.Tag) {
    runExperiment(0, "", "testResizingWorld2")
  }
  test("SettingRandomSeed", SlowTest.Tag) {
    runExperiment(0, "", "testRandomSeed")
  }
  test("RunNumber", SlowTest.Tag) {
    val workspace = runExperiment(0, "", "runNumber")
    // I suppose we could reset the run number to 0 after a run, and we do that in the GUI, but I
    // can't see a reason to ensure it headless - ST 7/7/10
    assertResult(Double.box(3))(
      workspace.report("behaviorspace-run-number"))
  }
  test("ExperimentName", SlowTest.Tag) {
    val workspace = runExperiment(0, "", "runNumber")
    assertResult("runNumber")(
      workspace.report("behaviorspace-experiment-name"))
  }
  // test export-graphics in headless mode
  if (!Version.is3D)
    test("ExportGraphics", SlowTest.Tag) {
      val workspace = newWorkspace()
      workspace.open("models/test/lab/FireWithExperiments.nlogox")
      newWorker("testExportGraphics")
        .run(workspace, () => workspace, 1)
    }
  if (!Version.is3D)
    test("ModelWithIncludedExperiments", SlowTest.Tag) {
      runExperimentFromModel("test/lab/FireWithExperiments.nlogox", "test1", "test/lab/FireWithExperiments1")
      runExperimentFromModel("test/lab/FireWithExperiments.nlogox", "test2", "test/lab/FireWithExperiments2")
      runExperimentFromModel("test/lab/FireWithExperiments.nlogox", "test3", "test/lab/FireWithExperiments3")
      runExperimentFromModel("test/lab/FireWithExperiments.nlogox", "test4", "test/lab/FireWithExperiments4")
      runExperimentFromModel("test/lab/FireWithExperiments.nlogox", "test5", "test/lab/FireWithExperiments5")
    }
  if (!Version.is3D)
    test("ResizingWorld3", SlowTest.Tag) {
      run2DExperiment(0, 1, 0, 1, "", "testResizingWorld3")
    }
  test("Stopping1", SlowTest.Tag) {
    runExperiment(0, "globals [x]",
      "testStopping1")
  }
  test("Stopping2", SlowTest.Tag) {
    runExperiment(0, "globals [x] to go if x = 5 [ stop ] set x x + 1 end",
      "testStopping2")
  }
  test("DontRunMetricsAtEveryStep", SlowTest.Tag) {
    runExperiment(0,
      "globals [ glob1 ] to setup set glob1 one-of patches end " +
        "to go set glob1 5 end" +
        " to-report bad-divide report glob1 / 5 end",
      "badAtBeginning")
  }
  test("metricsLocalRandomness", SlowTest.Tag) {
    runExperiment(0, "globals [x]",
      "metricsLocalRandomness")
  }
  test("exitConditionLocalRandomness", SlowTest.Tag) {
    runExperiment(0, "globals [x]",
      "exitConditionLocalRandomness")
  }
  test("metricGoBoom", SlowTest.Tag) {
    runExperiment(0, "", "metricGoBoom")
  }

  // metricGoBoom2 is for bug #114.  before any fix, it passed if run through runParallelExperiment
  // but failed through runExperimentFromModel.  that's because the bug was in the workspace-reusing
  // logic in lab.Lab, which run() here bypasses by using lab.Worker directly - ST 4/6/12
  if (!Version.is3D) {
    val goBoom2Declarations =
      "to setup clear-all create-turtles 1 reset-ticks end\n" +
      "to go if not any? turtles [ stop ] if ticks = 10 [ ask turtles [ die ] ] tick end"
    test("metricGoBoom2", SlowTest.Tag) {
      runExperiment(0, goBoom2Declarations, "metricGoBoom2")
    }
    test("metricGoBoom2-parallel", SlowTest.Tag) {
      runParallelExperiment("metricGoBoom2", goBoom2Declarations)
    }
    test("metricGoBoom2-parallel-from-model", SlowTest.Tag) {
      runExperimentFromModel("test/lab/metricGoBoom2.nlogox", "experiment", "test/lab/metricGoBoom2", wantTable = false,
                             threads = Runtime.getRuntime.availableProcessors)
    }
  }

  test("setupCommandsGoBoom", SlowTest.Tag) {
    runExperiment(0, "", "setupCommandsGoBoom")
  }
  test("goCommandsGoBoom", SlowTest.Tag) {
    runExperiment(0, "", "goCommandsGoBoom")
  }
  test("metricsWithSideEffects", SlowTest.Tag) {
    runExperiment(0,
      "globals [g] to-report metric set g g + 1 report g end",
      "metricsWithSideEffects")
  }
  test("stringMetrics", SlowTest.Tag) {
    runExperiment(0,
      """to-report s1 report "x" end to-report s2 report "\"x\"" end""",
      "stringMetrics")
  }
  if (Version.is3D)
    test("ResizingWorld13d", SlowTest.Tag) {
      run3DExperiment("testResizingWorld13d")
    }
  if (Version.is3D)
    test("ResizingWorld23d", SlowTest.Tag) {
      run3DExperiment("testResizingWorld23d")
    }
  /*
  TODO this keeps failing in Jenkins depending on CPU load.
  I either need to make it less sensitive or just get rid of it. - ST 6/9/10
  test("ParallelOperationOfWait1", SlowTest.Tag) {
    val processors = Runtime.getRuntime.availableProcessors
    // if we need this test to work with other numbers of processors it will need revision
    assert(List(1,2,4).contains(processors))
    val time = System.currentTimeMillis
    runParallelExperiment("wait1")
    val elapsed = (System.currentTimeMillis - time) / 1000.0
    // 4 runs of 2 seconds each, but we should see close to linear speedup in number of processors.
    // we use log2 to make sure the time taken is right to within a factor of two.  (e.g. if the
    // theoretical runtime is 2 seconds, the actual runtime must be under 4 seconds for the test to
    // pass.)
    def log2(n:Double) = math.floor(math.log(n) / math.log(2)).toInt
    withClue("elapsed time: " + elapsed) {
      assertResult(log2(8 / processors)(log2(elapsed))
  }
  */
  test("dontRunMetricsIfNoListener", SlowTest.Tag) {
    val workspace = newWorkspace()
    workspace.initForTesting(0)
    newWorker("metricGoBoom")
      .run(workspace, () => workspace, 1)
    // with no output being generated, the metrics shouldn't be run at all,
    // so no runtime error
    assertResult(Double.box(2))(
      workspace.report("ticks"))
  }
  test("BasicSubExperiment", SlowTest.Tag) {
    runExperiment(0, "globals [a b]", "testBasicSubExperiment")
  }
  test("MultipleSubExperiments", SlowTest.Tag) {
    runExperiment(0, "globals [a b]", "testMultipleSubExperiments")
  }
  test("ConstantOverride", SlowTest.Tag) {
    runExperiment(0, "globals [a]", "testConstantOverride")
  }
  test("ConstantReplace", SlowTest.Tag) {
    runExperiment(0, "globals [a b]", "testConstantReplace")
  }
  test("MultipleConstantReplace", SlowTest.Tag) {
    runExperiment(0, "globals [a b c]", "testMultipleConstantReplace")
  }
  test("SubExperimentRepetitionsNonSequential", SlowTest.Tag) {
    runExperiment(0, "globals [a b]", "testSubExperimentRepetitionsNonSequential")
  }
  test("SubExperimentRepetitionsSequential", SlowTest.Tag) {
    runExperiment(0, "globals [a b]", "testSubExperimentRepetitionsSequential")
  }
  test("ComplexSubExperiments", SlowTest.Tag) {
    runExperiment(0, "globals [a b c]", "testComplexSubExperiments")
  }
  test("SimpleLists", SlowTest.Tag) {
    runExperiment(4, "globals [n]", "testSimpleLists", true)
  }
  test("SimpleListsWithTable", SlowTest.Tag) {
    runExperiment(4, "globals [n]", "testSimpleLists", true, wantLists=true)
  }
  test("SimpleListsWithSpreadsheet", SlowTest.Tag) {
    runExperiment(4, "globals [n]", "testSimpleLists", false, wantLists=true)
  }
  test("ListsEmptyExperiment", SlowTest.Tag) {
    runExperiment(0, "", "testListsEmptyExperiment", true, wantLists=true)
  }
  test("StatsWithTable", SlowTest.Tag) {
    runExperiment(4, "", "testStats", wantTable=true, wantStats=true)
  }
  test("StatsWithSpreadsheet", SlowTest.Tag) {
    runExperiment(4, "", "testStats", wantTable=false, wantStats=true)
  }
  test("ExcludeStatsMetricsTable", SlowTest.Tag) {
    runExperiment(4, "globals [string-test list-test]", "testStatsExcludeMetrics", wantStats=true)
  }
  test("ExcludeStatsMetricsSpreadsheet", SlowTest.Tag) {
    runExperiment(4, "globals [string-test list-test]", "testStatsExcludeMetrics", wantTable=false, wantStats=true)
  }
  test("IgnoreCommentsInMultilineReporters", SlowTest.Tag) {
    runExperiment(4, "", "testIgnoreComments", wantTable = false)
  }
}
