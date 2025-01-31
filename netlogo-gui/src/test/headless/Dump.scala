// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.SimpleJobOwner
import org.nlogo.core.AgentKind
import org.nlogo.headless.ChecksumsAndPreviewsSettings.DumpsPath
import org.nlogo.workspace.ModelsLibrary

// This is accessible through the "bench" task in the sbt build.  It makes it convenient
// to run the `__dump` command on a particular models, or on whole groups of models.
// You can dump a compiled model to stdout, or replace all of the benchmark model dumps
// in test/benchdumps, or dump the whole models library to tmp/dumps. - ST 2/11/09, 8/21/13

object Dump {
  def main(argv:Array[String]) {
    argv match {
      case Array() => println("usage: dump all, dump bench, dump Fire, dump foo/bar/Fire.nlogox")
      case Array("all") => dumpAll()
      case Array("bench") => dumpBenchmarks()
      case Array(path:String) if path.endsWith(".nlogox") => print(dump(path))
      case Array(name:String) => print(dump(benchPath(name)))
    }
  }
  def dump(path:String) = {
    val workspace = HeadlessWorkspace.newInstance
    try {
      // I realized after writing this it would be more efficient to do what TestCompileAll does,
      // and not actually open the model. - ST 2/11/09
      workspace.open(path)
      val owner = new SimpleJobOwner("Dump", workspace.world.mainRNG, AgentKind.Observer)
      workspace.evaluateReporter(owner, "__dump").asInstanceOf[String]
    }
    finally { workspace.dispose() }
  }
  def benchPath(name:String) = "models/test/benchmarks/" + name + " Benchmark.nlogox"
  def dumpBenchmarks() {
    for(name <- ChecksumsAndPreviews.allBenchmarks)
      writeFile(DumpsPath + name + ".txt",
                dump(benchPath(name)))
  }

  def dumpAll() {
    Runtime.getRuntime().exec("rm -r target/dumps").waitFor()
    Runtime.getRuntime().exec("mkdir -p target/dumps").waitFor()
    //
    for(path <- ModelsLibrary.getModelPaths; if include(path))
    {
      val name = path.split("/").last.toList.dropRight(".nlogox".size).mkString
      print('.')
      writeFile(DumpsPath + name + ".txt",dump(path))
    }
    println
  }

  def writeFile(path:String,s:String) {
    val w = new java.io.FileWriter(path)
    w.write(s)
    w.close()
  }

  // HubNet models are excluded only because HeadlessWorkspace can't open them;
  // if this class used TestCompileAll's compilerTestingMode code, like it ought to, then
  // HubNet could be included. - ST 2/11/09
  def include(path:String) =
    !path.toUpperCase.containsSlice("HUBNET")
}
