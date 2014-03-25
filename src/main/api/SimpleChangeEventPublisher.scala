// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.mutable.Publisher

object SimpleChangeEvent

/**
 * A most basic event publisher only warning subscribers that "something has changed"
 *  Currently used by TreeAgentSet to allow the nw extension to listen for changes
 *  and update its graph context accordingly. NP 2013-05-14.
 */
class SimpleChangeEventPublisher extends Publisher[SimpleChangeEvent.type] {
  override type Pub = Publisher[SimpleChangeEvent.type]
  def publish() { publish(SimpleChangeEvent) }
}
