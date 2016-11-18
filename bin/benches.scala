#!/bin/sh
exec $SCALA_HOME/bin/scala -nocompdaemon -deprecation -classpath bin -Dfile.encoding=UTF-8 "$0" "$@"
!#

import sys.process.Process
import collection.mutable.{ HashMap, ListBuffer, HashSet }

val results = new HashMap[String, ListBuffer[Double]]
val haveGoodResult = new HashSet[String]

val home = System.getenv("HOME")

val classpath =
  Seq("netlogo-gui/target/classes",
      "shared/target/classes",
      "netlogo-gui/resources",
      home + "/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.0.jar",
      home + "/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.12/bundles/scala-parser-combinators_2.12-1.0.4.jar",
      home + "/.ivy2/cache/org.ow2.asm/asm-all/jars/asm-all-5.0.4.jar",
      home + "/.ivy2/cache/log4j/log4j/jars/log4j-1.2.16.jar",
      home + "/.ivy2/cache/org.parboiled/parboiled_2.12/jars/parboiled_2.12-2.1.3.jar",
      home + "/.ivy2/cache/org.picocontainer/picocontainer/jars/picocontainer-2.13.6.jar")
    .mkString(":")
Process("java -classpath " + classpath + " org.nlogo.headless.Main --fullversion")
  .lineStream.foreach(println)

// 4.0 & 4.1 numbers from my home iMac on Sep. 13 2011, running Mac OS X Lion.
// quad-core 2.8 GHz Intel Core i5, memory 4 GB 1333 Mhz DDR3 - ST 9/13/11
val results40 =
  Map("Ants" -> 4.797, "BZ" -> 4.816, "CA1D" -> 4.714, "Erosion" -> 3.663, "Fire" -> 0.206,
      "FireBig" -> 3.585, "Flocking" -> 2.399, "GasLabCirc" -> 3.955, "GasLabNew" -> 4.268,
      "GasLabOld" -> 3.533, "GridWalk" -> 6.099, "Heatbugs" -> 3.160, "Ising" -> 4.042,
      "Life" -> 5.481, "PrefAttach" -> 4.863, "Team" -> 2.839, "Termites" -> 3.298,
      "VirusNet" -> 1.272, "Wealth" -> 4.193, "Wolf" -> 4.321)
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
         .lineStream.map(_.split("/").last.split(" ").head).toList
}
allNames.foreach(name => results += (name -> new ListBuffer[Double]))
val width = allNames.map(_.size).max

def outputLines(name: String): Stream[String] =
  Process("java -classpath " + classpath +
          " org.nlogo.headless.HeadlessBenchmarker " +
          name + args.dropWhile(!_.head.isDigit).mkString(" ", " ", ""))
    .lineStream
def record(name: String, line: String) {
  val Match = ("@@@ " + name + """ Benchmark: (\d+\.\d+)( \(hit time limit\))?""").r
  val Match(num, warning) = line
  if (warning == null)
    haveGoodResult += name
  results(name) += num.toDouble
}

def printResults() {
  val stringWriter = new java.io.StringWriter
  val pw = new java.io.PrintWriter(stringWriter)
  pw.println()
  for(name <- allNames; numbers = results(name); if !numbers.isEmpty) {
    val min = numbers.min
    pw.print(("%" + width + "s  %7.3f").format(name, min))
    if (results40.isDefinedAt(name) && results41.isDefinedAt(name))
      pw.print(" (%3.0f%% vs 4.0, %3.0f%% vs 4.1)".format(
             100 * min / results40(name),
             100 * min / results41(name)))
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
    overall40 <- overall(results40)
    overall41 <- overall(results41)
  } pw.print("%21s(%3.0f%% vs 4.0, %3.0f%% vs 4.1)%n".format("", 100 * overall40, 100 * overall41))

  pw.println()
  val output = stringWriter.toString
  pw.close()
  print(output)

  val fpw = new java.io.PrintWriter(new java.io.File("tmp/bench.txt"))
  try { fpw.write(output) } finally { fpw.close() }
}

def runIt(name: String) {
  for(out <- outputLines(name))
    if(!out.startsWith("@@@@@@"))
      if(out.startsWith("@@@ ")) { println(out); record(name, out) }
      else System.err.println(out)
  printResults()
}

def cleanUp() {
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
