// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import org.nlogo.core.{ Widget => CoreWidget }

trait ConnectionInterface {
  def enqueueMessage(msg:MessageEnvelope.MessageEnvelope)
  // this is terrible
  def getClientInterface: Seq[CoreWidget]
  def newClient(isRobo:Boolean, waitTime:Int)
}
