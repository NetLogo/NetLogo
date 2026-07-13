// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ BufferedInputStream, BufferedReader, InputStreamReader, IOException }
import java.net.URI

import org.nlogo.core.{ File, FileMode }
import org.nlogo.util.Utils

object RemoteFile {
  def exists(path: String): Boolean = {
    val url = Utils.escapeSpacesInURL(path)
    try {
      URI.create(url).toURL.openStream()
      true
    } catch {
      case ex: IOException => false
    }
  }
}

class RemoteFile(filepath: String) extends File {

  override def getPrintWriter = null

  @throws(classOf[IOException])
  override def getInputStream =
    new BufferedInputStream(URI.create(Utils.escapeSpacesInURL(getPath)).toURL.openStream())

  @throws(classOf[IOException])
  override def open(mode: FileMode): Unit = {
    if (reader != null)
      throw new IOException(
        "Attempted to open an already open file")
    mode match {
      case FileMode.Read =>
        pos = 0
        eof = false
        reader = new BufferedReader(
            new InputStreamReader(
                new BufferedInputStream(URI.create(Utils.escapeSpacesInURL(filepath)).toURL.openStream()),
              "UTF-8"))
        this.mode = mode
      case FileMode.Write | FileMode.Append | FileMode.None =>
        unsupported
    }
  }

  private def unsupported =
    throw new IOException("Cannot write to remote files.")

  @throws(classOf[IOException])
  override def print(str: String) = unsupported
  @throws(classOf[IOException])
  override def println(line: String) = unsupported
  @throws(classOf[IOException])
  override def println() = unsupported

  override def flush(): Unit = { }

  @throws(classOf[IOException])
  override def close(ok: Boolean): Unit = {
    mode match {
      case FileMode.Read =>
        reader.close()
        reader = null
      case FileMode.None =>
      case FileMode.Write | FileMode.Append =>
        unsupported
    }
    mode = FileMode.None
  }

  override def getAbsolutePath = filepath
  override def getPath = filepath

}
