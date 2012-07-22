// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.Observer
import org.nlogo.api.SimpleJobOwner

object Benchmarker {
  // Stop when we are 98% confident that we're within 0.3% of the truth, using the formula
  // n = (z * stddev / interval) ^ 2   - ST 12/9/02, 1/27/09
  private val Z           = 2.3263  // z value for 98% confidence, according to the standard normal table
  private val TOLERANCE   = 0.003   // goal: get within 0.3%
  private val formatter   = new java.text.DecimalFormat("0.000")
  def benchmark(workspace:AbstractWorkspace,minTime:Int,maxTime:Int) {
    val times = new collection.mutable.ListBuffer[Double]
    val goProcedure = workspace.compileCommands("ca benchmark")
    val resultProcedure = workspace.compileReporter("result")
    val owner = new SimpleJobOwner("Benchmarker", workspace.world.mainRNG)
    def goOnce():Double = {
      workspace.runCompiledCommands(owner, goProcedure)
      val result = workspace.runCompiledReporter(owner, resultProcedure).asInstanceOf[Double]
      assert(result > 0)
      result
    }
    def average = total / times.size
    def total = times.sum
    def squareOfDifference = times.map(time => math.pow(time - average, 2)).sum
    def stddev = math.sqrt(squareOfDifference / times.size)
    def runs = 2 max math.ceil(math.pow(Z * stddev / (TOLERANCE * average),2)).toInt
    def warmUp() {
      println("(" + workspace.modelNameForDisplay + ")")
      System.gc()
      val startTime = System.currentTimeMillis
      while(System.currentTimeMillis - startTime < minTime * 1000)
        goOnce()
    }
    def done = total > minTime && (times.size >= runs || total >= maxTime)
    var lastChatterTime = 0L
    def chatter() {
      if(System.currentTimeMillis - lastChatterTime > 10000) { // every 10 seconds
        println(
          times.size + "/" + runs + " (mean=" + formatter.format(average) +
          ", stddev=" + formatter.format(stddev) + ")")
        lastChatterTime = System.currentTimeMillis
      }
    }
    def debrief() {
      lastChatterTime = 0L
      chatter()
      println("@@@ " + workspace.modelNameForDisplay + ": " + formatter.format(average) +
              (if(times.size < runs) " (hit time limit)" else ""))

    }
    warmUp()
    while(!done) { times += goOnce(); if(!done) chatter() }
    debrief()
  }
}
