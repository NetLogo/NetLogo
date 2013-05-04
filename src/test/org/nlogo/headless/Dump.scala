// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.workspace.ModelsLibrary
import org.nlogo.agent.Observer
import org.nlogo.api.SimpleJobOwner

// This is used by the "bench" target in the Makefile.  You can __dump a compiled model
// to stdout, or replace all of the benchmark model dumps in test/benchdumps, or dump
// the whole models library to target/dumps. - ST 2/11/09

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
      writeFile("test/benchdumps/" + name + ".txt",
                dump(benchPath(name)))
  }
  def dumpAll() {
    Runtime.getRuntime().exec("rm -r target/dumps").waitFor()
    Runtime.getRuntime().exec("mkdir -p target/dumps").waitFor()
    //
    for(path <- ModelsLibrary.getModelPaths)
    {
      val name = path.split("/").last.toList.dropRight(".nlogo".size).mkString
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
