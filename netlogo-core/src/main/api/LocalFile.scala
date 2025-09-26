// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ BufferedReader, File, FileInputStream, FileOutputStream, IOException, InputStreamReader,
                 OutputStreamWriter, PrintWriter }

import org.nlogo.core.{ File => CoreFile, FileMode }

class LocalFile(filepath: String) extends CoreFile {

  private var w: PrintWriter = null
  override def getPrintWriter = w

  @throws(classOf[IOException])
  override def getInputStream =
    new FileInputStream(getPath)

  @throws(classOf[IOException])
  override def open(mode: FileMode): Unit = {
    if (w != null || reader != null)
      throw new IOException(
        "Attempted to open an already open file")
    mode match {
      case FileMode.Read =>
        pos = 0
        eof = false
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8"))
        this.mode = mode
      case FileMode.Write =>
        w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filepath)), "UTF-8"))
        this.mode = mode
      case FileMode.Append =>
        w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filepath), true), "UTF-8"))
        this.mode = mode
      case FileMode.None =>
        throw new IllegalStateException("file is not open")
    }
  }

  @throws(classOf[IOException])
  override def print(str: String): Unit = {
    if (w == null)
      throw new IOException("Attempted to print to an unopened File")
    w.print(str)
  }

  @throws(classOf[IOException])
  override def println(line: String): Unit = {
    if (w == null)
      throw new IOException("Attempted to println to an unopened File")
    w.println(line)
  }

  @throws(classOf[IOException])
  override def println(): Unit = {
    if (w == null)
      throw new IOException("Attempted to println to an unopened File")
    w.println()
  }

  override def flush(): Unit = {
    if (w != null)
      w.flush()
  }

  @throws(classOf[IOException])
  override def close(ok: Boolean): Unit = {
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
    new File(filepath).getAbsolutePath

  override def getPath =
    new File(filepath).getPath

}
