// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

object LogEvents {

  object Types {
    val button        = "button"
    val chooser       = "chooser"
    val comment       = "comment"
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
  , "comment"
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
  , "comment"
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
    "agents"        -> Set("link", "turtle")
  , "all"           -> LogEvents.allEvents
  , "buttons"       -> Set("button")
  , "choosers"      -> Set("chooser")
  , "comments"      -> Set("comment")
  , "greens"        -> Set("chooser", "input-box", "slider", "switch")
  , "input-boxes"   -> Set("input-box")
  , "sliders"       -> Set("slider")
  , "switches"      -> Set("switch")
  , "ticks"         -> Set("tick")
  , "user-comments" -> Set("comment")
  , "widgets"       -> Set("widget-edit")
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

class LogEvents(val set: Set[String] = LogEvents.defaultEvents) {
  def isLogging(event: String): Boolean = {
    set.contains(event)
  }

  // Yeah it looks weird, but this is our "cache" of values so we don't have to check if
  // strings are in Sets every time we want to see if we're logging something.
  val button        = isLogging(LogEvents.Types.button)
  val chooser       = isLogging(LogEvents.Types.chooser)
  val comment       = isLogging(LogEvents.Types.comment)
  val compile       = isLogging(LogEvents.Types.compile)
  val commandCenter = isLogging(LogEvents.Types.commandCenter)
  val global        = isLogging(LogEvents.Types.global)
  val inputBox      = isLogging(LogEvents.Types.inputBox)
  val link          = isLogging(LogEvents.Types.link)
  val modelOpen     = isLogging(LogEvents.Types.modelOpen)
  val slider        = isLogging(LogEvents.Types.slider)
  val speedSlider   = isLogging(LogEvents.Types.speedSlider)
  val start         = isLogging(LogEvents.Types.start)
  val switch        = isLogging(LogEvents.Types.switch)
  val stop          = isLogging(LogEvents.Types.stop)
  val tick          = isLogging(LogEvents.Types.tick)
  val turtle        = isLogging(LogEvents.Types.turtle)
  val widgetEdit    = isLogging(LogEvents.Types.widgetEdit)

}
