package org.nlogo.webstart.logging.message

// Establishing a message-set through which the logging actors can communicate with one another
private[logmanager] sealed trait LogManagementMessage

private[logmanager] object LogManagementMessage {
  case class  Write(data: String) extends LogManagementMessage
  case object Read extends LogManagementMessage
  case object Abandon extends LogManagementMessage
  case object Finalize extends LogManagementMessage
  case object Flush extends LogManagementMessage
}
