package org.nlogo.webstart.logging.message

private[logging] sealed trait LoggingServerMessage

private[logging] object LoggingServerMessage {

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
