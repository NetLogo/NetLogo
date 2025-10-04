// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ BufferedReader, InputStreamReader }
import java.net.{ InetAddress, ServerSocket, Socket }

import scala.util.Try

// created as an abstraction for communication with BehaviorSpace processes,
// but could be used for other things in the future (Isaac B 10/2/25)
trait IPCHandler {
  def readLine(): Try[String]
  def writeLine(line: String): Try[Unit]
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
  val server = new ServerSocket(IPCHandler.Port, 0, InetAddress.getByName(IPCHandler.Address))
  val client = server.accept()
  val input = new BufferedReader(new InputStreamReader(client.getInputStream, "UTF-8"))
  val output = client.getOutputStream

  def readLine(): Try[String] = {
    Try {
      input synchronized {
        input.readLine()
      }
    }
  }

  def writeLine(line: String): Try[Unit] = {
    Try {
      output synchronized {
        output.write(s"$line\n".getBytes)
        output.flush()
      }
    }
  }

  def close(): Unit = {
    server.close()
  }
}

class IPCClientHandler extends IPCHandler {
  val socket = new Socket(IPCHandler.Address, IPCHandler.Port)
  val input = new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF-8"))
  val output = socket.getOutputStream

  def readLine(): Try[String] =
    Try(input.readLine())

  def writeLine(line: String): Try[Unit] = {
    Try {
      output.write(s"$line\n".getBytes)
      output.flush()
    }
  }

  def close(): Unit = {
    socket.close()
  }
}
