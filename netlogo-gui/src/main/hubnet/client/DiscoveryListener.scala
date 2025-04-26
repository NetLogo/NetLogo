// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.io.IOException
import java.net.{DatagramPacket, InetAddress, InetSocketAddress, MulticastSocket, NetworkInterface, SocketTimeoutException}
import java.util.Arrays
import org.nlogo.hubnet.protocol.DiscoveryMessage

object DiscoveryListener {
  /**The multicast group for server discovery */
  val SERVER_DISCOVERY_MULTICAST_GROUP = "228.5.8.80" // NOPMD pmd doesn't like hardcoded IPv4 addresses
  /**The multicast port for server discovery */
  val SERVER_DISCOVERY_MULTICAST_PORT = 5885
}

/**
 * The discovery listener joins a multicast group, waits for messages
 * broadcast by a  { @link org.nlogo.hubnet.server.DiscoveryAnnouncer }
 * in the server. When a message is received, it notifies an  { @link AnnouncementListener }.
 * @see org.nlogo.hubnet.protocol.DiscoveryMessage
 */
class DiscoveryListener(@volatile var interfaceAddress: Option[InetAddress]) extends Thread {
  import DiscoveryListener._

  // this should probably be changed to support a set of listeners like the
  // general java idiom.

  // The announcement listener to be notified if a message is received.
  private var listener: AnnouncementListener = _
  private var shouldKeepListening = true

  /**
   * Sets the specified announcement listener to receive messages from this discovery listener.
   * @param listener the announcement listener
   */
  def setAnnouncementListener(listener: AnnouncementListener): Unit = {
    synchronized {this.listener = listener}
  }

  /**
   * Removes the specified announcement listener so that it
   * no longer receives messages from this discovery listener.
   * @param listener the announcement listener
   */
  def removeAnnouncementListener(listener: AnnouncementListener): Unit = {
    synchronized {if (listener == this.listener) this.listener = null}
  }

  /**
   * Stops the discovery listener from listening for messages on the multicast group.
   */
  def stopListening(): Unit = {shouldKeepListening = false}

  /**
   * Notifies the registered announcement listener that a
   * message has been received.
   */
  private def notifyListeners(m: DiscoveryMessage): Unit = {
    if (listener != null) listener.synchronized {listener.announcementReceived(m)}
  }

  /**
   * Joins a multicast group and listens for packets
   * until <code> stopListening() </code> is called.
   */
  override def run(): Unit = {
    // arbitrary, but possibly larger than many MTUs, but this doesn't
    // really matter on the client, unless zero termination matters...
    val receiptBuffer = Array.ofDim[Byte](1024)
    var multicastSocket: MulticastSocket = null
    while (shouldKeepListening) {
      interfaceAddress.foreach { address =>
        Arrays.fill(receiptBuffer, 0.toByte)
        // the -1 should, hopefully, force zero termination upon truncation of large packets...
        val packet = new DatagramPacket(receiptBuffer, receiptBuffer.length - 1)
        val group = InetAddress.getByName(SERVER_DISCOVERY_MULTICAST_GROUP)
        val socketGroup = new InetSocketAddress(group, SERVER_DISCOVERY_MULTICAST_PORT)
        val netIf = NetworkInterface.getByInetAddress(address)
        try {
          multicastSocket = new MulticastSocket(SERVER_DISCOVERY_MULTICAST_PORT)
          multicastSocket.setSoTimeout(3000)
          multicastSocket.setNetworkInterface(netIf)
          multicastSocket.joinGroup(socketGroup,netIf)
          multicastSocket.receive(packet)
          notifyListeners(DiscoveryMessage.deserialize(packet.getAddress.getHostName, packet.getData))
        }
        catch {
          case timeout: SocketTimeoutException =>
          case ex: java.security.AccessControlException =>
            System.err.println(ex)
            System.err.println(ex.getMessage)
            org.nlogo.api.Exceptions.ignore(ex)
          case ex: IOException =>
            System.err.println(ex)
            System.err.println(ex.getMessage)
            org.nlogo.api.Exceptions.ignore(ex)
        }
        finally {
          if (multicastSocket != null) {
            multicastSocket.leaveGroup(socketGroup, netIf)
            multicastSocket.close()
          }
        }
      }
    }
  }
}
