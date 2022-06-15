// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.nlogo.api.NetLogoAdapter

class LoggingListener(private val events: Set[String], private val logger: FileLogger) extends NetLogoAdapter {

  def isLogging(event: String) = {
    events.contains(event)
  }

  override def sliderChanged(globalName: String, newValue: Double, min: Double, inc: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean) {
    if (isLogging(LogEventTypes.slider)) {
      val eventInfo = Map(
        "globalName"     -> globalName
      , "newValue"       -> newValue.toString
      , "min"            -> min.toString
      , "max"            -> max.toString
      , "inc"            -> inc.toString
      , "valueChanged"   -> valueChanged.toString
      , "buttonReleased" -> buttonReleased.toString
      )
      logger.log(LogEventTypes.slider, eventInfo)
    }
  }

}
