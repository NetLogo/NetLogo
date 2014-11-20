// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

/**
 * Static utility methods and constants for the HubNet server.
 */
object HubNetUtils {

  /**The multicast group for server discovery */
  val SERVER_DISCOVERY_MULTICAST_GROUP = "228.5.8.80"

  /**The multicast port for server discovery */
  val SERVER_DISCOVERY_MULTICAST_PORT = 5885

  /**How frequently to broadcast server discovery method, in milliseconds */
  val SERVER_DISCOVERY_ANNOUNCE_INTERVAL = 1000

  // these are terrible - JC 8/21/10
  var viewMirroring = true
  var plotMirroring = false

  // this doesnt appear to be used - JC 8/21/10
  def checkPacketSize(obj: Object, message: String) {
    try {
      import java.io.{ObjectOutputStream, ByteArrayOutputStream}
      import java.util.zip.DeflaterOutputStream
      val deflatedBytesOutput = new ByteArrayOutputStream()
      val deflaterStream = new DeflaterOutputStream(deflatedBytesOutput)
      val defalterObjOutput = new ObjectOutputStream(deflaterStream)
      val byteOutput = new ByteArrayOutputStream()
      val objOutput = new ObjectOutputStream(byteOutput)
      defalterObjOutput.writeObject(obj)
      defalterObjOutput.flush()
      deflaterStream.finish()
      deflaterStream.flush()
      objOutput.writeObject(obj)
      println("deflated: " + message + deflatedBytesOutput.size + " bytes")
      println(message + byteOutput.size + " bytes")
    } catch {
        case e: java.io.IOException =>
          println("choked: ")
          e.printStackTrace()
    }
  }
}
