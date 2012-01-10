// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.{ FunSuite, OneInstancePerTest, BeforeAndAfterEach }
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.nvm.{ LabInterface, Workspace }
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.util.SlowTest

class TestBehaviorSpace extends FunSuite with SlowTest
with OneInstancePerTest with BeforeAndAfterEach {

  val workspaces = new collection.mutable.ListBuffer[HeadlessWorkspace]
  def newWorkspace() = {
    val w = HeadlessWorkspace.newInstance
    workspaces += w
    w
  }

  override def beforeEach() { AbstractWorkspace.isApplet(false) }
  override def afterEach() { workspaces.foreach(_.dispose()) }

  // first 6 lines of results are header lines. we'll need to discard them
  def withoutFirst6Lines(s: String) =
    s.split("\n").drop(6).mkString("", "\n", "\n")
  def slurp(path: String) =
    stripLineFeeds(FileIO.file2String(path))
  def stripLineFeeds(s: String) =
    s.replaceAll("\r\n", "\n")

  def run2DExperiment(minX: Int, maxX: Int, minY: Int, maxY: Int, declarations: String, name: String) = {
    val workspace = newWorkspace()
    workspace.initForTesting(minX, maxX, minY, maxY, declarations)
    run("test/lab/" + name, 1, true, true, () => workspace,
      () => HeadlessWorkspace.newLab.newWorker(name, new java.io.File("test/lab/protocols.xml")))
    workspace
  }
  def runExperiment(worldSize: Int, declarations: String, name: String) = {
    val workspace = newWorkspace()
    workspace.initForTesting(worldSize, declarations)
    run("test/lab/" + name, 1, true, true, () => workspace,
      () => HeadlessWorkspace.newLab.newWorker(name, new java.io.File("test/lab/protocols.xml")))
    workspace
  }
  def run3DExperiment(name: String) {
    val workspace = newWorkspace()
    workspace.initForTesting(0)
    run("test/lab/" + name, 1, true, true, () => workspace,
      () => HeadlessWorkspace.newLab.newWorker(name, new java.io.File("test/lab/protocols.xml")))
  }
  def runParallelExperiment(name: String) {
    def workspace = {
      val w = newWorkspace()
      w.initForTesting(0)
      w
    }
    // only get spreadsheet results, since parallel table results are in scrambled order - ST 3/4/09
    run("test/lab/" + name, Runtime.getRuntime.availableProcessors, false, true, workspace _,
      () => HeadlessWorkspace.newLab.newWorker(name, new java.io.File("test/lab/protocols.xml")))
  }
  def runExperimentFromModel(modelPath: String, experimentName: String, filename: String) {
    val time = System.nanoTime
    new java.io.File("tmp").mkdir()
    new java.io.File("tmp/TestBehaviorSpace").mkdir()
    val tablePath = "tmp/TestBehaviorSpace/" + time + "-table.csv"
    val spreadsheetPath = "tmp/TestBehaviorSpace/" + time + "-spreadsheet.csv"
    // let's go through headless.Main here so that code gets some testing - ST 3/9/09
    Main.main(Array("--model", modelPath, "--experiment", experimentName,
      "--table", tablePath, "--spreadsheet", spreadsheetPath))
    expect(slurp(filename + "-table.csv"))(
      withoutFirst6Lines(slurp(tablePath)))
    expect(slurp(filename + "-spreadsheet.csv"))(
      withoutFirst6Lines(slurp(spreadsheetPath)))
  }
  // sorry this has gotten so baroque with all the closures and tuples and
  // whatnot. it should be redone - ST 8/19/09
  def run(filename: String, threads: Int, wantTable: Boolean, wantSpreadsheet: Boolean,
          fn: () => Workspace, fn2: () => LabInterface.Worker) {
    val dims = fn.apply.world.getDimensions
    def runHelper(fns: List[(String, (LabInterface.Worker, java.io.StringWriter) => Unit)]) {
      val worker = fn2.apply
      val writers = fns.map(_ => new java.io.StringWriter)
      for (((_, fn), writer) <- fns zip writers)
        fn(worker, writer)
      worker.run(fn.apply, fn, threads)
      for (((suffix, _), writer) <- fns zip writers) {
        val resultsPath = filename + suffix
        withClue(resultsPath) {
          expect(slurp(resultsPath))(
          withoutFirst6Lines(stripLineFeeds(writer.toString)))
        }
      }
    }
    def table(worker: LabInterface.Worker, writer: java.io.StringWriter) {
      worker.addTableWriter(filename, dims, new java.io.PrintWriter(writer))
    }
    def spreadsheet(worker: LabInterface.Worker, writer: java.io.StringWriter) {
      worker.addSpreadsheetWriter(filename, dims, new java.io.PrintWriter(writer))
    }
    runHelper(List(("-table.csv", table _), ("-spreadsheet.csv", spreadsheet _))
      .filter {
        case (suffix, _) =>
          suffix == "-table.csv" && wantTable ||
            suffix == "-spreadsheet.csv" && wantSpreadsheet
      })
  }

  test("BehaviorSpace1") {
    runExperiment(0, "globals [param1 param2 counter]",
      "testBehaviorSpace1")
  }
  test("BehaviorSpace2") {
    runExperiment(0, "globals [param1 param2 counter]",
      "testBehaviorSpace2")
  }
  test("MultipleMetrics") {
    runExperiment(0, "globals [counter param1]",
      "testMultipleMetrics")
  }
  test("NoMetrics1") {
    runExperiment(0, "globals [counter param1]",
      "testNoMetrics1")
  }
  test("NoMetrics2") {
    runExperiment(0, "globals [counter param1]",
      "testNoMetrics2")
  }
  test("ImmediateExit") {
    val workspace =
      runExperiment(0, "globals [counter foo]",
        "testImmediateExit")
    expect(Double.box(99))(
      workspace.report("foo"))
  }
  test("CarryoverBetweenRuns") {
    val workspace = newWorkspace()
    workspace.initForTesting(0, "globals [foo]")
    // no setup commands, so foo doesn't get reset
    HeadlessWorkspace.newLab
      .newWorker("testCarryover",
        new java.io.File("test/lab/protocols.xml"))
        .run(workspace, () => workspace, 1)
    expect(Double.box(20))(
      workspace.report("foo"))
  }
  test("ResizingWorld1") {
    runExperiment(0, "", "testResizingWorld1")
  }
  test("ResizingWorld2") {
    runExperiment(0, "", "testResizingWorld2")
  }
  test("SettingRandomSeed") {
    runExperiment(0, "", "testRandomSeed")
  }
  test("RunNumber") {
    val workspace = runExperiment(0, "", "runNumber")
    // I suppose we could reset the run number to 0 after a run, and we do that in the GUI, but I
    // can't see a reason to ensure it headless - ST 7/7/10
    expect(Double.box(3))(
      workspace.report("behaviorspace-run-number"))
  }
  // test export-graphics in headless mode
  if(!Version.is3D)
    test("ExportGraphics") {
      val workspace = newWorkspace()
      workspace.open("models/test/lab/FireWithExperiments.nlogo")
      HeadlessWorkspace.newLab.newWorker("testExportGraphics",
        new java.io.File("test/lab/protocols.xml"))
        .run(workspace, () => workspace, 1)
    }
  if(!Version.is3D)
    test("ModelWithIncludedExperiments") {
      runExperimentFromModel("models/test/lab/FireWithExperiments.nlogo", "test1", "models/test/lab/FireWithExperiments1")
      runExperimentFromModel("models/test/lab/FireWithExperiments.nlogo", "test2", "models/test/lab/FireWithExperiments2")
    }
  if(!Version.is3D)
    test("ResizingWorld3") {
      run2DExperiment(0, 1, 0, 1, "", "testResizingWorld3")
    }
  test("Stopping1") {
    runExperiment(0, "globals [x]",
      "testStopping1")
  }
  test("Stopping2") {
    runExperiment(0, "globals [x] to go if x = 5 [ stop ] set x x + 1 end",
      "testStopping2")
  }
  test("DontRunMetricsAtEveryStep") {
    runExperiment(0,
      "globals [ glob1 ] to setup set glob1 one-of patches end " +
        "to go set glob1 5 end" +
        " to-report bad-divide report glob1 / 5 end",
      "badAtBeginning")
  }
  test("metricsLocalRandomness") {
    runExperiment(0, "globals [x]",
      "metricsLocalRandomness")
  }
  test("exitConditionLocalRandomness") {
    runExperiment(0, "globals [x]",
      "exitConditionLocalRandomness")
  }
  test("metricGoBoom") {
    runExperiment(0, "", "metricGoBoom")
  }
  test("setupCommandsGoBoom") {
    runExperiment(0, "", "setupCommandsGoBoom")
  }
  test("goCommandsGoBoom") {
    runExperiment(0, "", "goCommandsGoBoom")
  }
  test("metricsWithSideEffects") {
    runExperiment(0,
      "globals [g] to-report metric set g g + 1 report g end",
      "metricsWithSideEffects")
  }
  if(Version.is3D)
    test("ResizingWorld13d") {
      run3DExperiment("testResizingWorld13d")
    }
  if(Version.is3D)
    test("ResizingWorld23d") {
      run3DExperiment("testResizingWorld23d")
    }
  /*
  TODO this keeps failing in Jenkins depending on CPU load.
  I either need to make it less sensitive or just get rid of it. - ST 6/9/10
  test("ParallelOperationOfWait1") {
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
      expect(log2(8 / processors)(log2(elapsed))
  }
  */
  test("dontRunMetricsIfNoListener") {
    val workspace = newWorkspace()
    workspace.initForTesting(0)
    HeadlessWorkspace.newLab
      .newWorker("metricGoBoom",
        new java.io.File("test/lab/protocols.xml"))
        .run(workspace, () => workspace, 1)
    // with no output being generated, the metrics shouldn't be run at all,
    // so no runtime error
    expect(Double.box(2))(
      workspace.report("ticks"))
  }
}
