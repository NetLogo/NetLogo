package org.nlogo.webstart.logging

import java.net.URL
import org.nlogo.log.{XMLAppender, XMLLayout}
import org.apache.log4j.WriterAppender

class WebStartXMLWriterAppender(mode: LogSendingMode, destinations: URL*)
                               (implicit writer: LogProxyWriter = new LogProxyWriter(mode, destinations: _*))
                                extends WriterAppender(new XMLLayout, writer) with XMLAppender with WebStartAppender {
  /*none*/ def deleteLog()  { writer.delete() }
  /*none*/ def initialize() { initializeTransformer(destinations.mkString("|"), writer) }
  override def close()      { closeDocument(); super.close() }
}
