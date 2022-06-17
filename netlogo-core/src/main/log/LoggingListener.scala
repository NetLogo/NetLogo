// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.nlogo.api.NetLogoAdapter
import org.nlogo.core.CompilerException

// It may not be obvious, but this class has no dependency on `LogManager`, because
// `LogManager` depends on it.  That's why it gets its own copy of the `events` to check
// instead of using the ones LogManager has.  -Jeremy B June 2022

class LoggingListener(private val events: LogEvents, private[log] var logger: FileLogger) extends NetLogoAdapter {

  override def codeTabCompiled(code: String, error: CompilerException) {
    if (events.compile) {
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
    if (events.commandCenter && !owner.startsWith("Slider")) {
      val eventInfo = if (error == null) {
        Map[String, Any](
          "owner"     -> owner
        , "code"      -> code
        , "agentType" -> agentType.toString
        , "success"   -> true
        )
      } else {
        Map[String, Any](
          "owner"      -> owner
        , "code"       -> code
        , "agentType"  -> agentType.toString
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
    if (events.button) {
      val eventInfo = Map[String, Any](
        "buttonName" -> buttonName
      , "wasStopped" -> false
      )
      logger.log(LogEvents.Types.button, eventInfo)
    }
  }

  override def buttonStopped(buttonName: String) {
    if (events.button) {
      val eventInfo = Map[String, Any](
        "buttonName" -> buttonName
      , "wasStopped" -> true
      )
      logger.log(LogEvents.Types.button, eventInfo)
    }
  }

  override def chooserChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (events.chooser) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> AnyRefFormat.forJson(newValue)
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.chooser, eventInfo)
    }
  }

  override def inputBoxChanged(globalName: String, newValue: AnyRef, valueChanged: Boolean) {
    if (events.inputBox) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> AnyRefFormat.forJson(newValue)
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.inputBox, eventInfo)
    }
  }

  override def modelOpened(name: String) {
    if (events.modelOpen) {
      val eventInfo = Map[String, Any](
        "name" -> name
      )
      logger.log(LogEvents.Types.modelOpen, eventInfo)
    }
  }

  override def sliderChanged(globalName: String, newValue: Double, min: Double, inc: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean) {
    if (events.slider) {
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
    if (events.switch) {
      val eventInfo = Map[String, Any](
        "globalName"   -> globalName
      , "newValue"     -> newValue
      , "valueChanged" -> valueChanged
      )
      logger.log(LogEvents.Types.switch, eventInfo)
    }
  }

  override def tickCounterChanged(ticks: Double) {
    if (events.tick) {
      val eventInfo = Map[String, Any](
        "ticks" -> ticks
      )
      logger.log(LogEvents.Types.tick, eventInfo)
    }
  }

}
