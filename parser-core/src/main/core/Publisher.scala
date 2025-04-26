// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.collection.mutable.Set

// this class replaces scala.collection.mutable.Publisher, which has been deprecated (Isaac B 4/24/25)
trait Publisher[T] {
  private val listeners = Set[Listener[T]]()

  def subscribe(listener: Listener[T]): Unit = {
    listeners += listener
  }

  def subscribe(listen: (T) => Unit): Unit = {
    listeners += new Listener[T] {
      def handle(e: T): Unit = {
        listen(e)
      }
    }
  }

  def unsubscribe(listener: Listener[T]): Unit = {
    listeners -= listener
  }

  def publish(e: T): Unit = {
    listeners.foreach(_.handle(e))
  }
}

abstract class Listener[T] {
  def handle(e: T): Unit
}
