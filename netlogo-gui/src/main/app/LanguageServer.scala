// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.{ BufferedReader, InputStream, InputStreamReader, OutputStream }
import java.net.{ ServerSocket, Socket }
import java.security.MessageDigest
import java.util.Base64

import org.nlogo.api.ExtensionManager
import org.nlogo.core.{ Token, TokenType }
import org.nlogo.nvm.PresentationCompilerInterface

import ujson.{ Arr, Obj }

class LanguageServer(compiler: PresentationCompilerInterface, extensionManager: ExtensionManager) extends Thread {
  override def run(): Unit = {
    val server = new ServerSocket(5588)

    val client: Socket = server.accept()

    val input: InputStream = client.getInputStream
    val output: OutputStream = client.getOutputStream

    if (processHandshake(input, output)) {
      while (processFrame(input, output)) {}
    }

    server.close()
  }

  private def processHandshake(input: InputStream, output: OutputStream): Boolean = {
    val reader = new BufferedReader(new InputStreamReader(input))

    val lines: LazyList[String] = LazyList.continually(reader.readLine).takeWhile(_.nonEmpty)

    lines.find(_.startsWith("Sec-WebSocket-Key")) match {
      case Some(s"Sec-WebSocket-Key: $key") =>
        val bytes: Array[Byte] = s"${key}258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes
        val responseKey: String = Base64.getEncoder.encodeToString(MessageDigest.getInstance("SHA-1").digest(bytes))

        output.write(Seq(
          "HTTP/1.1 101 Switching Protocols",
          "Upgrade: websocket",
          "Connection: Upgrade",
          s"Sec-WebSocket-Accept: $responseKey"
        ).mkString("", "\r\n", "\r\n\r\n").getBytes)

        true

      case _ =>
        output.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes)
        output.close()

        false
    }
  }

  private def processFrame(input: InputStream, output: OutputStream): Boolean = {
    try {
      val c1: Int = input.read

      if (c1 == -1) {
        output.close()

        return false
      }

      val fin: Int = (c1 >> 7) & 0b1
      val code: Int = c1 & 0b1111

      if (fin != 1 || code != 1) {
        output.close()

        return false
      }

      val c2: Int = input.read

      if (c2 == -1) {
        output.close()

        return false
      }

      val masked: Int = (c2 >> 7) & 0b1
      var length: Int = c2 & 0b1111111

      if (masked != 1) {
        output.close()

        return false
      }

      if (length == 126) {
        length = (input.read << 8) | input.read
      } else if (length == 127) {
        length = LazyList.tabulate(8)(i => input.read << (56 - i * 8)).reduce(_ | _)
      }

      val mask: Array[Int] = LazyList.fill(4)(input.read).toArray

      processMessage(new String(LazyList.tabulate(length)(i => (input.read ^ mask(i % 4)).toByte).toArray), output)
    } catch {
      case _ =>
        false
    }
  }

  private def processMessage(data: String, output: OutputStream): Boolean = {
    try {
      val json: Obj = ujson.read(data).obj

      val line: Int = json("line").num.toInt
      val text: String = json("text").str
      val tokens: Array[Token] = compiler.tokenizeForColorization(text, extensionManager).dropRight(1)

      val response: Array[Byte] = ujson.write(Obj(
        "line" -> line,
        "tokens" -> Arr(tokens.map(token => Obj(
          "from" -> token.start,
          "to" -> token.end,
          "type" -> typeNum(token.tpe)
        ))*)
      )).getBytes

      if (response.size <= 125) {
        output.write(0b10000001.toByte +: response.size.toByte +: response)
      } else if (response.size < 0xFFFF) {
        output.write(0b10000001.toByte +: 126.toByte +: ((response.size >> 8) & 0xFF).toByte +: (response.size & 0xFF).toByte +: response)
      } else {
        val lengthBytes: Array[Byte] = LazyList.tabulate(8)(i => ((response.size >> (56 - i * 8)) & 0xFF).toByte).toArray

        output.write(0b10000001.toByte +: 127.toByte +: lengthBytes :++ response)
      }

      true
    } catch {
      case _ =>
        false
    }
  }

  private def typeNum(tpe: TokenType): Int = {
    tpe match {
      case TokenType.Comment => 0
      case TokenType.OpenParen => 1
      case TokenType.CloseParen => 2
      case TokenType.OpenBracket => 3
      case TokenType.CloseBracket => 4
      case TokenType.OpenBrace => 5
      case TokenType.CloseBrace => 6
      case TokenType.Keyword => 7
      case TokenType.Literal => 8
      case TokenType.Command => 9
      case TokenType.Reporter => 10
      case TokenType.Ident => 11
      case _ => -1
    }
  }
}
