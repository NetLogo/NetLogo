#!/bin/sh
exec $SCALA_HOME/bin/scala -nocompdaemon -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import sys.process.Process

val home = System.getenv("HOME")

// it might be better to move this into sbt where we wouldn't need to
// use a hand-maintained classpath like this - ST 8/2/13
val classpath =
  Seq("netlogo-gui/target/classes",
      "shared/target/classes",
      "netlogo-gui/resources",
      System.getenv("SCALA_HOME") + "/lib/scala-library.jar",
      home + "/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.12/bundles/scala-parser-combinators_2.12-1.0.4.jar",
      home + "/.ivy2/cache/org.ow2.asm/asm-all/jars/asm-all-5.0.4.jar",
      home + "/.ivy2/cache/log4j/log4j/jars/log4j-1.2.16.jar",
      home + "/.ivy2/cache/org.parboiled/parboiled_2.12/jars/parboiled_2.12-2.1.3.jar",
      home + "/.ivy2/cache/org.picocontainer/picocontainer/jars/picocontainer-2.13.6.jar")
    .mkString(":")

val allNames: List[String] = {
  val nameArgs = args.takeWhile(!_.head.isDigit).toList
  if(!nameArgs.isEmpty) nameArgs
  else Process("find models/test/benchmarks -name *.nlogo -maxdepth 1")
    .lines.map(_.split("/").last.split(" ").head).toList
}

Process("mkdir -p tmp/profiles").!

def benchCommand(name: String) =
  "java -classpath " + classpath + " " +
  "-Xrunhprof:cpu=samples,depth=40,file=tmp/profiles/" + name + ".txt " +
  "org.nlogo.headless.HeadlessBenchmarker " + name + " 60 60"

for(name <- allNames) {
  println(name)
  Process(benchCommand(name)).!
}

// Local Variables:
// mode: scala
// End:
