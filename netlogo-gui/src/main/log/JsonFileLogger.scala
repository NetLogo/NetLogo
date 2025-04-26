// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log


import java.io.{ File, FilenameFilter, FileWriter, PrintWriter }
import java.lang.{
  Boolean => BoxedBoolean
, Double  => BoxedDouble
, Integer => BoxedInt
, Long    => BoxedLong
}
import java.nio.file.Path
import java.time.LocalDateTime

import scala.jdk.CollectionConverters.{ MapHasAsJava, SeqHasAsJava }

import org.json.simple.JSONValue

import org.nlogo.api.Exceptions.warning
import org.nlogo.core.LogoList

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
    logFile.getParentFile.mkdirs
    new PrintWriter(new FileWriter(logFile))
  }

  // The content of the events log is a single array.  But the simple JSON writer we're
  // using doesn't understand "buffered" logging or whatever, only whole objects/strings.
  // So we handle writing the open, close, and commas for the array of log objects.
  // -Jeremy B June 2021
  writer.write("[\n")
  private var first = true

  override def log(event: String, eventInfo: Map[String, Any]): Unit = {
    warning(classOf[Exception]) {
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
        val formattedEventInfo = eventInfo.map({ case (key, value) =>
          val formattedValue = value match {
            case v: AnyRef => formatAnyRef(v)
            case v         => v
          }
          (key, formattedValue)
        })
        map + ("eventInfo" -> formattedEventInfo.asJava)
      } else {
        map
      }
      JSONValue.writeJSONString(finalMap.asJava, writer)
      writer.write("\n")
    }
  }

  // The values NetLogo provides can be boxed, and we don't want to turn them into strings;
  // we want `10` not `"10"` in the output.  Also, arrays are handled directly by the JSON
  // library, so don't change them.  Everything else is stringified.  -Jeremy B June 2022
  private def formatAnyRef(value: AnyRef): AnyRef = {
    value match {
      case i: BoxedInt     => i
      case l: BoxedLong    => l
      case d: BoxedDouble  => d
      case b: BoxedBoolean => b
      case a: Array[_]     => a
      case s: String       => s
      case l: LogoList     => l.map(formatAnyRef(_)).asJava
      case null            => null
      case v               => v.toString
    }
  }

  override def close(): Unit = {
    writer.write("]")
    writer.flush()
    writer.close()
  }

}
