// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import scala.collection.mutable.Publisher
import scala.collection.mutable.Subscriber

/**
 * Convenience class to make scala.collection.mutable.{ Publisher, Subscriber }
 * simpler to use. 
 */
class SimplePublisher[E] extends Publisher[E] {
  override type Pub = SimplePublisher[E]
  def newSubscriber(f: E => Unit): SimpleSubscriber[Pub, E] = {
    val subscriber = new SimpleSubscriber[Pub, E](f)
    super.subscribe(subscriber)
    subscriber
  }
  override def publish(event: E): Unit = super.publish(event) // to make public
}

class SimpleSubscriber[P, E](f: E => Unit) extends Subscriber[E, P] {
  override def notify(pub: P, event: E): Unit = f(event)
}
