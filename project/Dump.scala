import sbt._
import Keys._

object Dump {

  // e.g. dump "models/Sample\ Models/Earth\ Science/Fire.nlogo"
  // e.g. dump Fire   (to dump a benchmark model)
  // e.g. dump bench  (to replace all of the benchmark model dumps in test/bench)
  // e.g. dump all    (to dump all models to target/dumps)

  lazy val dump = InputKey[Unit](
    "dump",
    "dump compiled models (dump Fire, dump bench, dump all, dump <path>)")

  lazy val settings = Seq(
    dump <<= dumpTask)

  lazy val dumpTask = inputTask { (argTask: TaskKey[Seq[String]]) =>
    (argTask, testLoader in Test) map {
      (args, loader) =>
        // oh god, I hope this doesn't break something. it doesn't work without it, the bytecode
        // generator can't load classes - ST 6/29/12
        Thread.currentThread.setContextClassLoader(loader)
        loader.loadClass("org.nlogo.headless.misc.Dump")
          .getMethod("main", classOf[Array[String]])
          .invoke(null, args.toArray)
      ()
    } }

}
