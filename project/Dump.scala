import sbt._
import Def.spaceDelimited
import Keys._

object Dump {

  // e.g. dump "models/Sample\ Models/Earth\ Science/Fire.nlogo"
  // e.g. dump Fire   (to dump a benchmark model)
  // e.g. dump bench  (to replace all of the benchmark model dumps in test/bench)
  // e.g. dump all    (to dump all models to tmp/dumps)

  lazy val dump = InputKey[Unit](
    "dump",
    "dump compiled models (dump Fire, dump bench, dump all, dump <path>)")

  lazy val dumpClassName = SettingKey[String]("class used to dump bytecode")

  lazy val settings = Seq(
    dumpClassName := "org.nlogo.headless.Dump",
    dump := {
      val args   = spaceDelimited("").parsed
      val loader = (testLoader in Test).value
      // oh god, I hope this doesn't break something. it doesn't work without it, the bytecode
      // generator can't load classes - ST 6/29/12
      Thread.currentThread.setContextClassLoader(loader)
      loader.loadClass(dumpClassName.value)
        .getMethod("main", classOf[Array[String]])
        .invoke(null, args.toArray)
        ()
    })

}
