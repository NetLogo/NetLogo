// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

object MessageEnvelope {
  trait MessageType
  case object Normal extends MessageType
  case object Enter extends MessageType
  case object Exit extends MessageType

  case class ActivityMessageEnvelope(source: String, tag: String, message: Any)
          extends MessageEnvelope(Normal, source, Some(tag), Some(message))
  // Represents a HubNet node entering the simulation. The message source is the new client id.
  case class EnterMessageEnvelope(source: String) extends MessageEnvelope(Enter, source, null, null)
  // Represents a HubNet node leaving the simulation. The message source is
  case class ExitMessageEnvelope(source: String) extends MessageEnvelope(Exit, source, null, null)

  /**
   * Represents a message from a HubNet node (and associated meta-data) at an
   * abstract level. This is used by HubNetNodeConnections to enqueue messages
   * in the HubNetManager. It includes all message-related data that is relevant
   * to the HNManager and the primitives, inluding a generic payload.
   */
  class MessageEnvelope(
          private val kind: MessageType = Normal,
          private val source: String, /* the source of this message (a client id). */
          private val tag: Option[String], /* the message tag. */
          private val message: Option[Any] /* the message itself. */ ) {
    def getSource = source
    def isEnterMessage = kind == Enter
    def isExitMessage = kind == Exit

    @throws(classOf[HubNetException]) def getMessage = if (kind == Normal) message.get else throwError(kind, "")
    @throws(classOf[HubNetException]) def getTag: String = if (kind == Normal) tag.get else throwError(kind, "-tag")
    @throws(classOf[HubNetException])
    private def throwError(messageType: MessageType, messageSuffix: String) = {
      throw new HubNetException(
        "hubnet-message" + messageSuffix + " cannot be used on an " + messageType.toString.toLowerCase + " message. " +
                "Use hubnet-" + messageType.toString.toLowerCase + "-message? instead.")
    }
  }
}
