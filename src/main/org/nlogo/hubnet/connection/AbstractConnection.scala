package org.nlogo.hubnet.connection

import java.io._
import java.util.concurrent.TimeUnit
import java.net.SocketException
import org.nlogo.util.Exceptions
import org.nlogo.hubnet.protocol.{Protocol, Message}

abstract class AbstractConnection(name: String, connectionStreams: Streamable, isClient:Boolean=false) extends Thread(name) {
  private val writingThread = new WritingThread(name)

  // this used to be ArrayBlockingQueue[Any](10000)
  // but, i really don't think we expect more than a few messages at a time in here.
  // the Array implementation takes up vastly more memory than the LinkedList.
  // so with small message queues, we can support more clients using LinkedList.
  // its also very suspicious that we have such a high capacity. i think it
  // should probably be considerably lower, 500 or less. if 500 messages back up
  // and haven't been sent, thats a pretty good indication that something is
  // wrong with the connection. (Note, changing to to 1000 now)
  // another note, SocketListener is also used in the swing clients. but its
  // very unlikely and maybe even impossible for them to fill this queue up.
  // (also, making this protected for testing. JC - 1/1/11)
  // I see no reason to have any ceiling at all here.  (LinkedBlockingQueue
  // doesn't make us set a ceiling.) - ST 1/12/11
  protected val writeQueue = new java.util.concurrent.LinkedBlockingQueue[Message](1000)

  protected val output = connectionStreams.getOutputStream
  private val input = connectionStreams.getInputStream
  @volatile private var keepListening = true
  @volatile private var keepWriting = true

  protected var protocol:Protocol = null
  def protocolName: String = protocol.name

  def connType = if(isClient) "client" else "server"

  def writeProtocol() {
    output.write(protocolName.getBytes)
    output.flush()
  }

  private def doProtocolExchange() {
    def readProtocol() {
      //TODO: figure out what to do here if the protocol is None.
      protocol = Protocol.readProtocol(in = input, out = output).get
    }
    if(isClient) { writeProtocol(); readProtocol() }
    else { readProtocol(); writeProtocol() }
  }

  doProtocolExchange()

  override def run() {
    writingThread.start()
    while (keepListening) {
      try receiveData(protocol.readMessage())
      catch {
        case e@(_:InterruptedIOException|_:OptionalDataException|_:RuntimeException) =>
          handleEx(e.asInstanceOf[Exception], false)
        case e@(_:SocketException|_:EOFException) => keepListening = false
        case e: IOException => handleEx(e, false); keepListening = false
      }
    }
    disconnect("Shutting down.")
  }

  def receiveData(m:Message)
  def handleEx(e: Exception, sendingEx: Boolean)

  // this should only be called if you need to wait for the send to
  // be completed it bypasses the writeQueue CB 09/28/2004
  @throws(classOf[java.io.IOException])
  def waitForSendData(m: Message) {send(m)}

  // use this method if you don't need to wait for the send to complete
  // this should only be used before stopWriting() is called.
  def sendData(m: Message) {
    // Note that the writingThread may be dead already at this point,
    // but it doesn't matter... the caller is *never* guaranteed that
    // messages are going to get through, since the client could vanish
    // at any time. - ST 11/22/04
    writeQueue.add(m)
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
  private def send(m: Message) {
    output.synchronized { protocol.writeMessage(m) }
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
