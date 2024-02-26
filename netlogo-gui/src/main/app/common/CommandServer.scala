// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.nlogo.app.common.CommandLine
import org.nlogo.api.FileIO
import org.nlogo.awt.EventQueue
import org.nlogo.core.AgentKind

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import org.scalasbt.ipcsocket.{ UnixDomainServerSocket, Win32NamedPipeServerSocket }

import scala.jdk.CollectionConverters._
import scala.sys.addShutdownHook
import scala.util.Properties.isWin

import java.net.ServerSocket
import java.io.{ BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter }
import java.nio.file.{ Files, Paths }

private class CommandRequest(var agent: AgentKind = AgentKind.Observer, var code: String = "") {
  // Example: {"type": "nl-run-code", "code": "show 123"}
  def fromJSONString(input: String): Boolean = {
    val parser = new JSONParser()
    var successful = false

    try {
      val obj = parser.parse(input).asInstanceOf[JSONObject]

      val type_ = obj.get("type").asInstanceOf[String]
      if (type_ != "nl-run-code") {
        throw RuntimeException("Unsupported remote command: " + type_)
      }

      code = obj.get("code").asInstanceOf[String]

      if (code == null) {
        throw RuntimeException("No code provided")
      } else {
        successful = true
      }
    } catch {
      case e: RuntimeException => System.err.println(e.getMessage)
      case _ => System.err.println("Failed to parse remote command")
    }

    successful
  }
}

private class CommandThread(serverSocket: ServerSocket, callback: CommandRequest => Unit) extends Thread {
  override def run() = {
    while (true) {
      val request = new CommandRequest
      val socket = serverSocket.accept

      val input = socket.getInputStream
      val reader = new BufferedReader(new InputStreamReader(input))

      val output = socket.getOutputStream
      val writer = new BufferedWriter(new OutputStreamWriter(output))

      for (line <- reader.lines.iterator.asScala) {
        if (request.fromJSONString(line)) {
          callback(request)
          writer.write(s"{\"type\": \"nl-status\", \"status\": \"ok\"}\n")
        } else {
          writer.write(s"{\"type\": \"nl-status\", \"status\": \"failed\", \"message\": \"Failed to parse request\"}\n")
        }

        writer.flush
      }

      input.close
      output.close
      socket.close
    }
  }
}

class CommandServer(commandLine: CommandLine) {

  makeServerSocket().foreach(x => new CommandThread(x, commandCallback).start)

  private def commandCallback(request: CommandRequest) = {
    EventQueue.invokeAndWait(() => commandLine.execute(AgentKind.Observer, request.code, true))
  }

  private def getServerSocketPath: String = {
    val pid: Long = ProcessHandle.current.pid

    if (isWin) {
      s"\\\\.\\pipe\\netlogo-$pid"
    } else {
      Option(System.getenv("XDG_RUNTIME_DIR")) match {
        case Some(x) => s"${x}/netlogo-$pid"
        case None => FileIO.perUserFile(s"netlogo-$pid", false)
      }
    }
  }

  private def makeServerSocket(): Option[ServerSocket] = {
    val path = getServerSocketPath
    val socket =
      try {
        if (isWin) {
          Some(new Win32NamedPipeServerSocket(path))
        } else {
          addShutdownHook(Files.deleteIfExists(Paths.get(path)))
          Some(new UnixDomainServerSocket(path))
        }
      } catch {
        case _ => {
          System.err.println(s"Failed to create remote command socket at ${path}")
          None
        }
      }

    println(s"Created remote command socket at ${path}")
    socket
  }
}
