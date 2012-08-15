package org.nlogo.log

import java.io.Writer
import java.net.URL
import org.apache.log4j.{Appender, WriterAppender}

/*
 The `RemoteLogWriter` pretty much needs to be a default param in a sneaky second parameter list here, because:
   a) I don't want the user of this class to worry about constructing `RemoteLogWriter`s
   b) We have to extend `WriterAppender` here
   c) A `Writer` needs to be passed to the constructor of `WriterAppender` in order for us to extend it
 */
class WebStartXMLWriterAppender(mode: LogSendingMode, destinations: URL*)
                               (writer: RemoteLogWriter = new RemoteLogWriter(mode, destinations: _*))
                               extends WriterAppender(new XMLLayout, writer) with XMLAppender with WebStartAppender {
  /*none*/ def deleteLog() { writer.delete() }
  /*none*/ def initialize() { initializeTransformer(destinations.mkString("|"), writer) }
  override def close() { closeDocument(); super.close() }
}


// An interface for log-handling `Appender`s that operate through Java WebStart
trait WebStartAppender extends Appender {
  /*none*/ def deleteLog()
  /*none*/ def initialize()
}


// A `Writer` subclass for writing log entries to remote locations
class RemoteLogWriter(mode: LogSendingMode, destinations: URL*) extends Writer {
  import DirectorMessage.{ToDirectorWrite, ToDirectorAbandon, ToDirectorFinalize}
  private val director = new LogDirector(mode, destinations: _*).start()
  override def write(cbuf: Array[Char], off: Int, len: Int) { director ! ToDirectorWrite(cbuf.subSequence(off, off + len).toString) }
  override def flush()  { /* While the Director manages its own flushing, making this throw an exception breaks everything; ignore operation */ }
  /*none*/ def delete() { director !? ToDirectorAbandon }
  override def close()  { director !? ToDirectorFinalize } // It's important that this blocks while waiting for operation to complete
}
