// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.time.format.DateTimeFormatter

object DateTimeFormats {
  private[log] val file     = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS")
  private[log] val logEntry = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
}
