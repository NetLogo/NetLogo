// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.nlogo.api.NetLogoAdapter
import org.nlogo.core.CompilerException

class LoggingListener(private val events: Set[String], private[log] var logger: FileLogger) extends NetLogoAdapter {

  def isLogging(event: String) = {
    events.contains(event)
  }

  override def codeTabCompiled(code: String, error: CompilerException) {
    if (isLogging(LogEvents.Types.compile)) {
      val eventInfo = if (error == null) {
        Map[String, Any](
          "code"    -> code
        , "success" -> true
        )
      } else {
        Map[String, Any](
          "code"       -> code
        , "success"    -> false
        , "error"      -> error.getMessage
        , "errorStart" -> error.start
        , "errorEnd"   -> error.end
        )
      }
      logger.log(LogEvents.Types.compile, eventInfo)
    }
  }

  override def commandEntered(owner: String, code: String, agentType: Char, error: CompilerException) {
    if (isLogging(LogEvents.Types.commandCenter) && !owner.startsWith("Slider")) {
      val eventInfo = if (error == null) {
        Map[String, Any](
          "owner"     -> owner
        , "code"      -> code
        , "agentType" -> agentType
        , "success"   -> true
        )
      } else {
        Map[String, Any](
          "owner"      -> owner
        , "code"       -> code
        , "agentType"  -> agentType
        , "success"    -> false
        , "error"      -> error.getMessage
        , "errorStart" -> error.start
        , "errorEnd"   -> error.end
        )
      }
      logger.log(LogEvents.Types.commandCenter, eventInfo)
    }
  }

  override def buttonPressed(buttonName: String) {
    if (isLogging(LogEvents.Types.button)) {
      val eventInfo = Map[String, Any](
        "buttonName" -> buttonName
      , "wasStopped" -> false
      )
      logger.log(LogEvents.Types.button, eventInfo)
    }
  }

  override def buttonStopped(buttonName: String) {
    if (isLogging(LogEvents.Types.button)) {
      val eventInfo = Map[String, Any](
        "buttonName" -> buttonName
      , "wasStopped" -> true
      )
      logger.log(LogEvents.Types.button, eventInfo)
    }
  }

  override def chooserChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (isLogging(LogEvents.Types.chooser)) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> AnyRefFormat.forJson(newValue)
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.chooser, eventInfo)
    }
  }

  override def inputBoxChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (isLogging(LogEvents.Types.inputBox)) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> AnyRefFormat.forJson(newValue)
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.inputBox, eventInfo)
    }
  }

  override def modelOpened(name: String) {
    if (isLogging(LogEvents.Types.modelOpen)) {
      val eventInfo = Map[String, Any](
        "name" -> name
      )
      logger.log(LogEvents.Types.modelOpen, eventInfo)
    }
  }

  override def sliderChanged(globalName: String, newValue: Double, min: Double, inc: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean) {
    if (isLogging(LogEvents.Types.slider)) {
      val eventInfo = Map[String, Any](
        "globalName"     -> globalName
      , "newValue"       -> newValue
      , "min"            -> min
      , "max"            -> max
      , "inc"            -> inc
      , "valueChanged"   -> valueChanged
      , "buttonReleased" -> buttonReleased
      )
      logger.log(LogEvents.Types.slider, eventInfo)
    }
  }

  override def switchChanged(globalName: String, newValue: Boolean, valueChanged: Boolean) {
    if (isLogging(LogEvents.Types.switch)) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> newValue
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.switch, eventInfo)
    }
  }

  override def tickCounterChanged(ticks: Double) {
    if (isLogging(LogEvents.Types.tick)) {
      val eventInfo = Map[String, Any](
        "ticks" -> ticks
      )
      logger.log(LogEvents.Types.tick, eventInfo)
    }
  }

}
