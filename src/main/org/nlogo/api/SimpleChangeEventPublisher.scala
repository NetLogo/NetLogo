// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.mutable.Publisher

sealed abstract trait SimpleChangeEvent
case object SimpleChangeEvent extends SimpleChangeEvent

/**
 * A most basic event publisher only warning subscribers that "something has changed"
 *  Currently used by TreeAgentSet to allow the nw extension to listen for changes
 *  and update its graph context accordingly. NP 2013-05-14.
 */
class SimpleChangeEventPublisher extends Publisher[SimpleChangeEvent] {
  override type Pub = Publisher[SimpleChangeEvent]
  def publish() { publish(SimpleChangeEvent) }
}
