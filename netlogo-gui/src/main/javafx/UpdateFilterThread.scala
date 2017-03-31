// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue, TimeUnit }
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }

import javafx.application.Platform

import org.nlogo.internalapi.{ ModelUpdate, MonitorsUpdate, WorldUpdate }
import org.nlogo.agent.World


trait UpdateFilter {
  def worldUpdates:     BlockingQueue[ModelUpdate]
  def updateInterval:   Int
  def currentTime:      Long
  def processUpdates(): Unit

  val filteredUpdates = new LinkedBlockingQueue[ModelUpdate]()

  var lastProcessRun: Long = -1000

  var latestWorldUpdate:   (Long, Option[WorldUpdate])    = (-1000, None)
  var latestMonitorUpdate: (Long, Option[MonitorsUpdate]) = (-1000, None)

  def step(): Unit = {
    worldUpdates.poll(updateInterval, TimeUnit.MILLISECONDS) match {
      case wu@WorldUpdate(_, _)    => latestWorldUpdate   = (latestWorldUpdate._1,   Some(wu))
      case mu@MonitorsUpdate(_, _) => latestMonitorUpdate = (latestMonitorUpdate._1, Some(mu))
      case other if other != null  => filteredUpdates.add(other)
      case _ =>
    }
    if (currentTime - latestWorldUpdate._1 > updateInterval) {
      latestWorldUpdate._2.foreach { wu =>
        filteredUpdates.add(wu)
        latestWorldUpdate = (currentTime, None)
      }
    }
    if (currentTime - latestMonitorUpdate._1 > updateInterval) {
      latestMonitorUpdate._2.foreach { mu =>
        filteredUpdates.add(mu)
        latestMonitorUpdate = (currentTime, None)
      }
    }
    if (currentTime - lastProcessRun > updateInterval) {
      processUpdates()
      lastProcessRun = currentTime
    }
  }
}

// This thread handles filtering updates to keep the UI thread from getting behind on world updates
// interval is given in ms, callback is called on the Platform thread
class UpdateFilterThread(val worldUpdates: BlockingQueue[ModelUpdate],
  val updateInterval: Int,
  periodicCallback: () => Unit) extends Thread("Update Filter") with UpdateFilter {

  def currentTime: Long = System.currentTimeMillis

  @volatile
  var dying = false

  def die(): Unit = {
    dying = true
    interrupt()
    join()
  }

  override def run(): Unit = {
    while (! dying) {
      try {
        step()
      } catch {
        case i: InterruptedException =>
      }
    }
  }

  def processUpdates(): Unit = {
    Platform.runLater(new Runnable() {
      override def run(): Unit = {
        periodicCallback()
      }
    })
  }
}
