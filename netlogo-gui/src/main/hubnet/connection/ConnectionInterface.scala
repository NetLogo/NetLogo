// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import org.nlogo.core.{ Widget => CoreWidget }

trait ConnectionInterface {
  def enqueueMessage(msg:MessageEnvelope.MessageEnvelope)
  def modelWidgets: Seq[CoreWidget]
  // returns client window if in GUI mode, for theme synchronization (IB 11/14/24)
  def newClient(isRobo: Boolean, waitTime: Int): AnyRef
}
