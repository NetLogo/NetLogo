// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue, TimeUnit }
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }

import javafx.application.Platform

import org.nlogo.internalapi.{ ModelUpdate, WorldUpdate }
import org.nlogo.agent.World

// This thread handles filtering updates to keep the UI thread from getting behind on world updates
// interval is given in ms, callback is called on the Platform thread
class UpdateFilterThread(worldUpdates: BlockingQueue[ModelUpdate], periodicInterval: Int, periodicCallback: () => Unit) extends Thread("Update Filter") {

  val filteredUpdates = new LinkedBlockingQueue[ModelUpdate]()
  var latestWorld = new AtomicReference[(World, Long)]((null, 0))
  val updatePending = new AtomicBoolean(false)
  private var lastUpdateRequest: Long = 0
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
        worldUpdates.poll(periodicInterval, TimeUnit.MILLISECONDS) match {
          case null                         =>
          case WorldUpdate(world: World, t) =>
            latestWorld.set((world, t))
          case other                        => filteredUpdates.put(other)
        }
        requestUpdateIfNeeded()
      } catch {
        case i: InterruptedException =>
      }
    }
  }

  private def requestUpdateIfNeeded(): Unit = {
    val currentTime = System.currentTimeMillis
    if (currentTime - lastUpdateRequest > periodicInterval && ! updatePending.get()) {
      updatePending.set(true)
      Platform.runLater(new Runnable() {
        override def run(): Unit = {
          updatePending.set(false)
          periodicCallback()
        }
      })
      lastUpdateRequest = currentTime
    }
  }
}
