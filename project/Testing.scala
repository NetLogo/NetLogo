import sbt._
import Keys._

object Testing {

  val FastTest = config("fast") extend(Test)
  val MediumTest = config("medium") extend(Test)
  val SlowTest = config("slow") extend(Test)

  val configs = Seq(FastTest, MediumTest, SlowTest)

  lazy val tr = InputKey[Unit]("tr", "org.nlogo.headless.TestReporters", test)
  lazy val tc = InputKey[Unit]("tc", "org.nlogo.headless.TestCommands", test)
  lazy val te = InputKey[Unit]("te", "org.nlogo.headless.TestExtensions", test)
  lazy val tm = InputKey[Unit]("tm", "org.nlogo.headless.TestModels", test)
  lazy val testChecksums = InputKey[Unit]("test-checksums", "org.nlogo.headless.TestChecksums", test)

  private val testKeys = Seq(tr, tc, te, tm, testChecksums)

  lazy val settings =
    fastMediumSlowSettings ++
    inConfig(Test)(specialTestTaskSettings)

  lazy val fastMediumSlowSettings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(MediumTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(fastFilter(path, _))) },
      testOptions in MediumTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(mediumFilter(path, _))) },
      testOptions in SlowTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(slowFilter(path, _))) })

  lazy val specialTestTaskSettings =
    testKeys.flatMap(Defaults.defaultTestTasks) ++
    testKeys.flatMap(Defaults.testTaskOptions) ++
    testKeys.map(key => key <<= oneTest(key)) ++
    testKeys.map(key => logBuffered in key := false)

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
  def oneTest(key: InputKey[Unit]): Project.Initialize[InputTask[Unit]] = {
    inputTask { (argTask: TaskKey[Seq[String]]) =>
      (argTask, streams, loadedTestFrameworks, testGrouping in key, testExecution in key, testLoader, fullClasspath in key, javaHome in key, state) flatMap {
        case (args, s, frameworks, groups, config, loader, cp, javaHome, st) =>
          implicit val display = Project.showContextKey(st)
          val filter = Tests.Filter(Defaults.selectedFilter(Seq(key.key.description.get)))
          val mungedArgs =
            if(args.isEmpty) Nil
            else List("-n", args.mkString(" "))
          val modifiedOpts =
            filter +: Tests.Argument(TestFrameworks.ScalaTest, mungedArgs: _*) +: config.options
          val newConfig = config.copy(options = modifiedOpts)
          Defaults.allTestGroupsTask(
            s, frameworks, loader, groups, newConfig, cp, javaHome) map
              (Tests.showResults(s.log, _, "not found"))
      }
    }
  }

}
