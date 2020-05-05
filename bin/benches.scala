#!/bin/sh
exec scala -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

// April 2020 - AAB - remove deprecated -nocompdaemon, use lazyLines

import sys.process.Process
import java.io.File
import collection.mutable.{ HashMap, ListBuffer, HashSet }
import Ordering.Double.TotalOrdering

val results = new HashMap[String, ListBuffer[Double]]
val haveGoodResult = new HashSet[String]

val home = System.getenv("HOME")

// This script is run in the NetLogo directory. However the benchmarking class
// HeadlessBenchmarker is run by sbt from the NetLogo/bin directory.
// In particular it looks for the benchmark models in
// "../models/test/benchmarks/" For compatibility this script must execute
// the command "java -classpath " + classpath + ... +
//  "org.nlogo.headless.HeadlessBenchmarker " + ... from NetLogo/bin
// Therefore the classpath must be relative to NetLogo/bin aab April 2020
val classpath =
  Seq("../netlogo-gui/target/classes",
      "../parser-jvm/target/classes",
      "../shared/target/classes",
      "../netlogo-gui/resources",
      home + "/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.10.jar",
      home + "/.ivy2/cache/org.typelevel/cats-core_2.12/jars/cats-core_2.12-1.0.0-MF.jar",
      home + "/.ivy2/local/org.nlogo/xml-lib_2.12/0.0.1/jars/xml-lib_2.12.jar",
      home + "/.ivy2/cache/com.typesafe/config/bundles/config-1.3.1.jar",
      home + "/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.12/bundles/scala-parser-combinators_2.12-1.0.5.jar",
      home + "/.ivy2/cache/org.scala-lang.modules/scala-xml_2.12/bundles/scala-xml_2.12-1.0.6.jar",
      home + "/.ivy2/cache/org.typelevel/cats-core_2.12/jars/cats-core_2.12-1.0.0-MF.jar",
      home + "/.ivy2/cache/org.ow2.asm/asm-all/jars/asm-all-5.0.4.jar",
      home + "/.ivy2/cache/log4j/log4j/jars/log4j-1.2.16.jar",
      home + "/.ivy2/cache/commons-codec/commons-codec/jars/commons-codec-1.10.jar",
      home + "/.ivy2/cache/org.parboiled/parboiled_2.12/jars/parboiled_2.12-2.1.3.jar",
      home + "/.ivy2/cache/org.picocontainer/picocontainer/jars/picocontainer-2.13.6.jar")

    .mkString(":")
// Since the classpath is relative to the bin directory, this must be run there
Process("java -classpath " + classpath + " org.nlogo.headless.Main --fullversion",
  cwd = new File("bin"))
  .lazyLines.foreach(println)

// 4.1 numbers from my home iMac on Sep. 13 2011, running Mac OS X Lion.
// quad-core 2.8 GHz Intel Core i5, memory 4 GB 1333 Mhz DDR3 - ST 9/13/11
// 6.1.1 numbers from my home MacPro on May. 1 2020, running Mac OS X Mojave.
// 6-core 2.6 GHz Intel Core i7, 16 GB 2400 MHz DDR4 - AAB 5/04/20
val results61 =
  Map("Ants" -> 2.582, "BZ" ->  3.401, "CA1D" -> 2.838, "Erosion" -> 2.495, "Fire" -> 0.099,
      "FireBig" -> 2.528, "Flocking" -> 1.440, "GasLabCirc" -> 2.697, "GasLabNew" -> 2.578,
      "GasLabOld" -> 1.990, "GridWalk" -> 3.850, "Heatbugs" -> 1.413, "Ising" -> 2.035,
      "Life" -> 2.728 , "PrefAttach" -> 1.644, "Team" -> 1.732, "Termites" -> 2.024,
      "VirusNet" -> 0.598, "Wealth" -> 2.326, "Wolf" ->  2.519)
