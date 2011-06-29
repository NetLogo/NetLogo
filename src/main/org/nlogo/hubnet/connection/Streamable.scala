package org.nlogo.hubnet.connection

import java.net.Socket
import java.io.{InputStream, OutputStream}

trait Streamable {
  def getOutputStream: OutputStream
  def getInputStream: InputStream
  def close()
}

object Streamable{
  def apply(socket:Socket) = new Streamable {
    def getOutputStream = socket.getOutputStream
    def getInputStream = socket.getInputStream
    def close() { socket.close() }
  }
}
