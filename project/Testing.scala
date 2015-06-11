import sbt._
import Def.{ Initialize, spaceDelimited }
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

  private val testKeys = Seq(tr, tc, te, tm, testChecksums)

  val settings = inConfig(Test)(
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(MediumTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    testKeys.flatMap(Defaults.defaultTestTasks) ++
    testKeys.flatMap(Defaults.testTaskOptions) ++
    Seq(
      testOptions in FastTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(fastFilter(path, _))) },
      testOptions in MediumTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(mediumFilter(path, _))) },
      testOptions in SlowTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(slowFilter(path, _))) },
      tr <<= oneTest(tr, "org.nlogo.headless.TestReporters"),
      tc <<= oneTest(tc, "org.nlogo.headless.TestCommands"),
      tm <<= oneTest(tm, "org.nlogo.headless.TestModels"),
      te <<= oneTest(te, "org.nlogo.headless.TestExtensions"),
      testChecksums <<= oneTest(testChecksums, "org.nlogo.headless.TestChecksums")
    ))

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

  // mostly copy-and-pasted from Defaults.inputTests. there may well be a better
  // way this could be done - ST 6/17/12
  def oneTest(key: InputKey[Unit], name: String): Initialize[InputTask[Unit]] =
    Def.inputTask {
      (streams, testResultLogger, loadedTestFrameworks, testGrouping in key, testExecution in key, testLoader, resolvedScoped, fullClasspath in key, javaHome in key, state) flatMap {
        case (s, testLogger, frameworks, groups, config, loader, scoped, cp, javaHome, st) =>
          val args = spaceDelimited("").parsed
          implicit val display = Project.showContextKey(st)
          val filter = Tests.Filters(Defaults.selectedFilter(Seq(name)))
          val mungedArgs =
            if(args.isEmpty) Nil
            else List("-n", args.mkString(" "))
          val modifiedOpts =
            filter +: Tests.Argument(TestFrameworks.ScalaTest, mungedArgs: _*) +: config.options
          val newConfig = config.copy(options = modifiedOpts)
          Defaults.allTestGroupsTask(
            s, frameworks, loader, groups, newConfig, cp, javaHome) map
              (testLogger.run(s.log, _, "not found"))
      }
    }

}
