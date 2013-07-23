import sbt._
import Keys._

object Testing {

  val FastTest = config("fast") extend(Test)
  val MediumTest = config("medium") extend(Test)
  val SlowTest = config("slow") extend(Test)

  val configs = Seq(FastTest, MediumTest, SlowTest)

  lazy val tr            = inputKey[Unit]("org.nlogo.headless.lang.TestReporters")
  lazy val tc            = inputKey[Unit]("org.nlogo.headless.lang.TestCommands")
  lazy val te            = inputKey[Unit]("org.nlogo.headless.lang.TestExtensions")
  lazy val tm            = inputKey[Unit]("org.nlogo.headless.lang.TestModels")
  lazy val testChecksums = inputKey[Unit]("org.nlogo.headless.misc.TestChecksums")

  private val testKeys = Seq(tr, tc, te, tm, testChecksums)

  lazy val settings =
    fastMediumSlowSettings ++
    inConfig(Test)(specialTestTaskSettings)

  lazy val fastMediumSlowSettings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(MediumTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest :=
        Seq(Tests.Filter(fastFilter((fullClasspath in Test).value, _))),
      testOptions in MediumTest :=
        Seq(Tests.Filter(mediumFilter((fullClasspath in Test).value, _))),
      testOptions in SlowTest :=
        Seq(Tests.Filter(slowFilter((fullClasspath in Test).value, _)))
    )

  lazy val specialTestTaskSettings =
    testKeys.flatMap(Defaults.defaultTestTasks) ++
    testKeys.flatMap(Defaults.testTaskOptions) ++
    testKeys.map(key => key <<= oneTest(key)) ++
    testKeys.map(key => logBuffered in key := false)

  private def fastFilter(path: Classpath, name: String): Boolean = !slowFilter(path, name)
  private def mediumFilter(path: Classpath, name: String): Boolean =
    fastFilter(path, name) ||
    name == "org.nlogo.headless.lang.TestReporters" ||
    name == "org.nlogo.headless.lang.TestCommands"
  private def slowFilter(path: Classpath, name: String): Boolean = {
    val jars = path.files.map(_.asURL).toArray[java.net.URL]
    val loader = new java.net.URLClassLoader(jars, getClass.getClassLoader)
    def clazz(name: String) = Class.forName(name, false, loader)
    clazz("org.nlogo.util.SlowTest").isAssignableFrom(clazz(name))
  }

  // mostly copy-and-pasted from Defaults.inputTests. is there a better way?
  // - ST 6/17/12, 7/23/13
  def oneTest(key: InputKey[Unit]): Def.Initialize[InputTask[Unit]] = {
    val parser = Def.value((_: State) => Def.spaceDelimited())
    InputTask.createDyn(parser)(
      Def.task {
        (args: Seq[String]) =>
          val config = {
            val oldConfig = (testExecution in key).value
            val mungedArgs =
              if(args.isEmpty) Nil
              else List("-n", args.mkString(" "))
            val className = key.key.description.get
            val filter = Tests.Filters(Defaults.selectedFilter(Seq(className)))
            val modifiedOpts = filter +:
              Tests.Argument(TestFrameworks.ScalaTest, mungedArgs: _*) +:
              oldConfig.options
            oldConfig.copy(options = modifiedOpts)
          }
          val task: Task[Tests.Output] =
            Defaults.allTestGroupsTask(
              streams.value, loadedTestFrameworks.value, testLoader.value,
              (testGrouping in key).value, config, (fullClasspath in key).value,
              // here I want to write `(javaHome in key).value` but it fails with
              // "Illegal dynamic reference: Def". don't understand why - ST 7/23/13
              Some(new java.io.File(""))
            )
          val result: Task[Unit] =
            task.map(Tests.showResults(streams.value.log, _, "not found"))
          Def.value(result)
      })
  }

}
