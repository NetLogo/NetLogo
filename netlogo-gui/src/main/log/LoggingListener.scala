// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.nlogo.api.NetLogoAdapter
import org.nlogo.core.CompilerException

class LoggingListener(private val events: Set[String], private val logger: FileLogger) extends NetLogoAdapter {

  def isLogging(event: String) = {
    events.contains(event)
  }

  override def codeTabCompiled(code: String, error: CompilerException) {
    if (isLogging(LogEventTypes.compile)) {
      val eventInfo = if (error == null) {
        Map(
          "code"    -> code
        , "success" -> "true"
        )
      } else {
        Map(
          "code"       -> code
        , "success"    -> "false"
        , "error"      -> error.getMessage
        , "errorStart" -> error.start.toString
        , "errorEnd"   -> error.end.toString
        )
      }
      logger.log(LogEventTypes.compile, eventInfo)
    }
  }

  override def commandEntered(owner: String, code: String, agentType: Char, error: CompilerException) {
    if (isLogging(LogEventTypes.commandCenter) && !owner.startsWith("Slider")) {
      val eventInfo = if (error == null) {
        Map(
          "owner"     -> owner
        , "code"      -> code
        , "agentType" -> agentType.toString
        , "success"   -> "true"
        )
      } else {
        Map(
          "owner"      -> owner
        , "code"       -> code
        , "agentType"  -> agentType.toString
        , "success"    -> "false"
        , "error"      -> error.getMessage
        , "errorStart" -> error.start.toString
        , "errorEnd"   -> error.end.toString
        )
      }
      logger.log(LogEventTypes.commandCenter, eventInfo)
    }
  }

  override def buttonPressed(buttonName: String) {
    if (isLogging(LogEventTypes.button)) {
      val eventInfo = Map(
        "buttonName" -> buttonName
      , "wasStopped" -> "false"
      )
      logger.log(LogEventTypes.button, eventInfo)
    }
  }

  override def buttonStopped(buttonName: String) {
    if (isLogging(LogEventTypes.button)) {
      val eventInfo = Map(
        "buttonName" -> buttonName
      , "wasStopped" -> "true"
      )
      logger.log(LogEventTypes.button, eventInfo)
    }
  }

  override def chooserChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (isLogging(LogEventTypes.chooser)) {
      val eventInfo = Map(
        "globalName"   -> globalName
      , "newValue"     -> Option(newValue).map(_.toString).getOrElse("")
      , "valueChanged" -> valueChanged.toString
      )
      logger.log(LogEventTypes.chooser, eventInfo)
    }
  }

  override def inputBoxChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (isLogging(LogEventTypes.inputBox)) {
      val eventInfo = Map(
        "globalName"   -> globalName
      , "newValue"     -> Option(newValue).map(_.toString).getOrElse("")
      , "valueChanged" -> valueChanged.toString
      )
      logger.log(LogEventTypes.inputBox, eventInfo)
    }
  }

  override def modelOpened(name: String) {
    if (isLogging(LogEventTypes.modelOpen)) {
      val eventInfo = Map(
        "name" -> name
      )
      logger.log(LogEventTypes.modelOpen, eventInfo)
    }
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

  override def switchChanged(globalName: String, newValue: Boolean, valueChanged: Boolean) {
    if (isLogging(LogEventTypes.switch)) {
      val eventInfo = Map(
        "globalName"   -> globalName
      , "newValue"     -> newValue.toString
      , "valueChanged" -> valueChanged.toString
      )
      logger.log(LogEventTypes.switch, eventInfo)
    }
  }

  override def tickCounterChanged(ticks: Double) {
    if (isLogging(LogEventTypes.tick)) {
      val eventInfo = Map(
        "ticks" -> ticks.toString
      )
      logger.log(LogEventTypes.tick, eventInfo)
    }
  }

}
