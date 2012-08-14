package org.nlogo.log.webstart

sealed trait LogSendingMode

object LogSendingMode {
  case object Continuous extends LogSendingMode
  case object AfterLoggingCompletes extends LogSendingMode
}
