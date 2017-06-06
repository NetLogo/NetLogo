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

  class Updater[T <: ModelUpdate] {
    var lastUpdateTime = -1000L
    var latestStoredUpdate = Option.empty[T]
    var latestSentUpdate = Option.empty[T]

    def dueForUpdate: Boolean =
      currentTime - lastUpdateTime > updateInterval

    def registerNewUpdate(t: T) =
      latestStoredUpdate = Some(t)

    def sendUpdates() =
      if (dueForUpdate) {
        latestStoredUpdate.foreach { u =>
          lastUpdateTime = currentTime
          filteredUpdates.add(u)
          latestSentUpdate.foreach(filteredUpdates.remove)
          latestSentUpdate = Some(u)
        }
      }
  }

  val worldUpdater = new Updater[WorldUpdate]
  val monitorUpdater = new Updater[MonitorsUpdate]
  val allUpdaters = Seq(worldUpdater, monitorUpdater)

  def step(): Unit = {
    worldUpdates.poll(updateInterval, TimeUnit.MILLISECONDS) match {
      case wu@WorldUpdate(_, _)    => worldUpdater.registerNewUpdate(wu)
      case mu@MonitorsUpdate(_, _) => monitorUpdater.registerNewUpdate(mu)
      case other if other != null  => filteredUpdates.add(other)
      case _ =>
    }
    allUpdaters.foreach(_.sendUpdates())
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
