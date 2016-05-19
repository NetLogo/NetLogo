// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.Femto

object WidgetRegistry {

  private val map = collection.mutable.Map[String,String]()

  map("Switch")        = "org.nlogo.widget.SwitchWidget"
  map("Dummy Switch")  = "org.nlogo.widget.DummySwitchWidget"

  // in the GUI, it's "Note"; in saved models, it's "TEXTBOX"
  map("TextBox")       = "org.nlogo.widget.NoteWidget"
  map("Note")          = "org.nlogo.widget.NoteWidget"
  map("Dummy TextBox") = "org.nlogo.widget.NoteWidget"
  map("Dummy Note")    = "org.nlogo.widget.NoteWidget"

  map("Dummy Slider")  = "org.nlogo.window.DummySliderWidget"

  map("Dummy Button")  = "org.nlogo.window.DummyButtonWidget"

  map("Dummy Monitor") = "org.nlogo.window.DummyMonitorWidget"

  map("Output")        = "org.nlogo.window.OutputWidget"
  map("Dummy Output")  = "org.nlogo.window.OutputWidget"

  def apply(name: String): Widget =
    map.get(name)
       .map(className => Femto.get[Widget](className))
       .orNull

}
