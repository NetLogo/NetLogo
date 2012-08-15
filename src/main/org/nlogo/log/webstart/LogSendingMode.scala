package org.nlogo.log.webstart

sealed trait LogSendingMode

// An enumeration of modes that `LogDirector` can utilize
object LogSendingMode {
  case object Continuous extends LogSendingMode
  case object AfterLoggingCompletes extends LogSendingMode
}
