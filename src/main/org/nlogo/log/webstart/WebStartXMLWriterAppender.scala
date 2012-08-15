package org.nlogo.log.webstart

import java.net.URL
import org.nlogo.log.{XMLAppender, XMLLayout}
import org.apache.log4j.WriterAppender

/*
 The `RemoteLogWriter` pretty much needs to be a default param in a sneaky second parameter list here, because:
   a) I don't want the user of this class to worry about constructing `RemoteLogWriter`s
   b) We have to extend `WriterAppender` here
   c) A `Writer` needs to be passed to the constructor of `WriterAppender` in order for us to extend it
 */
class WebStartXMLWriterAppender(mode: LogSendingMode, destinations: URL*)
                               (writer: RemoteLogWriter = new RemoteLogWriter(mode, destinations: _*))
                                extends WriterAppender(new XMLLayout, writer) with XMLAppender with WebStartAppender {
  /*none*/ def deleteLog()  { writer.delete() }
  /*none*/ def initialize() { initializeTransformer(destinations.mkString("|"), writer) }
  override def close()      { closeDocument(); super.close() }
}
