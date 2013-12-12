// so e.g. `tr` is short for
//   testOnly org.nlogo.headless.lang.TestReporters
// and `tr Lists Strings` is short for
//   testOnly org.nlogo.headless.lang.TestReporters -- -n "Lists Strings"
// where -n tells ScalaTest to look for the given Tag names.
// you can run individual tests too with e.g.:
//   tr Numbers::Sqrt1 Numbers::Sqrt2

val tr = inputKey[Unit]("org.nlogo.headless.lang.TestReporters")
val tc = inputKey[Unit]("org.nlogo.headless.lang.TestCommands")
val te = inputKey[Unit]("org.nlogo.headless.lang.TestExtensions")
val tm = inputKey[Unit]("org.nlogo.headless.lang.TestModels")

def taggedTest(name: String): Def.Initialize[InputTask[Unit]] =
  Def.inputTaskDyn {
    val args = Def.spaceDelimited("<arg>").parsed
    val scalaTestArgs =
      if (args.isEmpty) ""
      else args.mkString(" -- -n \"", " ", "\"")
    (testOnly in Test).toTask(s" $name$scalaTestArgs")
  }

// `ts` is a little different. the arguments if any are a substring
// to match in the model path, so e.g. `ts Particle Swarm` is short for
//   testOnly org.nlogo.headless.misc.TestChecksums -- "-Dmodel=Particle Swarm"
// where TestChecksums.runTest will take care of interpreting that
// as a substring match.

val ts = inputKey[Unit]("org.nlogo.headless.misc.TestChecksums")

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

/// wire it all together

inConfig(Test)(
  Seq(tr, tc, te, tm, ts).flatMap(key =>
    Defaults.defaultTestTasks(key) ++
    Defaults.testTaskOptions(key)) ++
  Seq(tr, tc, te, tm).flatMap(key =>
    Seq(key := taggedTest(key.key.description.get).evaluated)) ++
  Seq(ts).flatMap(key =>
    Seq(key := keyValueTest(key.key.description.get, "model").evaluated)))
