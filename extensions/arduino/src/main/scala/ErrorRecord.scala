package arduino

case class ErrorRecord(inboundString: String,
  errorDescription: String,
  exception: Option[Exception])

