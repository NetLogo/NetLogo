#!/bin/sh
exec bin/scala -nocompdaemon -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import sys.process.Process

// it might be better to move this into sbt where we wouldn't need to
// use a hand-maintained classpath like this - ST 8/2/13
val classpath =
  Seq("target/classes",
      System.getenv("HOME") + "/.sbt/boot/scala-2.10.2/lib/scala-library.jar",
      "resources",
      "lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar")
    .mkString(":")

val allNames: List[String] =
  Process("find models/test/benchmarks -name *.nlogo -maxdepth 1")
    .lines.map(_.split("/").last.split(" ").head).toList

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
