// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object RemoteFile {
  def exists(path: String): Boolean = {
    val url = org.nlogo.util.Utils.escapeSpacesInURL(path)
    try { new java.net.URL(url).openStream(); true }
    catch {
      case ex: java.io.IOException =>
        false
    }
  }
}

class RemoteFile(filepath: String) extends File {

  override def getPrintWriter = null

  @throws(classOf[java.io.IOException])
  override def getInputStream =
    new java.io.BufferedInputStream(
      new java.net.URL(
        org.nlogo.util.Utils.escapeSpacesInURL(getPath))
      .openStream())

  @throws(classOf[java.io.IOException])
  override def open(mode: FileMode) {
    if (reader != null)
      throw new java.io.IOException(
        "Attempted to open an already open file")
    mode match {
      case FileMode.Read =>
        pos = 0
        eof = false
        reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(
                new java.io.BufferedInputStream(
                    new java.net.URL(org.nlogo.util.Utils.escapeSpacesInURL(filepath)).openStream()),
              "UTF-8"))
        this.mode = mode
      case FileMode.Write | FileMode.Append | FileMode.None =>
        unsupported
    }
  }

  private def unsupported =
    throw new java.io.IOException("Cannot write to remote files.")

  @throws(classOf[java.io.IOException])
  override def print(str: String) = unsupported
  @throws(classOf[java.io.IOException])
  override def println(line: String) = unsupported
  @throws(classOf[java.io.IOException])
  override def println() = unsupported

  override def flush() { }

  @throws(classOf[java.io.IOException])
  override def close(ok: Boolean) {
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
