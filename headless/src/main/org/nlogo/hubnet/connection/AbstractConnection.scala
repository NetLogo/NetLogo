// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import java.io._
import java.util.concurrent.TimeUnit
import java.net.SocketException
import org.nlogo.util.Exceptions

abstract class AbstractConnection(name: String, connectionStreams: Streamable) extends Thread(name) {
  private val writingThread = new WritingThread(name)

  // protected for testing. JC - 1/1/11
  protected val writeQueue = new java.util.concurrent.LinkedBlockingQueue[Any]

  private var count = 0
  private val RESET_DELAY = 1000

  protected val output = connectionStreams.getOutputStream
  private val input = connectionStreams.getInputStream
  private var keepListening = true
  @volatile private var keepWriting = true

  override def run() {
    writingThread.start()
    while (keepListening) {
      try receiveData(input.readObject())
      catch {
        case e@(_:InterruptedIOException|_:OptionalDataException|_:RuntimeException) =>
          handleEx(e.asInstanceOf[Exception], false)
        case e@(_:SocketException|_:EOFException) => keepListening = false
        case e: IOException => handleEx(e, false); keepListening = false
      }
    }
    disconnect("Shutting down.")
  }

  def receiveData(a: AnyRef)
  def handleEx(e: Exception, sendingEx: Boolean)

  // this should only be called if you need to wait for the send to
  // be completed it bypasses the writeQueue CB 09/28/2004
  @throws(classOf[java.io.IOException])
  def waitForSendData(a: Any) {send(a)}

  // use this method if you don't need to wait for the send to complete
  // this should only be used before stopWriting() is called.
  def sendData(a: Any) {
    // Note that the writingThread may be dead already at this point,
    // but it doesn't matter... the caller is *never* guaranteed that
    // messages are going to get through, since the client could vanish
    // at any time. - ST 11/22/04
    writeQueue.add(a)
  }

  // after a call to this, you should only call waitForSendData()
  // and not sendData() since the writingThread will be dead
  def stopWriting() { keepWriting = false }

  def disconnect(reason:String) {
    if (keepListening) {
      keepListening = false
      stopWriting()
      new Thread("SocketListener disconnect") {
        override def run() {
          def warn(body: => Unit) {
            Exceptions.warning(classOf[java.io.IOException]) { body }
          }
          Exceptions.handling(classOf[RuntimeException]) {
            warn(output.close())
            warn(input.close())
            warn(connectionStreams.close())
          }
        }
      }.start()
    }
  }

  def getSendQueueSize = writeQueue.size

  @throws(classOf[java.io.IOException])
  private def send(a: Any) {
    output.synchronized {
      output.writeObject(a)
      // we're not sure if this call to flush is absolutely necessary.
      // it can take up to .02 seconds.  however, since it is in a
      // background thread and we want to ensure that we send out the
      // messages as quickly as possible, let's leave it in.  if we
      // ever need this code in a non-background thread.  we should
      // revisit this issue.
      // --mag 10/14/02
      output.flush()
      // always do a reset on the outputstream so that we don't get
      // outofmemory errors from the serialized graph of object
      // building up
      // --mag 12/9/02
      // only resetting every 1000th message seems to decrease
      // bandwidth a good deal. --josh 11/19/09
      count += 1
      if (count % RESET_DELAY == 0) output.reset()
    }
  }

  private class WritingThread(name: String) extends Thread("WritingThread:" + name) {
    override def run() {
      while (keepWriting) {
        try
          try {
            val m = writeQueue.poll(100, TimeUnit.MILLISECONDS)
            if(m!=null) send(m)
          }
          catch { case ex: java.io.IOException => if (keepWriting) throw ex }
        catch {
          case e: java.io.InterruptedIOException => handleEx(e, true)
          case e: java.io.IOException =>
            handleEx(e, true)
            keepWriting = false
          case e: InterruptedException => Exceptions.warn(e)
        }
      }
    }
  }
}
