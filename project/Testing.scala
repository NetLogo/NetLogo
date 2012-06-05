import sbt._
import Keys._

object Testing {

  val FastTest = config("fast") extend(Test)
  val MediumTest = config("medium") extend(Test)
  val SlowTest = config("slow") extend(Test)
  
  val configs = Seq(FastTest, MediumTest, SlowTest)

  lazy val tr = InputKey[Unit]("tr", "run TestReporters", test)
  lazy val tc = InputKey[Unit]("tc", "run TestCommands", test)
  lazy val te = InputKey[Unit]("te", "run TestExtensions", test)
  lazy val tm = InputKey[Unit]("tm", "run TestModels", test)
  lazy val testChecksums = InputKey[Unit]("test-checksums", "run TestChecksums", test)

  val settings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(MediumTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(fastFilter(path, _))) },
      testOptions in MediumTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(mediumFilter(path, _))) },
      testOptions in SlowTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(slowFilter(path, _))) },
      tr <<= Testing.oneTest(tr, "org.nlogo.headless.TestReporters"),
      tc <<= Testing.oneTest(tc, "org.nlogo.headless.TestCommands"),
      tm <<= Testing.oneTest(tm, "org.nlogo.headless.TestModels"),
      te <<= Testing.oneTest(te, "org.nlogo.headless.TestExtensions"),
      testChecksums <<= Testing.oneTest(testChecksums, "org.nlogo.headless.TestChecksums")
    ) ++
    Seq(tr, tc, tm, te, testChecksums).flatMap(Defaults.testTaskOptions)

  private def fastFilter(path: Classpath, name: String): Boolean = !slowFilter(path, name)
  private def mediumFilter(path: Classpath, name: String): Boolean =
    fastFilter(path, name) ||
    name == "org.nlogo.headless.TestReporters" ||
    name == "org.nlogo.headless.TestCommands"
  private def slowFilter(path: Classpath, name: String): Boolean = {
    val jars = path.files.map(_.asURL).toArray[java.net.URL]
    val loader = new java.net.URLClassLoader(jars, getClass.getClassLoader)
    def clazz(name: String) = Class.forName(name, false, loader)
    clazz("org.nlogo.util.SlowTest").isAssignableFrom(clazz(name))
  }

  // mostly copy-and-pasted from Defaults.testOnlyTask.  This is the best I can figure out for
  // 0.11, but it appears to me that the test-only stuff has been refactored in 0.12 and 0.13 in
  // a way that might make this easier.  see e.g.
  // github.com/harrah/xsbt/commit/fe753768d93ebeaf59c9435059b583a7b2e744d3 - ST 5/31/12
  private def oneTest(key: InputKey[_], name: String) =
    inputTask { (argTask: TaskKey[Seq[String]]) =>
      (argTask, streams in key, loadedTestFrameworks in Test, parallelExecution in key, testOptions in key, testLoader in Test, definedTests in Test) flatMap {
        case (args, s, frameworks, par, opts, loader, discovered) =>
          val filter = Tests.Filter(Defaults.selectedFilter(Seq(name)))
          val mungedArgs =
            if(args.isEmpty) Nil
            else List("-n", args.mkString(" "))
          val augmentedOpts =
            filter +: Tests.Argument(TestFrameworks.ScalaTest, mungedArgs: _*) +: opts            
          Tests(frameworks, loader, discovered, augmentedOpts, par, "not found", s.log) map { results =>
            Tests.showResults(s.log, results)
          } } }

}
