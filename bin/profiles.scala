#!/bin/sh
exec bin/scala -nocompdaemon -deprecation -classpath bin "$0" "$@" 
!# 

// Local Variables:
// mode: scala
// End:

import Scripting.{ shell, shellDo }
val allNames:List[String] =
  shell("""find test/models/benchmarks -name \*.nlogo -maxdepth 1""")
    .map(_.split("/").last.split(" ").head).toList
shellDo("mkdir -p tmp/profiles")
val version =
  shell("""java -classpath target/classes:project/boot/scala-2.9.1/lib/scala-library.jar:resources org.nlogo.headless.Main --fullversion""")
    .next
def benchCommand(name:String) =
  "make bench ARGS=\"" + name + " 60 60\" " +
  "JARGS=-Xrunhprof:cpu=samples,depth=40,file=tmp/profiles/" + name + ".txt"
for(name <- allNames) {
  println(name)
  shellDo(benchCommand(name))
}
