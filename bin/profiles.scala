#!/usr/bin/env scala -nocompdaemon -deprecation -Dfile.encoding=UTF-8
//!#

import sys.process.Process

val allNames: List[String] =
  Process("find models/test/benchmarks -name *.nlogo -maxdepth 1")
    .lines.map(_.split("/").last.split(" ").head).toList

Process("rm -rf tmp/profiles").!
Process("mkdir -p tmp/profiles").!
for(name <- allNames) {
  val command = Seq("./sbt",
    "run-main org.nlogo.headless.HeadlessBenchmarker " + name + " 10 10")
  val javaOpts = "-Xrunhprof:cpu=samples,depth=40,file=tmp/profiles/" + name + ".txt"
  Process(command, None, "JAVA_OPTS" -> javaOpts).!
}
