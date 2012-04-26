package org.nlogo.webstart.logging

sealed trait LogSendingMode

object LogSendingMode {
  case object Continuous extends LogSendingMode
  case object AfterLoggingCompletes extends LogSendingMode
}
