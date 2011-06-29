package org.nlogo.hubnet.protocol

import java.io.{ObjectInputStream, ObjectOutputStream, OutputStream, InputStream}

object Protocol{
  def readProtocol(in:InputStream, out:OutputStream): Option[Protocol] = {
    val bytes = new Array[Byte](8)
    in.read(bytes)
    val name = new String(bytes).trim
    println("read protocol name: " + name)
    name match {
      case "Java" => Some(JavaSerializationProtocol(in, out))
      case "Binary" => Some(BinaryProtocol(in, out))
      case _ => None
    }
  }
}

trait Protocol {
  val name:String
  def readMessage(): Message
  def writeMessage(m:Message)
}

case class JavaSerializationProtocol(in:InputStream, out:OutputStream) extends Protocol {
  val name = "Java"
  lazy val objOut = new ObjectOutputStream(out) {}
  lazy val objIn = new ObjectInputStream(in)
  private var count = 0
  private val RESET_DELAY = 1000
  def readMessage(): Message = {
    objIn.readObject() match {
      case s:String => VersionMessage(s)
      case m:Message => m
      case bad => sys.error("unexpected message: " + bad)
    }
  }
  def writeMessage(m:Message) {
    objOut.writeObject(m)
    // we're not sure if this call to flush is absolutely necessary.
    // it can take up to .02 seconds.  however, since it is in a
    // background thread and we want to ensure that we send out the
    // messages as quickly as possible, let's leave it in.  if we
    // ever need this code in a non-background thread.  we should
    // revisit this issue.
    // --mag 10/14/02
    objOut.flush()
    // always do a reset on the outputstream so that we don't get
    // outofmemory errors from the serialized graph of object
    // building up
    // --mag 12/9/02
    // only resetting every 1000th message seems to decrease
    // bandwidth a good deal. --josh 11/19/09
    count += 1
    if (count % RESET_DELAY == 0) objOut.reset()
  }
}
