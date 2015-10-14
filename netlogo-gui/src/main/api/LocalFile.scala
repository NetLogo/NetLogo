// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

class LocalFile(filepath: String) extends File {

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
    mode match {
      case FileMode.Read =>
        pos = 0
        eof = false
        reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(
                new java.io.BufferedInputStream(
                    new java.io.FileInputStream(new java.io.File(filepath)))))
        this.mode = mode
      case FileMode.Write =>
        w = new java.io.PrintWriter(new java.io.FileWriter(filepath))
        this.mode = mode
      case FileMode.Append =>
        w = new java.io.PrintWriter(new java.io.FileWriter(filepath, true))
        this.mode = mode
      case FileMode.None =>
        sys.error("file is not open")
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
      case FileMode.Write | FileMode.Append =>
        w.close()
        w = null
      case FileMode.Read =>
        reader.close()
        reader = null
      case FileMode.None =>
    }
    mode = FileMode.None
  }

  override def getAbsolutePath =
    new java.io.File(filepath).getAbsolutePath

  override def getPath =
    new java.io.File(filepath).getPath

}
