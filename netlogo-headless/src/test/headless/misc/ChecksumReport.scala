// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import scala.collection.parallel.CollectionConverters.ArrayIsParallelizable

// The purpose here is to run all the model checksums (in parallel!) and report
// what the slowest models were, so we can try to shorten their runtimes so the
// whole thing won't take so long.

object ChecksumReport {

  val tester = new ChecksumTester

  def main(args: Array[String]): Unit = {
    printReport(ChecksumsAndPreviews.checksumEntries().par.map { entry =>
      time(entry).map((entry.modelPath.toString, _))
    }.flatten.seq.toMap)
  }

  def time(entry: ChecksumsAndPreviews.Entry): Option[Long] =
    try {
      print(".")
      val start = System.currentTimeMillis
      tester.testChecksum(entry)
      Some(System.currentTimeMillis - start)
    }
    catch {
      case t: Throwable =>
        println(s"${entry.modelPath}: ")
        t.printStackTrace()
        tester.fail(s"${entry.modelPath}: ${t.getMessage}")
        None
    }

  def printReport(runTimes: Map[String, Long]): Unit = {
    val numWinners = 30
    println(s"\n$numWinners slowest models:")
    val sorted =
      runTimes.keys.toSeq
        .sortBy(runTimes)
        .reverse
    for (key <- sorted.take(numWinners))
      println(s"  $key ${runTimes(key) / 1000} seconds")
  }

}
