package org.nlogo.webstart.logging.message

sealed trait DirectorMessage

object DirectorMessage {
  case class ToDirectorWrite(data: String) extends DirectorMessage
  case object ToDirectorAbandon extends DirectorMessage
  case object ToDirectorFinalize extends DirectorMessage
  case object FromDirectorClosed extends DirectorMessage
}
