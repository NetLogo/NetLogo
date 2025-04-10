// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

object ModelCreator extends ModelCreator

trait ModelCreator {
  //
  // Code to create models
  //

  // a can have N widgets. plots, sliders, etc are widgets.
  trait Widget

  object Model {
    def apply(widgets: core.Widget*): Model =
      apply(code = "", widgets: _*)

    def apply(code: String,widgets: core.Widget*): Model =
      if (widgets.exists(_.isInstanceOf[core.View]))
        new Model(code = code, widgets = widgets.toList)
      else
        new Model(code = code, widgets = core.View() :: widgets.toList)
  }

  class Model(code: String, widgets: List[core.Widget]) extends core.Model(code = code, widgets = widgets)

  val counter = Iterator.from(0)
  def quoted(s:String) = '"' + s + '"'

  //
  // Code to create plots
  //

  def Pens(pens: core.Pen*): List[core.Pen] = pens.toList

  def Plot(name: String = "Plot" + counter.next,
    setupCode: String = "", updateCode: String = "", pens: List[core.Pen] = Nil, legendOn: Boolean = false): core.Plot =
      core.Plot(Some(name), 5, 5, 5, 5, false, Some("time"), Some("num of turtles"), 0.0, 10.0, 0.0, 10.0, true, true, legendOn, setupCode, updateCode, pens)

  def Pen(name:String = "Pen" + counter.next, setupCode:String = "", updateCode: String = ""): core.Pen =
    core.Pen(name, 1.0, 0, -16777216, true, setupCode, updateCode)

  //
  // Code to create sliders
  //

  trait Direction
  case object HORIZONTAL extends Direction
  case object VERTICAL extends Direction

  def Slider(name: String = "Slider" + counter.next,
                    // all of these should be something other than string. however,
                    // constraints can be foo + 30, so we need to allow for that.
                    // so we currently, clients use "10" even though they want just 10.
                    // this is something we should work out in the future. josh - 3/5/10
                    min: String = "0", max: String = "100", current: String = "50", inc: String = "1",
                    units:String = "NIL", direction: Direction = HORIZONTAL): core.Slider =
    core.Slider(Some(name), 5, 5, 5, 5, false, Some(name), min, max, current.toDouble, inc, if (units == "NIL") None else Some(units), if (direction == HORIZONTAL) core.Horizontal else core.Vertical)

  //
  // Code to create switches
  //

  def Switch(name:String = "Switch" + counter.next, on: Boolean = true): core.Switch =
    core.Switch(Some(name), 5, 5, 5, 5, false, Some(name), on)

  //
  // Code to create choosers
  //

  def Chooser(name:String = "Chooser" + counter.next, choices:List[AnyRef] = Nil, index:Int = 0): core.Chooser = {
    core.Chooser(Some(name), 5, 5, 5, 5, false, Some(name), choices.map(core.Chooseable(_)), index)
  }

  object InputBoxTypes {
    val Num         = core.NumericInput.NumberLabel
    val Col         = core.NumericInput.ColorLabel
    val Str         = core.StringInput.StringLabel
    val StrReporter = core.StringInput.ReporterLabel
    val StrCommand  = core.StringInput.CommandLabel
  }

  def InputBox(label: core.StringInput.StringKind,
    value: String = "",
    name: String,
    multiline: Boolean = false): core.InputBox = {
      core.InputBox(Some(name), 5, 5, 5, 5, false, core.StringInput(value, label, multiline))
  }

  def InputBox(label: core.NumericInput.NumericKind, value: Double, name: String): core.InputBox = {
      core.InputBox(Some(name), 5, 5, 5, 5, false, core.NumericInput(value, label))
  }
}
