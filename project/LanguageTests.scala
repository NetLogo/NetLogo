import sbt._
import Keys._

object LanguageTests {

  // so e.g. `tr` is short for
  //   test-only org.nlogo.headless.lang.TestReporters
  // and `tr Lists Strings` is short for
  //   test-only org.nlogo.headless.lang.TestReporters -- -n "Lists Strings"

  val tr = inputKey[Unit]("org.nlogo.headless.lang.TestReporters")
  val tc = inputKey[Unit]("org.nlogo.headless.lang.TestCommands")
  val te = inputKey[Unit]("org.nlogo.headless.lang.TestExtensions")
  val tm = inputKey[Unit]("org.nlogo.headless.lang.TestModels")
  val ts = inputKey[Unit]("org.nlogo.headless.misc.TestChecksums")

  val settings = inConfig(Test)(
    Seq(tr, tc, te, tm, ts).flatMap(key =>
      Defaults.defaultTestTasks(key) ++
      Defaults.testTaskOptions(key) ++
      Seq(key <<= oneTest(key),
          logBuffered in key := false)))

  // The code for this mostly copy-and-pasted from Defaults.inputTests in the sbt sources.
  // Could this be done better? - ST 6/17/12, 7/23/13
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
