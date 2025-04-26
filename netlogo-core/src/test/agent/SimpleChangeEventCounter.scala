// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ SimpleChangeEvent, SimpleChangeEventPublisher }
import org.nlogo.core.Listener

class SimpleChangeEventCounter(pub: SimpleChangeEventPublisher) extends Listener[SimpleChangeEvent.type] {
  pub.subscribe(this)
  var eventCount: Int = 0
  override def handle(e: SimpleChangeEvent.type): Unit = {
    eventCount += 1
  }
}
