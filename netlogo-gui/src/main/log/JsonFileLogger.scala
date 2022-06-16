// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.{ File, FileWriter, PrintWriter }
import java.time.LocalDateTime

import collection.JavaConverters._

import org.json.simple.JSONValue

class JsonFileLogger(private val logFileDirectory: File) extends FileLogger {

  private val _writer = {
    val now         = LocalDateTime.now
    val logFileName = s"netlogo_log_${now.format(DateTimeFormats.file)}.json"
    val logFilePath = logFileDirectory.toPath().resolve(logFileName)
    val logFile     = logFilePath.toFile()
    new PrintWriter(new FileWriter(logFile))
  }
  this._writer.write("[\n")

  private var _first = true

  override def log(event: String, eventInfo: Map[String, Any]) {
    if (this._first) {
      this._first = false
      this._writer.write("  ")
    } else {
      this._writer.write(", ")
    }

    val timeStamp = LocalDateTime.now

    val map = Map[String, Any](
      "event"     -> event
    , "timeStamp" -> timeStamp.format(DateTimeFormats.logEntry)
    )
    val finalMap = if (!eventInfo.isEmpty) {
      map + ("eventInfo" -> eventInfo.asJava)
    } else {
      map
    }
    JSONValue.writeJSONString(finalMap.asJava, this._writer)
    this._writer.write("\n")
  }

  override def close() {
    this._writer.write("]")
    this._writer.flush()
    this._writer.close()
  }

}
