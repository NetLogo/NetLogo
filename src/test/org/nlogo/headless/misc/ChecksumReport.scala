// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

// I copied and pasted the thread pool stuff from TestCompileAll.  If I ever make a third copy,
// it's time to abstract, maybe using ExecutorServices.invokeAll - ST 2/12/09, 3/29/09

import java.util.concurrent.{ Executors, TimeUnit }

object ChecksumReport extends ChecksumTester(println _) {

  def main(args: Array[String]) {

    val checksums = ChecksumsAndPreviews.Checksums.load()

    val runTimes = new collection.mutable.HashMap[String, Long]
            with collection.mutable.SynchronizedMap[String, Long]

    val executor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)

    for (entry <- checksums.values) {
      def doIt() {
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
      executor.execute(new Runnable { def run() { doIt() }})
    }
    executor.shutdown()
    executor.awaitTermination(java.lang.Integer.MAX_VALUE, TimeUnit.SECONDS)
    // print report of longest runtimes (so we can alter preview commands to not take so long)
    val n = 30
    println(n + " slowest models:")
    val keys = checksums.keySet.toSeq.sortBy(runTimes).reverse.take(n)
    for (key <- keys)
      println("  " + key + " " + runTimes(key) / 1000 + " seconds")
    // done, whole test fails if any model failed
    if (failures.toString.nonEmpty)
      println("Test Failed!")

  }

}
