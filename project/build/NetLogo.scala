import com.sun.jmx.snmp.tasks.Task
import sbt._

// - use Java 1.5 libraries as compiler bootclasspath?
// - use "package" to build NetLogo.jar, NetLogoLite.jar, BehaviorSpace.jar?
// - generate scaladoc for BehaviorSpace?

class NetLogo(info: ProjectInfo) extends DefaultProject(info)
  with Libraries
  with Fork
  with SbtTestHelpers
  with Autogen
  with Jars
  with Internationalization
  with ScalaVersionBumper
  with Depend
  with PMD
  with Classycle
  with ModelIndex
  with BehaviorSpaceSources {

  // we tried this out for a while, but it suppresses too much.  better for now
  // to accept the default (Level.Info) - ST 6/8/10
  // override def defaultLoggingLevel = Level.Warn

  log.setTrace(0)  // 0 equals nosbt equals leave out sbt's lines from stack traces

  /// paths
  // on this project we keep all sources, whether they be Scala or Java, and whether they be
  // regular classes or test classes, in a single src tree.
  override def mainScalaSourcePath = "src" / "main"
  override def mainJavaSourcePath = "src" / "main"
  override def testScalaSourcePath = "src" / "test"
  override def testJavaSourcePath =  "src" / "test"
  override def testSourceRoots = super.testSourceRoots +++ ("src" / "tools")
  override def mainResourcesPath = "resources"

  /// compiling
  override def copyResourcesAction = super.copyResourcesAction dependsOn(native2ascii)
  // had to put copyResourcesAction here even though it seems like it shouldn't be needed.
  // its possible that this is an sbt bug, but I don't have the time to look into it. - JC 3/8/11
  override def compileAction = super.compileAction dependsOn(autogen, copyResourcesAction, java5, nativeJoglLibs)
  override def compileOptions =
    "-unchecked -Xfatal-warnings -encoding us-ascii -Xcheckinit"
     .split(" ").map(CompileOption).toSeq ++ super.compileOptions
  override def javaCompileOptions =
    "-bootclasspath dist/java5/classes.jar:dist/java5/ui.jar -g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.5 -target 1.5"
     .split(" ").map(JavaCompileOption).toSeq ++ super.javaCompileOptions

  override def cleanAction = super.cleanAction dependsOn(cleanAutogenFiles)

  /// parallel tests, parallel building
  override def parallelExecution = true

  /// running
  // we override fork because by default sbt wants to run the app in the same JVM we did the build
  // in. this doesn't work in NetLogo's case because our GUI cannot be cleanly shut down, so we
  // fork to get a fresh JVM. - ST 2009
  override def artifactID = "NetLogo"
  override def fork = Some(forkConfiguration)  // from Fork.scala

  // we can put top level options here, if we need.
  // one thing we'll probably want to do is increase the number of tests run by scalacheck
  // here is an example of that.
  //override def testOptions = super.testOptions ++ Seq(TestArgument(TestFrameworks.ScalaCheck, "-s", "5000"))
  //  -oD tells ScalaTest to print out how long each test takes to run - ST 1/12/11
  override def testOptions = super.testOptions ++ Seq(TestArgument(TestFrameworks.ScalaTest, "-oD"))

  lazy val testChecksums = singleTestTask("org.nlogo.headless.TestChecksums")
  lazy val tc = singleTestTask("org.nlogo.headless.TestCommands")
  lazy val tr = singleTestTask("org.nlogo.headless.TestReporters")
  lazy val tm = singleTestTask("org.nlogo.headless.TestModels")
  lazy val te = singleTestTask("org.nlogo.headless.TestExtensions")

  lazy val testSlow = runSubclassesOf("org.nlogo.util.SlowTest")
  lazy val testMedium = task { None }.dependsOn(List(testFast, tc, tr, tm, te).map(_.apply(Array())):_*)
  lazy val testFast = runEverythingButSubclassesOf("org.nlogo.util.SlowTest")

  def checksumsAndPreviewsTask(arg: String) =
    task { args => task {
      implicit val runner = new Run(buildScalaInstance) // don't fork
      Run.run("org.nlogo.headless.ChecksumsAndPreviews", testClasspath.get, Array(arg) ++ args, log)
    }.dependsOn(testCompile) }

  lazy val checksum = checksumsAndPreviewsTask("--checksum")
  lazy val checksums = checksumsAndPreviewsTask("--checksums")
  lazy val preview = checksumsAndPreviewsTask("--preview")
  lazy val previews = checksumsAndPreviewsTask("--previews")

  // I think these two would be better implemented using
  // code.google.com/p/simple-build-tool/wiki/Properties - ST 1/27/11
  lazy val threed = task {System.setProperty("org.nlogo.is3d", "true"); None}
  lazy val nogen = task {System.setProperty("org.nlogo.noGenerator", "true"); None}

  lazy val go =
    task { args => task {
      Run.run("org.nlogo.app.App", runClasspath.get, args, log)
    }.dependsOn(compile) }
  lazy val hubnetClient =
    runTask(Some("org.nlogo.hubnet.client.App"), runClasspath).dependsOn(compile)

  // e.g. dump "models/Sample\ Models/Earth\ Science/Fire.nlogo"
  // e.g. dump Fire   (to dump a benchmark model)
  // e.g. dump bench  (to replace all of the benchmark model dumps in test/bench)
  // e.g. dump all    (to dump all models to tmp/dumps)

  lazy val dump = task { args => task {
    implicit val runner = new Run(buildScalaInstance) // don't fork
    Run.run("org.nlogo.headless.Dump", testClasspath.get, args, log)
  } }

  val infotabDocs = "docs" / "infotab.html"
  val infoTabModel = "models" / "Code Examples" / "Info Tab Example.nlogo"
  lazy val genInfoTabDocs = fileTask(infotabDocs from infoTabModel) {
    runTask(Some("org.nlogo.tools.InfoTabDocGenerator"), testClasspath).dependsOn(testCompile).run
  }

}
