import sbt._
import Def.{ Initialize, spaceDelimited }
import Keys._

object Testing {
  lazy val fast     = taskKey[Unit]("fast tests")
  lazy val medium   = taskKey[Unit]("medium tests")
  lazy val slow     = taskKey[Unit]("slow tests")
  lazy val language = taskKey[Unit]("language tests")
  lazy val crawl    = taskKey[Unit]("extremely slow tests")

  lazy val tr = inputKey[Unit]("org.nlogo.headless.TestReporters")
  lazy val tc = inputKey[Unit]("org.nlogo.headless.TestCommands")
  lazy val te = inputKey[Unit]("org.nlogo.headless.TestExtensions")
  lazy val tm = inputKey[Unit]("org.nlogo.headless.TestModels")
  lazy val ts = inputKey[Unit]("org.nlogo.headless.TestChecksums")

  lazy val testTempDirectory = settingKey[File]("Temp directory for tests to write files to")

  private val testKeys = Seq(tr, tc, te, tm, ts)

  lazy val suiteSettings = Seq(
    (fast in Test) := {
      (testOnly in Test).toTask(" -- -l org.nlogo.util.SlowTestTag -l org.nlogo.headless.LanguageTestTag").value
    },
    (medium in Test) := {
      (testOnly in Test).toTask(" -- -l org.nlogo.util.SlowTestTag").value
    },
    (language in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.headless.LanguageTestTag").value
    },
    (crawl in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.util.SlowTestTag").value
    },
    (slow in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.headless.LanguageTestTag -n org.nlogo.util.SlowTestTag").value
    })

  val settings = suiteSettings ++
    inConfig(Test)(
      Seq(
        testTempDirectory := file("tmp"),
        testOnly <<= testOnly dependsOn Def.task{ IO.createDirectory(testTempDirectory.value) },
        test     <<= test     dependsOn Def.task{ IO.createDirectory(testTempDirectory.value) }) ++
      testKeys.flatMap(key =>
          Defaults.defaultTestTasks(key) ++
          Defaults.testTaskOptions(key)) ++
      Seq(tr, tc, te, tm).flatMap(key =>
          Seq(key := taggedTest(key.key.description.get).evaluated)) ++
      Seq(ts).flatMap(key =>
          Seq(key := keyValueTest(key.key.description.get, "model").evaluated)))

  def taggedTest(name: String): Def.Initialize[InputTask[Unit]] =
    Def.inputTaskDyn {
      val args = Def.spaceDelimited("<arg>").parsed
      val scalaTestArgs =
        if (args.isEmpty) ""
        else args.mkString(" -- -z \"", " ", "\"")
      (testOnly in Test).toTask(s" $name$scalaTestArgs")
    }

  def keyValueTest(name: String, key: String): Def.Initialize[InputTask[Unit]] =
    Def.inputTaskDyn {
      val args = Def.spaceDelimited("<arg>").parsed
      val scalaTestArgs =
        if (args.isEmpty)
          ""
        else
          s""" -- "-D$key=${args.mkString(" ")}""""
      (testOnly in Test).toTask(s" $name$scalaTestArgs")
    }
}
