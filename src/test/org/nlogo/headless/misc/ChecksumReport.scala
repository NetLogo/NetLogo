// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

// The purpose here is to run all the model checksums and report what the
// slowest models were, so we can try to shorten the runtime so the whole
// thing won't take so long.

// I copied and pasted the thread pool stuff from TestCompileAll.  If I ever make a third copy,
// it's time to abstract, maybe using ExecutorServices.invokeAll - ST 2/12/09, 3/29/09

import java.util.concurrent.{ Executors, TimeUnit }

object ChecksumReport extends ChecksumTester(println _) {

  val runTimes = new collection.mutable.HashMap[String, Long]
          with collection.mutable.SynchronizedMap[String, Long]

  val executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)

  class Runner(entry: ChecksumsAndPreviews.Checksums.Entry) extends Runnable {
    override def run() {
      try {
        print(".")
        runTimes.update(entry.path, System.currentTimeMillis)
        // for now, store current time; we'll replace it later with the difference
        // between this value and the time then - ST 3/27/08
        testChecksum(entry.path, entry.worldSum, entry.graphicsSum, entry.revision)
        runTimes.update(entry.path, System.currentTimeMillis - runTimes(entry.path))
      }
      catch {
        case t: Throwable =>
          println(entry.path + ": ")
          t.printStackTrace()
          addFailure(entry.path + ": " + t.getMessage)
      }
    }
  }

  def main(args: Array[String]) {
    val checksums = ChecksumsAndPreviews.Checksums.load()
    for (entry <- checksums.values)
      executor.execute(new Runner(entry))
    executor.shutdown()
    executor.awaitTermination(java.lang.Integer.MAX_VALUE, TimeUnit.SECONDS)
    printReport(checksums.keySet.toSet, System.out)
  }

  def printReport(keys: Set[String], out: java.io.PrintStream) {
    // print report of longest runtimes (so we can alter preview commands to not take so long)
    val numWinners = 30
    out.println(numWinners + " slowest models:")
    val winners = keys.toSeq.sortBy(runTimes).reverse.take(numWinners)
    for (key <- winners)
      out.println("  " + key + " " + runTimes(key) / 1000 + " seconds")
    // done, whole test fails if any model failed
    if (failures.toString.nonEmpty)
      out.println("Test Failed!")
  }

}
