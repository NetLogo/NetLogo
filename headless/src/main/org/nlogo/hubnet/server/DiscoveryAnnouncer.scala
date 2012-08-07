// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import java.io.IOException
import java.net.{DatagramPacket, InetAddress, MulticastSocket, UnknownHostException}
import HubNetUtils._
import org.nlogo.hubnet.protocol.DiscoveryMessage

/**
 * Repeatedly broadcasts a message to a multicast group announcing
 * the existence of this server. Messages are received by
 * a org.nlogo.hubnet.client.DiscoveryListener on the client.
 * @see org.nlogo.hubnet.protocol.DiscoveryMessage
 **/
class DiscoveryAnnouncer(uniqueId: String, modelName: String, portNumber: Int) extends Thread {
  private val message = DiscoveryMessage(uniqueId, modelName, portNumber)
  @volatile private var shouldRun = true
  def shutdown() { shouldRun = false }

  /**
   * Broadcasts messages on the multicast group
   * as long as <code> shouldRun </code> is true.
   **/
  override def run(): Unit = {
    def dump(err: String, ex: Exception) {
      System.err.println("@ " + new java.util.Date() + " : ")
      System.err.println("Error: " + err + "\n\t" + ex)
    }
    val group = try InetAddress.getByName(SERVER_DISCOVERY_MULTICAST_GROUP)
    catch {
      case uhe: UnknownHostException =>
        dump("could not map the multicast group " + SERVER_DISCOVERY_MULTICAST_GROUP + " to an InetAddress", uhe)
        return
    }
    val multicastSocket = try new MulticastSocket(SERVER_DISCOVERY_MULTICAST_PORT)
    catch {
      case ioe: IOException =>
        dump("Error creating multicast socket to port " + SERVER_DISCOVERY_MULTICAST_PORT, ioe)
        return
    }
    while (shouldRun) {
      val messageBytes = message.toByteArray
      try {
        multicastSocket.setTimeToLive(63)
        multicastSocket.send(new DatagramPacket(messageBytes, messageBytes.length, group, SERVER_DISCOVERY_MULTICAST_PORT))
      }
      catch {case ioe: IOException => dump("Could not transmit multicast announcement.", ioe)}
      try Thread.sleep(SERVER_DISCOVERY_ANNOUNCE_INTERVAL)
      catch { case ie: InterruptedException => dump("Multicast announcement thread interrupted.\n\t", ie) }
    }
    try multicastSocket.close()
    catch { case e: RuntimeException => org.nlogo.util.Exceptions.ignore(e) }
  }
}
