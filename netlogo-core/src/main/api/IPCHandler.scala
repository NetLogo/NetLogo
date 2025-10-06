// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ BufferedReader, InputStreamReader, OutputStream }
import java.net.{ InetAddress, ServerSocket, Socket, SocketException }

import scala.util.Try

// created as an abstraction for communication with BehaviorSpace processes,
// but could be used for other things in the future (Isaac B 10/2/25)
trait IPCHandler {
  protected var input: Option[BufferedReader] = None
  protected var output: Option[OutputStream] = None

  protected var connectionThread: Option[Thread] = None

  def connect(): Unit

  def readLine(): Try[String] = {
    connectionThread match {
      case Some(thread) if thread.isAlive =>
        thread.join()

      case _ =>
    }

    Try {
      input synchronized {
        input.map(_.readLine()).getOrElse(throw new Exception)
      }
    }
  }

  def writeLine(line: String): Try[Unit] = {
    connectionThread match {
      case Some(thread) if thread.isAlive =>
        thread.join()

      case _ =>
    }

    Try {
      output synchronized {
        output.foreach { stream =>
          stream.write(s"$line\n".getBytes)
          stream.flush()
        }
      }
    }
  }

  def close(): Unit
}

object IPCHandler {
  val Address = "127.0.0.1"
  val Port = 18711

  def apply(server: Boolean): IPCHandler = {
    if (server) {
      new IPCServerHandler
    } else {
      new IPCClientHandler
    }
  }
}

class IPCServerHandler extends IPCHandler {
  private var server: Option[ServerSocket] = None
  private var client: Option[Socket] = None

  override def connect(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        val server = new ServerSocket(IPCHandler.Port, 0, InetAddress.getByName(IPCHandler.Address))

        IPCServerHandler.this.server = Option(server)

        try {
          val client = server.accept()

          IPCServerHandler.this.client = Option(client)

          input = Option(new BufferedReader(new InputStreamReader(client.getInputStream, "UTF-8")))
          output = Option(client.getOutputStream)
        } catch {
          case _: SocketException => // ignore, most likely the connection was aborted by the client (Isaac B 10/6/25)
        }
      }
    }

    connectionThread = Option(thread)

    thread.start()
  }

  override def close(): Unit = {
    connectionThread.foreach(_.interrupt())
    client.foreach(_.close())
    server.foreach(_.close())
  }
}

class IPCClientHandler extends IPCHandler {
  private var socket: Option[Socket] = None

  override def connect(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        val socket = new Socket(IPCHandler.Address, IPCHandler.Port)

        IPCClientHandler.this.socket = Option(socket)

        input = Option(new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF-8")))
        output = Option(socket.getOutputStream)
      }
    }

    connectionThread = Option(thread)

    thread.start()
  }

  override def close(): Unit = {
    connectionThread.foreach(_.interrupt())
    socket.foreach(_.close())
  }
}
