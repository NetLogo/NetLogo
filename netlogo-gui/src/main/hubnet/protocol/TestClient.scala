// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import java.net.Socket
import org.nlogo.api.TwoDVersion
import org.nlogo.api.HubNetInterface.ClientInterface
import java.io.{IOException, ObjectOutputStream}
import org.nlogo.util.ClassLoaderObjectInputStream
import java.util.concurrent.{Executors, ExecutorService, TimeUnit, LinkedBlockingQueue}
import collection.JavaConverters._

object TestClient{
  implicit val pool = Executors.newCachedThreadPool()
  // just putting this hear because i'm anticipating calling it from java (android)
  // if we don't use it, it can be removed later.
  // really, id like to figure out how to write android apps in scala.
  def create(userId: String, clientType: String, ip:String, port:Int) =
    new TestClient(userId, clientType, ip, port, pool)
}

case class TestClient(userId: String, clientType: String="COMPUTER", ip:String="127.0.0.1", port:Int=9173,
                       executor: ExecutorService=TestClient.pool){
  import org.nlogo.hubnet.protocol.{ViewUpdate => ViewUp}

  private val socket = new Socket(ip, port) {setSoTimeout(0)}
  private val in = ClassLoaderObjectInputStream(
    Thread.currentThread.getContextClassLoader, socket.getInputStream)
  private val out = new ObjectOutputStream(socket.getOutputStream)

  // public api
  val (activityName, interfaceSpec) = handshake()
  lazy val messagesReceived = new LinkedBlockingQueue[Message]

  def sendActivityCommand(message:String, content: Any){
    send(new ActivityCommand(message, content.asInstanceOf[AnyRef]))
  }

  def close(reason:String){ send(ExitMessage(reason)) }
  def getWidgetControls: List[WidgetControl] =
    messagesReceived.asScala.collect{ case wc: WidgetControl => wc }.toList
  def getViewUpdates: List[ViewUp] =
    messagesReceived.asScala.collect{ case vu: ViewUp => vu }.toList

  def nextMessage(timeoutMillis:Long=200): Option[Message] =
    Option(messagesReceived.poll(timeoutMillis, TimeUnit.MILLISECONDS))

  // attempts the handshake and explodes if it fails
  // called from the constructor.
  private def handshake(): (String, ClientInterface) = {
    def sendAndReceive(a: AnyRef): AnyRef = {
      rawSend(a)
      in.readObject()
    }
    try{
      sendAndReceive(TwoDVersion.version)
      val response = sendAndReceive(new HandshakeFromClient(userId, clientType))
      val result = response match {
        case h: HandshakeFromServer =>
          send(EnterMessage)
          executor.submit(new Receiver())
          (h.activityName, h.clientInterface)
        case r => throw new IllegalStateException(userId + " handshake failed. response:" + r)
      }
      result
    } catch {
      case e:Exception => throw new IllegalStateException("dead client: " + userId)
    }
  }

  // sends a message to the server
  protected def send(a: AnyRef) = { rawSend(a) }

  protected def rawSend(a: AnyRef){
    out.writeObject(a)
    out.flush()
  }

  private var _dead = false
  def dead = _dead

  // i suppose its quite posible that we don't need this business at all
  // instead, when we need the next message, we can just read it off the socket.
  // we could then get rid of the exector altogether..
  private class Receiver extends Runnable {
    override def run() {
      try {
        messagesReceived.put(in.readObject.asInstanceOf[Message])
        executor.submit(this)
      } catch {
        // keep track of death so that users of TestClient know.
        // previously we were just dropping this here. bad. JC - 3/31/11
        case e:IOException => _dead = true
      }
    }
  }
}
