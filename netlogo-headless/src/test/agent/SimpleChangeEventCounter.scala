package org.nlogo.agent

import org.nlogo.api.SimpleChangeEventPublisher
import org.nlogo.api.SimpleChangeEvent

class SimpleChangeEventCounter(pub: SimpleChangeEventPublisher) extends SimpleChangeEventPublisher#Sub {
  pub.subscribe(this)
  var eventCount: Int = 0
  override def notify(pub: SimpleChangeEventPublisher#Pub, event: SimpleChangeEvent.type) {
    eventCount += 1
  }
}
