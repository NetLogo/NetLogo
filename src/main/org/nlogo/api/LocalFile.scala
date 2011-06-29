package org.nlogo.api

class LocalFile(_filepath: String, suffix: String = null) extends File {

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

  private var w: java.io.PrintWriter = null
  override def getPrintWriter = w

  @throws(classOf[java.io.IOException])
  override def getInputStream =
    new java.io.FileInputStream(getPath)

  @throws(classOf[java.io.IOException])
  override def open(mode: FileMode) = {
    if (w != null || reader != null)
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
                    new java.io.FileInputStream(new java.io.File(fullpath)))))
        this.mode = mode
      case FileMode.WRITE =>
        w = new java.io.PrintWriter(new java.io.FileWriter(fullpath))
        this.mode = mode
      case FileMode.APPEND =>
        w = new java.io.PrintWriter(new java.io.FileWriter(fullpath, true))
        this.mode = mode
    }
  }

  @throws(classOf[java.io.IOException])
  override def print(str: String) {
    if (w == null)
      throw new java.io.IOException("Attempted to print to an unopened File")
    w.print(str)
  }

  @throws(classOf[java.io.IOException])
  override def println(line: String) {
    if (w == null)
      throw new java.io.IOException("Attempted to println to an unopened File")
    w.println(line)
  }

  @throws(classOf[java.io.IOException])
  override def println() {
    if (w == null)
      throw new java.io.IOException("Attempted to println to an unopened File")
    w.println()
  }

  override def flush() {
    if (w != null)
      w.flush()
  }

  @throws(classOf[java.io.IOException])
  override def close(ok: Boolean) {
    mode match {
      case FileMode.WRITE | FileMode.APPEND =>
        w.close()
        w = null
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
