// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.util.Femto

object WidgetRegistry {

  private val map = collection.mutable.Map[String,String]()

  map("SWITCH")        = "org.nlogo.widget.SwitchWidget"

  // in the GUI, it's "Note"; in saved models, it's "TEXTBOX"
  map("TEXTBOX")       = "org.nlogo.widget.NoteWidget"
  map("NOTE")          = "org.nlogo.widget.NoteWidget"

  def apply(name: String): Widget =
    map.get(name)
       .map(className => Femto.get(classOf[Widget], className, Array()))
       .orNull

}
