#!/bin/sh
exec bin/scala -deprecation -classpath bin -nocompdaemon "$0" "$@" 
!# 

// Local Variables:
// mode: scala
// End:

import Scripting.shell

import collection.mutable.{HashMap,ListBuffer,HashSet}
val results = new HashMap[String,ListBuffer[Double]]
val haveGoodResult = new HashSet[String]

shell("""java -classpath target/scala_2.9.1/classes:project/boot/scala-2.9.1/lib/scala-library.jar:resources org.nlogo.headless.Main --fullversion""")
  .foreach(println)

val results40 = 
  Map("Ants" -> 7.471, "BZ" -> 7.839, "CA1D" -> 5.826, "Erosion" -> 6.292, "Fire" -> 0.273,
      "FireBig" -> 7.099, "Flocking" -> 7.023, "GasLabCirc" -> 6.439, "GasLabNew" -> 7.465,
      "GasLabOld" -> 6.067, "GridWalk" -> 7.104, "Heatbugs" -> 6.286, "Ising" -> 6.414,
      "Life" -> 8.986, "PrefAttach" -> 7.129, "Team" -> 4.466, "Termites" -> 5.228,
      "VirusNet" -> 3.090, "Wealth" -> 5.5750, "Wolf" -> 6.988)
val results41 =
  Map("Ants" -> 6.767, "BZ" -> 7.706, "CA1D" -> 5.782, "Erosion" -> 5.968, "Fire" -> 0.266,
      "FireBig" -> 7.058, "Flocking" -> 7.275, "GasLabCirc" -> 6.046, "GasLabNew" -> 7.626,
      "GasLabOld" -> 6.185, "GridWalk" -> 7.162, "Heatbugs" -> 5.919, "Ising" -> 5.736,
      "Life" -> 8.851, "PrefAttach" -> 7.747, "Team" -> 5.289, "Termites" -> 6.578,
      "VirusNet" -> 3.457, "Wealth" -> 5.813, "Wolf" -> 6.498)

val allNames:List[String] = {
  val nameArgs = args.takeWhile(!_.head.isDigit).toList
  if(!nameArgs.isEmpty) nameArgs
  else shell("""find test/models/benchmarks -name \*.nlogo -depth 1""")
        .map(_.split("/").last.split(" ").head).toList
}
allNames.foreach(name => results += (name -> new ListBuffer[Double]))
val width = allNames.map(_.size).max

def outputLines(name:String):Iterator[String] =
  shell("make bench ARGS=\"" + name + args.dropWhile(!_.head.isDigit).mkString(" "," ","") + "\"")
def record(name:String,line:String) {
  val Match = ("@@@ " + name + """ Benchmark: (\d+\.\d+)( \(hit time limit\))?""").r
  val Match(num,warning) = line
  if(warning == null) haveGoodResult += name
  results(name) += num.toDouble
}
def printResults() {
  println()
  for(name <- allNames; numbers = results(name); if !numbers.isEmpty) {
    val min = numbers.min
    printf("%" + width + "s  %7.3f",name,min)
    if(results40.isDefinedAt(name) && results41.isDefinedAt(name))
      printf(" (%3.0f%% vs 4.0, %3.0f%% vs 4.1)",
             100 * min / results40(name),
             100 * min / results41(name))
    if(!haveGoodResult(name)) print(" (no reliable result yet)")
    println()
  }
  def geometricMean(xs:List[Double]) = math.pow(xs.reduceLeft(_ * _), 1.0 / xs.size)
  def overall(oldResults:Map[String,Double]) =
    geometricMean(allNames.filter(x => results(x).nonEmpty && oldResults.isDefinedAt(x))
                          .map(name => results(name).min / oldResults(name)))
  printf("                    (%3.0f%% vs 4.0, %3.0f%% vs 4.1)",
         100 * overall(results40), 100 * overall(results41))
  println(); println()
}
def runIt(name:String) {
  for(out <- outputLines(name))
    if(!out.startsWith("@@@@@@"))
      if(out.startsWith("@@@ ")) { println(out); record(name,out) }
      else System.err.println(out)
  printResults()
}
while(true) {
  allNames.foreach(runIt)
  // make extra efforts to get at least one good result for each model
  allNames.filter(!haveGoodResult(_)).foreach(runIt)
}
