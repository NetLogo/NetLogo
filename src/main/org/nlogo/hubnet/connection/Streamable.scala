package org.nlogo.hubnet.connection

import java.net.Socket
import java.io.{ObjectInputStream, ObjectOutputStream}

trait Streamable {
  def getOutputStream: ObjectOutputStream
  def getInputStream: ObjectInputStream
  def close(): Unit
}

object Streamable{
  def apply(socket:Socket) = new Streamable {
    def getOutputStream: ObjectOutputStream = {
      new ObjectOutputStream(socket.getOutputStream){
        // I don't know if this fixes anything but the ObjectInputStream constructor
        // reads a header from the output stream, so maybe this will fix the
        // StreamCorruptedException at the meeting on 9/2/08, it can't hurt, right?
        // ev 9/4/08
        flush()
      }
    }
    def getInputStream: ObjectInputStream = new ObjectInputStream(socket.getInputStream)
    def close(): Unit = socket.close()
  }
}
