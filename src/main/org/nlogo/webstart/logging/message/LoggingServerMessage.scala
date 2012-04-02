package org.nlogo.webstart.logging.message

private[logmanager] sealed trait LoggingServerMessage

private[logmanager] object LoggingServerMessage {

  private val Sep = "|"

  case class ToServerWrite(data: String) {
    override def toString: String = "write%s%s".format(Sep, data)
  }

  case object ToServerPulse {
    override def toString: String = "pulse"
  }

  case object ToServerAbandon {
    override def toString: String = "abandon"
  }

  case object ToServerFinalize {
    override def toString: String = "finalize"
  }

}
