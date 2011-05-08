package org.nlogo.headless
import org.nlogo.api.{CompilerException,LogoException,Version}
import org.nlogo.workspace.Benchmarker
import org.nlogo.util.SysInfo
object HeadlessBenchmarker {
  def main(argv:Array[String]) {
    Main.setHeadlessProperty()
    val (name,minTime,maxTime) = argv match {
      case Array(name)         => (name,60,300)
      case Array(name,min)     => (name,min.toInt,min.toInt)
      case Array(name,min,max) => (name,min.toInt,max.toInt)
      case _ => throw new IllegalArgumentException("expected: name | name min | name min max")
    }
    println("@@@@@@ benchmarking " + Version.fullVersion)
    println("@@@@@@ warmup " + minTime + " seconds, min " + minTime + " seconds, max " + maxTime + " seconds")
    val workspace = HeadlessWorkspace.newInstance
    try {
      workspace.open("test/models/benchmarks/" + name + " Benchmark.nlogo")
      Benchmarker.benchmark(workspace,minTime,maxTime)
    }
    finally { workspace.dispose() }
  }
}
