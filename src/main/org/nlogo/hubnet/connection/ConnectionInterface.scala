// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import org.nlogo.api.WidgetIO.WidgetSpec

trait ConnectionInterface {
  def enqueueMessage(msg:MessageEnvelope.MessageEnvelope)
  def getClientInterface: Iterable[WidgetSpec]
  def getControllerClientInterface: Iterable[WidgetSpec]
  def newClient(isRobo:Boolean, waitTime:Int)
}
