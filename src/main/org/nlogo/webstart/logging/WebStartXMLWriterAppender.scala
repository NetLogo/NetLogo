package org.nlogo.webstart.logging

import java.net.URL
import org.nlogo.log.{XMLAppender, XMLLayout}
import org.apache.log4j.WriterAppender

class WebStartXMLWriterAppender(mode: LogSendingMode, destinations: URL*)
                               (implicit writer: LogProxyWriter = new LogProxyWriter(mode, destinations: _*))
                                extends WriterAppender(new XMLLayout, writer) with XMLAppender with WebStartAppender {
  def deleteLog() { writer.delete() }
}
