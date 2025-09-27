// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

import java.net.Socket
import java.io.{ InputStream, ObjectStreamClass, ObjectInputStream, ObjectOutputStream }

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
    def getInputStream: ObjectInputStream =
      new VersionIDMismatchIgnoringObjectInputStream(socket.getInputStream)
    def close(): Unit = socket.close()
  }
}

// This fixes #1364, a spurious serialVersionUID mismatch.  Basically we just ignore all such
// mismatches and keep on trucking.  The version check on the version of NetLogo itself should
// prevent any problems.  This leaves us free to use ProGuard in ways that alter automatically
// calculated serialVersionUIDs without breaking HubNet.  thank you
// stackoverflow.com/questions/795470/how-to-deserialize-an-object-persited-in-a-db-now-when-the-obect-has-different-se/796589#796589
// !!!!!! - ST 8/17/11
private class VersionIDMismatchIgnoringObjectInputStream(in: InputStream)
extends ObjectInputStream(in) {
  override protected def readClassDescriptor: ObjectStreamClass = {
    var resultClassDescriptor = super.readClassDescriptor
    val localClass = Class.forName(resultClassDescriptor.getName)
    if (localClass == null) {
      // println("No local class for " + resultClassDescriptor.getName);
      resultClassDescriptor
    }
    else {
      val localClassDescriptor = ObjectStreamClass.lookup(localClass)
      if (localClassDescriptor != null) { // only if class implements serializable
        val localSUID = localClassDescriptor.getSerialVersionUID
        val streamSUID = resultClassDescriptor.getSerialVersionUID
        if (streamSUID != localSUID) { // check for serialVersionUID mismatch.
          /*
          val s = new StringBuilder("Overriding serialized class version mismatch (" + resultClassDescriptor.getName + ": ")
          s ++= "local serialVersionUID = "
          s ++= localSUID.toString
          s ++= " stream serialVersionUID = "
          s ++= streamSUID.toString
          val e = new InvalidClassException(s.toString)
          println("Potentially Fatal Deserialization Operation. " + e);
          */
          resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
        }
      }
      resultClassDescriptor
    }
  }
}