val results41 =
  Map("Ants" -> 4.357, "BZ" -> 4.759, "CA1D" -> 4.453, "Erosion" -> 3.511, "Fire" -> 0.207,
      "FireBig" -> 3.680, "Flocking" -> 2.438, "GasLabCirc" -> 4.021, "GasLabNew" -> 4.601,
      "GasLabOld" -> 3.718, "GridWalk" -> 6.362, "Heatbugs" -> 2.913, "Ising" -> 3.748,
      "Life" -> 5.240, "PrefAttach" -> 5.004, "Team" -> 3.560, "Termites" -> 3.462,
      "VirusNet" -> 1.442, "Wealth" -> 4.282, "Wolf" -> 4.281)

val allNames: List[String] = {
  val nameArgs = args.takeWhile(!_.head.isDigit).toList
  if(!nameArgs.isEmpty) nameArgs
  else Process("find models/test/benchmarks -name *.nlogo -maxdepth 1")
         .lazyLines.map(_.split("/").last.split(" ").head).toList
}
allNames.foreach(name => results += (name -> new ListBuffer[Double]))
val width = allNames.map(_.size).max

def outputLines(name: String): LazyList[String] =
  Process("java -XX:+UseParallelGC -classpath " + classpath +
          " org.nlogo.headless.HeadlessBenchmarker " +
          name + args.dropWhile(!_.head.isDigit).mkString(" ", " ", ""),
        cwd = new File("bin"))
    .lazyLines
def record(name: String, line: String) : Unit = {
  val Match = ("@@@ " + name + """ Benchmark: (\d+\.\d+)( \(hit time limit\))?""").r
  val Match(num, warning) = line
  if (warning == null)
    haveGoodResult += name
  results(name) += num.toDouble
}

def printResults() : Unit = {
  val stringWriter = new java.io.StringWriter
  val pw = new java.io.PrintWriter(stringWriter)
  pw.println()

  for(name <- allNames; numbers = results(name); if !numbers.isEmpty) {
    val min = numbers.min
    pw.print(("%" + width + "s  %7.3f").format(name, min))
    if (results41.isDefinedAt(name) && results61.isDefinedAt(name))
      pw.print(" (%3.0f%% vs 4.1, %3.0f%% vs 6.1)".format(
             100 * min / results41(name),
             100 * min / results61(name)))
    if (!haveGoodResult(name)) pw.print(" (no reliable result yet)")
    pw.println()
  }
  def geometricMean(xs: List[Double]) = math.pow(xs.reduceLeft(_ * _), 1.0 / xs.size)

  def overall(oldResults: Map[String, Double]): Option[Double] = {
    val ratios = for {
      name <- allNames
      if results(name).nonEmpty
      oldResult <- oldResults.get(name)
    } yield results(name).min / oldResult
    if (ratios.nonEmpty) Some(geometricMean(ratios))
    else None
  }

  for {
    overall41 <- overall(results41)
    overall61 <- overall(results61)
  } pw.print("%21s(%3.0f%% vs 4.1, %3.0f%% vs 6.1.1)%n".format("", 100 * overall41, 100 * overall61))

  pw.println()
  val output = stringWriter.toString
  pw.close()
  print(output)

  val fpw = new java.io.PrintWriter(new java.io.File("tmp/bench.txt"))
  try { fpw.write(output) } finally { fpw.close() }
}

def runIt(name: String) : Unit = {
  for(out <- outputLines(name))
    if(!out.startsWith("@@@@@@"))
      if(out.startsWith("@@@ ")) { println(out); record(name, out) }
      else System.err.println(out)
  printResults()
}

def cleanUp() : Unit = {
  // remove files created by ImportWorld Benchmark
  for {
    files <- Option(new java.io.File("models/test/benchmarks/").listFiles)
    file <- files if file.getName.matches("firebig-[0-9]*.csv")
  } file.delete()
}

while(true) {
  allNames.foreach(runIt)
  // make extra efforts to get at least one good result for each model
  allNames.filter(!haveGoodResult(_)).foreach(runIt)
  cleanUp()
}

// Local Variables:
// mode: scala
// End:
