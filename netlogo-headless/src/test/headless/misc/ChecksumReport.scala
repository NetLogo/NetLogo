// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.nlogo.workspace.Checksummer

// The purpose here is to run all the model checksums (in parallel!) and report
// what the slowest models were, so we can try to shorten their runtimes so the
// whole thing won't take so long.

// addendum: it doesn't work properly in parallel for some reason, but it doesn't
// take that long sequentially so it's not worth it to fix at the moment. maybe
// fix later. (Isaac B 12/10/25)

object ChecksumReport {
  def main(args: Array[String]): Unit = {
    printReport(ChecksumsAndPreviews.checksumEntries().map { entry =>
      time(entry).map((entry.modelPath.toString, _))
    }.flatten.toSeq)
  }

  def time(entry: ChecksumsAndPreviews.Entry): Option[Long] = {
    println(entry.modelPath)

    val workspace = HeadlessWorkspace.newInstance

    try {
      workspace.silent = true

      workspace.open(entry.modelPath.toString, true)

      val start = System.currentTimeMillis

      Checksummer.initModelForChecksumming(workspace, entry.variant.getOrElse(""))

      val end = System.currentTimeMillis

      workspace.dispose()

      Some(end - start)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()

        None
    } finally {
      workspace.dispose()
    }
  }

  def printReport(runTimes: Seq[(String, Long)]): Unit = {
    val numWinners = 30

    println(s"\n$numWinners slowest models:")

    runTimes.sortBy(_._2).reverse.take(numWinners).foreach { (model, time) =>
      println(s"  $model ${time / 1000} seconds")
    }
  }
}
