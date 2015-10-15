// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

// The purpose here is to run all the model checksums (in parallel!) and report
// what the slowest models were, so we can try to shorten their runtimes so the
// whole thing won't take so long.

import ChecksumsAndPreviews.Checksums

object ChecksumReport {

  val tester = new ChecksumTester(println _)
  import tester.info

  def main(args: Array[String]) {
    val entries = Checksums.load().values
    val results =
      for {
        entry <- entries.par
        millis <- time(entry)
      } yield entry.path -> millis
    printReport(results.seq.toMap)
  }

  def time(entry: Checksums.Entry): Option[Long] =
    try {
      print(".")
      val start = System.currentTimeMillis
      tester.testChecksum(
        entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
      Some(System.currentTimeMillis - start)
    }
    catch {
      case t: Throwable =>
        info(entry.path + ": ")
        t.printStackTrace()
        tester.addFailure(entry.path + ": " + t.getMessage)
        None
    }

  def printReport(runTimes: Map[String, Long]) {
    val numWinners = 30
    info(s"\n$numWinners slowest models:")
    val sorted =
      runTimes.keys.toSeq
        .sortBy(runTimes)
        .reverse
    for (key <- sorted.take(numWinners))
      info(s"  $key ${runTimes(key) / 1000} seconds")
    if (tester.failures.toString.nonEmpty)
      info("but there were failures...!")
  }

}
