// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.workspace.ModelsLibrary
import org.nlogo.api.{ SimpleJobOwner, TwoDVersion }

// This is accessible through the "bench" task in the sbt build.  It makes it convenient
// to run the `__dump` command on a particular models, or on whole groups of models.
// You can dump a compiled model to stdout, or replace all of the benchmark model dumps
// in test/benchdumps, or dump the whole models library to tmp/dumps. - ST 2/11/09, 8/21/13

object Dump {
  def main(argv:Array[String]) {
    argv match {
      case Array() => println("usage: dump all, dump bench, dump Fire, dump foo/bar/Fire.nlogo")
      case Array("all") => dumpAll()
      case Array("bench") => dumpBenchmarks()
      case Array(path:String) if path.endsWith(".nlogo") => print(dump(path))
      case Array(name:String) => print(dump(benchPath(name)))
    }
  }
  def dump(path:String) = {
    val workspace = HeadlessWorkspace.newInstance
    try {
      // I realized after writing this it would be more efficient to do what TestCompileAll does,
      // and not actually open the model. - ST 2/11/09
      workspace.open(path)
      val owner = new SimpleJobOwner("Dump", workspace.world.mainRNG)
      workspace.evaluateReporter(owner, "__dump").asInstanceOf[String]
    }
    finally { workspace.dispose() }
  }
  def benchPath(name:String) = "models/test/benchmarks/" + name + " Benchmark.nlogo"
  def dumpBenchmarks() {
    for(name <- ChecksumsAndPreviews.allBenchmarks)
      writeFile("netlogo-headless/test/benchdumps/" + name + ".txt",
                dump(benchPath(name)))
  }
  def dumpAll() {
    Runtime.getRuntime().exec("rm -r target/dumps").waitFor()
    Runtime.getRuntime().exec("mkdir -p target/dumps").waitFor()
    //
    for {
      path <- ModelsLibrary.getModelPaths(TwoDVersion, true)
      if !TestCompileAll.badPath(path)
    } {
      val name = path.split("/").last.toList.mkString.stripSuffix(".nlogo")
      print('.')
      writeFile("target/dumps/" + name + ".txt",dump(path))
    }
    println
  }
  def writeFile(path:String,s:String) {
    val w = new java.io.FileWriter(path)
    w.write(s)
    w.close()
  }
}
