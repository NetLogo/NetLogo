// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

object LogEvents {

  object Types {
    val button        = "button"
    val chooser       = "chooser"
    val compile       = "compile"
    val commandCenter = "command-center"
    val global        = "global"
    val inputBox      = "input-box"
    val link          = "link"
    val modelOpen     = "model-open"
    val slider        = "slider"
    val speedSlider   = "speed-slider"
    val start         = "start"
    val switch        = "switch"
    val stop          = "stop"
    val tick          = "tick"
    val turtle        = "turtle"
    val widgetEdit    = "widget-edit"
  }

  val allEvents = Set(
    "button"
  , "chooser"
  , "compile"
  , "command-center"
  , "global"
  , "input-box"
  , "link"
  , "model-open"
  , "slider"
  , "speed-slider"
  , "start"
  , "switch"
  , "stop"
  , "turtle"
  , "widget-edit"
  , "tick"
  )

  val defaultEvents = Set(
    "button"
  , "chooser"
  , "compile"
  , "command-center"
  , "input-box"
  , "model-open"
  , "slider"
  , "speed-slider"
  , "start"
  , "switch"
  , "stop"
  , "widget-edit"
  , "tick"
  )

  val eventShortcuts = Map(
    "all"    -> LogEvents.allEvents
  , "greens" -> Set("chooser", "input-box", "slider", "switch")
  )

  def translateEvent(event: String): Set[String] = {
    if (LogEvents.allEvents.contains(event)) {
      Set(event)
    } else {
      if (LogEvents.eventShortcuts.contains(event)) {
        LogEvents.eventShortcuts.getOrElse(event, Set())
      } else {
        Set()
      }
    }
  }

  def parseEvents(eventsString: String): Set[String] = {
    if (eventsString.trim().equals("")) {
      LogEvents.defaultEvents
    } else {
      val dirtyEvents    = eventsString.split(",").toList.map(_.trim())
      val expandedEvents = dirtyEvents.map(LogEvents.translateEvent(_))
      expandedEvents.flatten.toSet
    }
  }

}
