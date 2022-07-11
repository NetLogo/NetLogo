// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.{ File, FilenameFilter }

trait FileLogger {
  def close() {}
  def log(event: String, eventInfo: Map[String, Any]) {}
  val fileNameFilter: FilenameFilter
}

private[log] class NoOpLogger extends FileLogger {
  val fileNameFilter = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      false
    }
  }
}
