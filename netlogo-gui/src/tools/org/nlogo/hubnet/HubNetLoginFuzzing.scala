// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet

import java.net.Socket
import java.io.{ ObjectOutputStream}
import java.util.concurrent.Executors // , ExecutorService, TimeUnit, LinkedBlockingQueue}

import org.nlogo.api.Version
import org.nlogo.util.ClassLoaderObjectInputStream
import protocol.{ ActivityCommand, EnterMessage, HandshakeFromClient, HandshakeFromServer }

import scala.util.Random

object HubNetLoginFuzzing extends App {
  sealed trait Action
  case class SendAction(send: Send) extends Action
  case object WaitOneMessage extends Action

  sealed trait Send
  case class SendVersion(version: String) extends Send
  case class SendHandshake(clientId: String) extends Send
  case object SendEnter extends Send
  case class SendActivityMessage(tag: String, value: AnyRef) extends Send

  lazy val threadPool = Executors.newFixedThreadPool(4)

  val random = new Random(0)

  val actions: Iterator[ActionRunnable] =
    Iterator
      .continually(makeActions(random))
      .map(makeActionRunnables _)


  actions.take(400).foreach(threadPool.submit _)

  threadPool.shutdown()

  while (! threadPool.isTerminated()) { }

  actions.foreach { a =>
    println(a.result)
  }

  def makeActions(rand: Random): Seq[Action] = {
    rand.nextInt(8) match {
      case 0 =>
        Seq(SendAction(SendVersion(Version.version)), WaitOneMessage)
      case 1 =>
        Seq(SendAction(SendVersion(Version.version)), WaitOneMessage, SendAction(SendHandshake(randString(rand))), WaitOneMessage)
      case 2 =>
        Seq(SendAction(SendHandshake(randString(rand))), WaitOneMessage)
      case 3 =>
        Seq(SendAction(SendVersion(Version.version)), WaitOneMessage, SendAction(SendHandshake(null)), WaitOneMessage)
      case 4 =>
        Seq(SendAction(SendVersion(Version.version)), WaitOneMessage,
          SendAction(SendHandshake(randString(rand))), WaitOneMessage,
          SendAction(SendEnter))
      case 5 =>
        Seq(SendAction(SendVersion(Version.version)), WaitOneMessage,
          SendAction(SendHandshake(randString(rand))), WaitOneMessage,
          SendAction(SendEnter),
          SendAction(SendActivityMessage("Choice", Double.box(3))), WaitOneMessage)
      case 6 =>
        Seq(SendAction(SendVersion(Version.version)),
          SendAction(SendHandshake(randString(rand))),
          SendAction(SendEnter),
          SendAction(SendEnter))
      case 7 =>
        Seq(SendAction(SendVersion(Version.version)),
          SendAction(SendHandshake(randString(rand))),
          SendAction(SendHandshake(randString(rand))),
          SendAction(SendEnter))
    }
  }

  private def randString(rand: Random): String = {
    (1 to 5).map(_ => rand.nextPrintableChar()).mkString("")
  }

  def makeActionRunnables(actions: Seq[Action]): ActionRunnable =
    new ActionRunnable(actions)

  class ActionRunnable(actions: Seq[Action]) extends Runnable {
    sealed trait Event
    case class PendingSend(send: Send) extends Event
    case class Sent(send: Send) extends Event
    case class Received(any: AnyRef) extends Event {
      override def toString = any match {
        case hs: HandshakeFromServer => "Received(HandshakeFromServer(" + hs.activityName + "...))"
        case _ => "Received(" + any.toString + ")"
      }
    }
    case class ExceptionRaised(e: Exception) extends Event
    case object Timeout extends Event
    case object SocketClosed extends Event

    var events: List[AnyRef] = Nil
    var remainingActions = actions

    def makeConnection(): Option[(Socket, ObjectOutputStream, ClassLoaderObjectInputStream)] = {
      var socket: Socket = null
      var out: ObjectOutputStream = null
      var in: ClassLoaderObjectInputStream = null

      try {
        while (socket == null || out == null || in == null) {
          socket = new Socket("127.0.0.1", 9173) { setSoTimeout(250) }
          while (! socket.isConnected()) {}

          while (socket != null && out == null) {
            try {
              out = new ObjectOutputStream(socket.getOutputStream)
            } catch {
              case _: java.net.SocketException =>
                socket = null
                println("output stream opening exception...")
            }
          }
          while (socket != null && in == null) {
            try {
              in = ClassLoaderObjectInputStream(Thread.currentThread.getContextClassLoader, socket.getInputStream)
            } catch {
              case _: java.net.SocketException =>
                socket = null
                println("input stream opening exception...")
            }
          }
        }
        Some((socket, out, in))
      } catch {
        case e: Exception =>
          ExceptionRaised(e) :: events
          None
      }
    }

    override def run() {
      makeConnection().foreach {
        case (socket, out, in) =>
          try {
            while (remainingActions.nonEmpty && ! socket.isClosed()) {
              remainingActions.head match {
                case SendAction(send: Send) =>
                  events = PendingSend(send) :: events
                  send match {
                    case SendVersion(v) =>
                      out.writeObject(v); out.flush()
                    case SendHandshake(clientId) =>
                      out.writeObject(HandshakeFromClient(clientId, "COMPUTER")); out.flush()
                    case SendEnter =>
                      out.writeObject(EnterMessage); out.flush()
                    case SendActivityMessage(tag, value) =>
                      out.writeObject(ActivityCommand(tag, value)); out.flush()
                  }
                  events = Sent(send) :: events.tail
                    case WaitOneMessage =>
                      try {
                        val obj = in.readObject()
                        events = Received(obj) :: events
                      } catch {
                        case to: java.net.SocketTimeoutException =>
                          events = Timeout :: events
                      }
              }
              remainingActions = remainingActions.tail
            }
            if (socket.isClosed()) {
              events = SocketClosed :: events
            }
          } catch {
            case e: Exception =>
              events = ExceptionRaised(e) :: events
              e.printStackTrace()
          } finally {
            if (socket != null) {
              socket.close()
            }
          }
      }
    }

    def result: String = {
      actions.mkString(",") + " => " + events.reverse.mkString(",")
    }
  }

  // messages:
  // Version (right / wrong)
  // HandshakeFromClient (clientId, "COMPUTER")
  // EnterMessage
  // ActivityCommand (tag, value)
}
