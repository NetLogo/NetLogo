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
import scala.sys.{ addShutdownHook, ShutdownHookThread }
import scala.util.Properties.isWin
import scala.util.Try

import java.net.{ ServerSocket, Socket }
import java.io.{ BufferedReader, BufferedWriter, InputStream, InputStreamReader, OutputStream, OutputStreamWriter }
import java.nio.file.{ Files, Paths }

private class CommandRequest(var code: String = "") {
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

private class CommandThread(serverSocket: CommandServerSocket, callback: CommandRequest => Unit) extends Thread {
  override def run() = {
    var socket: Option[Socket] = None
    var input: Option[InputStream] = None
    var output: Option[OutputStream] = None

    try {
      while (!isInterrupted) {
        socket = Some(serverSocket.accept())
        input = socket.map(_.getInputStream())
        output = socket.map(_.getOutputStream())

        (input, output) match {
          case (Some(x), Some(y)) => processRequests(x, y)
          case _ => throw new IllegalStateException("Failed to open input/output stream")
        }
      }
    } catch {
      case _: InterruptedException => ()
    } finally {
      input.foreach(_.close)
      output.foreach(_.close)
      socket.foreach(_.close)
    }
  }

  private def processRequests(input: InputStream, output: OutputStream): Unit = {
    val request = new CommandRequest
    val reader = new BufferedReader(new InputStreamReader(input))
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
  }
}

class CommandServerSocket {
  val path: String = getSocketPath
  private var shutdownHookThread: Option[ShutdownHookThread] = None
  private val socket: ServerSocket = makeSocket(getSocketPath)

  private def getSocketPath: String = {
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

  private def makeSocket(path: String): ServerSocket = {
    println(s"Creating remote command socket at ${path}")

    if (isWin) {
      new Win32NamedPipeServerSocket(path)
    } else {
      shutdownHookThread = Some(addShutdownHook(Files.deleteIfExists(Paths.get(path))))
      new UnixDomainServerSocket(path)
    }
  }

  def accept(): Socket = {
    socket.accept()
  }

  def close(): Unit = {
    socket.close()
    shutdownHookThread.foreach({x =>
      x.run()
      x.remove()
    })
  }
}

class CommandServer(commandLine: CommandLine) {

  private var serverSocket: Option[CommandServerSocket] = None
  private var serverThread: Option[Thread] = None

  def start(): Unit = {
    stop()
    initServerSocket()
    serverThread = serverSocket.map(x => new CommandThread(x, commandCallback))
    serverThread.map(_.start)
  }

  def stop(): Unit = {
    serverThread.foreach(_.interrupt())
    serverThread = None
    serverSocket.foreach(_.close())
    serverSocket = None
  }

  def running: Boolean = serverThread.isDefined

  private def commandCallback(request: CommandRequest) = {
    EventQueue.invokeAndWait(() => commandLine.execute(AgentKind.Observer, request.code, true))
  }

  private def initServerSocket(): Unit = {
    serverSocket.foreach(_.close())
    serverSocket = Try(new CommandServerSocket).toOption

    if (serverSocket.isEmpty) {
      System.err.println(s"Failed to create remote command socket")
    }
  }
}
