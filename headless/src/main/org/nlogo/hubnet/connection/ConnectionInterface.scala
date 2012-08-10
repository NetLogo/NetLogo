// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

trait ConnectionInterface {
  def enqueueMessage(msg:MessageEnvelope.MessageEnvelope)
  // this is terrible
  def getClientInterface: Array[String]
  def newClient(isRobo:Boolean, waitTime:Int)
}
