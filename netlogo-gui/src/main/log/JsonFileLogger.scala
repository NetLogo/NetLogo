// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.{ File, FilenameFilter, FileWriter, PrintWriter }
import java.nio.file.Path
import java.time.LocalDateTime

import collection.JavaConverters._

import org.json.simple.JSONValue

// This JSON logger is kept in `netlogo` outside of `core` to avoid `headless` needing the
// JSON dependency.  -Jeremy B June 2022

class JsonFileLogger(private val logDirectoryPath: Path) extends FileLogger {

  val fileNameFilter = new FilenameFilter {
    override def accept(dir: File, name: String) = {
      name.startsWith("netlogo_log_") && name.endsWith(".json")
    }
  }

  private val writer = {
    val now         = LocalDateTime.now
    val logFileName = s"netlogo_log_${now.format(DateTimeFormats.file)}.json"
    val logFilePath = logDirectoryPath.resolve(logFileName)
    val logFile     = logFilePath.toFile()
    new PrintWriter(new FileWriter(logFile))
  }
  writer.write("[\n")

  private var first = true

  override def log(event: String, eventInfo: Map[String, Any]) {
    if (first) {
      first = false
      writer.write("  ")
    } else {
      writer.write(", ")
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
    JSONValue.writeJSONString(finalMap.asJava, writer)
    writer.write("\n")
  }

  override def close() {
    writer.write("]")
    writer.flush()
    writer.close()
  }

}
