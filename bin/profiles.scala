#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import sys.process.Process
import java.io.File

val home = System.getenv("HOME")

// it might be better to move this into sbt where we wouldn't need to
// use a hand-maintained classpath like this - ST 8/2/13

// This script is run in the NetLogo directory. However the benchmarking class
// HeadlessBenchmarker is run by sbt from the NetLogo/bin directory.
// In particular it looks for the benchmark models in
// "../models/test/benchmarks/" For compatibility this script must execute
// the command "java -classpath " + classpath + ... +
//  "org.nlogo.headless.HeadlessBenchmarker " + ... from NetLogo/bin
// Therefore the classpath must be relative to NetLogo/bin aab April 2020
val classpath =
  Seq("../netlogo-gui/target/classes",
      "../shared/target/classes",
      "../netlogo-gui/resources",
      home + "/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.10.jar",
      home + "/.ivy2/cache/org.ow2.asm/asm-all/jars/asm-all-5.2.jar",
      home + "/.ivy2/cache/org.picocontainer/picocontainer/jars/picocontainer-2.15.jar",
      home + "/.ivy2/cache/log4j/log4j/jars/log4j-1.2.17.jar",
      home + "/.ivy2/cache/commons-codec/commons-codec/jars/commons-codec-1.15.jar",
      home + "/.ivy2/cache/org.parboiled/parboiled_2.12/jars/parboiled_2.12-2.3.0.jar",
      home + "/.ivy2/cache/com.typesafe/config/bundles/config-1.4.1.jar",
      home + "/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.12/bundles/scala-parser-combinators_2.12-1.1.2.jar")
    .mkString(":")


val allNames: List[String] = {
  val nameArgs = args.takeWhile(!_.head.isDigit).toList
  if(!nameArgs.isEmpty) nameArgs
  // This command runs in the NetLogo directory
  else Process("find models/test/benchmarks -name *.nlogo -maxdepth 1")
    .lazyLines.map(_.split("/").last.split(" ").head).toList
}

Process("mkdir -p tmp/profiles").!

// This command executes in the NetLogo/bin directory, the tmp directory is
// in the NetLogo directory
def benchCommand(name: String) =
  "java -classpath " + classpath + " " +
  "-Xrunhprof:cpu=samples,depth=40,file=../tmp/profiles/" + name + ".txt " +
  "org.nlogo.headless.HeadlessBenchmarker " + name + " 60 60"

for(name <- allNames) {
  println(name)
  Process(benchCommand(name), cwd = new File("bin")).!
}

// Local Variables:
// mode: scala
// End:
