// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.time.format.DateTimeFormatter
import java.lang.{
  Boolean => BoxedBoolean
, Double  => BoxedDouble
, Integer => BoxedInt
, Long    => BoxedLong
}

object DateTimeFormats {
  private[log] val file     = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS")
  private[log] val logEntry = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
}

object AnyRefFormat {
  def forJson(value: AnyRef) = {
    value match {
      case i: BoxedInt     => i
      case l: BoxedLong    => l
      case d: BoxedDouble  => d
      case b: BoxedBoolean => b
      case a: Array[_]     => a
      case s: String       => s
      case null            => null
      case v               => v.toString
    }
  }
}
