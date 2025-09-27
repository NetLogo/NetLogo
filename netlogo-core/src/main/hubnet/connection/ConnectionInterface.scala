// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import org.nlogo.core.{ Widget => CoreWidget }

trait ConnectionInterface {
  def enqueueMessage(msg:MessageEnvelope.MessageEnvelope): Unit
  def modelWidgets: Seq[CoreWidget]
  // returns client window if in GUI mode, for theme synchronization (Isaac B 11/14/24)
  def newClient(isRobo: Boolean, waitTime: Int): Option[AnyRef]
}
