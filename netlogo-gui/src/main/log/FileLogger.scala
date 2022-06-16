// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

trait FileLogger {
  def close() {}
  def log(event: String, eventInfo: Map[String, Any]) {}
  def zipLogFiles() {}
  def deleteLogFiles() {}
}

private[log] class NoOpLogger extends FileLogger {}
