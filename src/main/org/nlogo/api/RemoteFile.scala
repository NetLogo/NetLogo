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

class RemoteFile(_filepath: String, suffix: String) extends File {

  def this(_filepath: String) = this(_filepath, null)

  val filepath =
    if (_filepath == null || suffix == null)
      _filepath
    else {
      val tmpf = _filepath.toLowerCase
      val tmps = suffix.toLowerCase
      if (tmpf.endsWith(tmps))
        _filepath.substring(0, tmpf.lastIndexOf(tmps))
      else
        _filepath
    }

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
    val fullpath =
      filepath + Option(suffix).getOrElse("")
    mode match {
      case FileMode.READ =>
        pos = 0
        eof = false
        reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(
                new java.io.BufferedInputStream(
                    new java.net.URL(org.nlogo.util.Utils.escapeSpacesInURL(fullpath)).openStream())))
        this.mode = mode
      case FileMode.WRITE | FileMode.APPEND =>
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
      case FileMode.READ =>
        reader.close()
        reader = null
      case FileMode.NONE =>
    }
    mode = FileMode.NONE
  }

  override def getAbsolutePath =
    new java.io.File(filepath + Option(suffix).getOrElse("")).getAbsolutePath

  override def getPath =
    new java.io.File(filepath + Option(suffix).getOrElse("")).getPath

}
