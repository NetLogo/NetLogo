#!/usr/bin/env scala -nocompdaemon -deprecation -Dfile.encoding=UTF-8
//!#

import sys.process.Process
import collection.mutable.{ HashMap, ListBuffer, HashSet }

val results = new HashMap[String, ListBuffer[Double]]
val haveGoodResult = new HashSet[String]

Process(Seq("./sbt", "run-main org.nlogo.headless.Main --fullversion"))
  .lines.foreach(println)

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
         .lines.map(_.split("/").last.split(" ").head).toList
}
allNames.foreach(name => results += (name -> new ListBuffer[Double]))
val width = allNames.map(_.size).max

def outputLines(name: String): Stream[String] = {
  val command = "run-main org.nlogo.headless.HeadlessBenchmarker " +
    name + args.dropWhile(!_.head.isDigit).mkString(" ", " ", "")
  Process(Seq("./sbt", command))
    .lines
}
def record(name: String, line: String) {
  val Match = ("@@@ " + name + """ Benchmark: (\d+\.\d+)( \(hit time limit\))?""").r
  val Match(num, warning) = line
  if (warning == null)
    haveGoodResult += name
  results(name) += num.toDouble
}
def printResults() {
  println()
  for(name <- allNames; numbers = results(name); if !numbers.isEmpty) {
    val min = numbers.min
    printf("%" + width + "s  %7.3f", name, min)
    if(results40.isDefinedAt(name) && results41.isDefinedAt(name))
      printf(" (%3.0f%% vs 4.0, %3.0f%% vs 4.1)",
             100 * min / results40(name),
             100 * min / results41(name))
    if(!haveGoodResult(name)) print(" (no reliable result yet)")
    println()
  }
  def geometricMean(xs: List[Double]) = math.pow(xs.reduceLeft(_ * _), 1.0 / xs.size)
  def overall(oldResults: Map[String, Double]) =
    geometricMean(allNames.filter(x => results(x).nonEmpty && oldResults.isDefinedAt(x))
                          .map(name => results(name).min / oldResults(name)))
  printf("                    (%3.0f%% vs 4.0, %3.0f%% vs 4.1)",
         100 * overall(results40), 100 * overall(results41))
  println(); println()
}
def runIt(name: String) {
  for(out <- outputLines(name))
    if(!out.startsWith("@@@@@@"))
      if(out.startsWith("@@@ ")) { println(out); record(name, out) }
      else System.err.println(out)
  printResults()
}
while(true) {
  allNames.foreach(runIt)
  // make extra efforts to get at least one good result for each model
  allNames.filter(!haveGoodResult(_)).foreach(runIt)
}
