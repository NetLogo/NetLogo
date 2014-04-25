// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait Widget
sealed trait DeclaresGlobal {
  def varName: String
}

sealed trait DeclaresConstraint {
  def varName: String
  def constraint: List[String]
}
sealed trait DeclaresGlobalCommand {
  def varName: String
  def default: Any
  def asNetLogoString(x: Any): String = x match {
    case s: String => s""""$s""""
    case b: Boolean => if(b) "true" else "false"
    case l: List[Any] => "[" + l.map(asNetLogoString).mkString(" ") + "]"
    case o: AnyRef => o.toString
  }
  def command: String = "set " + varName + " " + asNetLogoString(default)
}
case class Button(display: String, left: Int, top: Int, right: Int, bottom: Int,
             source: String, forever: Boolean, buttonType: String = "OBSERVER", actionKey: String = "NIL") extends Widget
case class Plot(display: String, left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
             xAxis: String = "", yAxis: String = "", xmin: Double = 0, xmax: Double = 0, ymin: Double = 0, ymax: Double = 0,
             autoPlotOn: Boolean = true, legendOn: Boolean = false,
             setupCode: String = "", updateCode: String = "", pens: List[Pen] = Nil) extends Widget
case class Pen(display: String, interval: Double = 1, mode: Int = 0, color: Int = 0, inLegend: Boolean = false,
             setupCode: String = "", updateCode: String = "") extends Widget
case class TextBox(display: String, left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
             fontSize: Int, color: Double, transparent: Boolean) extends Widget
case class Switch(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, on: Boolean = false)
              extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = on
  override def constraint = List("SWITCH", asNetLogoString(default))
}
case class Chooser(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, choices: List[AnyRef] = Nil, currentChoice: Int = 0)
           extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = choices(currentChoice)
  override def constraint = List("CHOOSER", asNetLogoString(choices), currentChoice.toString)
}
sealed trait Direction
case object Horizontal extends Direction
case object Vertical extends Direction
case class Slider(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, min: String = "1", max: String = "10", default: Double = 1, step: String = "1",
             units: String = "", direction: Direction = Horizontal)
             extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def constraint = List("SLIDER", min, max, step, default.toString)
}
case class Monitor(display: String, left: Int, top: Int, right: Int, bottom: Int,
             source: String, precision: Int, fontSize: Int) extends Widget
case class Output(left: Int, top: Int, right: Int, bottom: Int, fontSize: Int) extends Widget

abstract class InputBoxType[T](val name:String)
case object Num extends InputBoxType[Double]("Number")
case object Str extends InputBoxType[String]("String")
case object StrReporter extends InputBoxType[String]("String (reporter)")
case object StrCommand extends InputBoxType[String]("String (command)")
case object Col extends InputBoxType[Int]("Color")
case class InputBox[T](left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, varName: String,
             value: T, multiline: Boolean = false, boxtype: InputBoxType[T])
           extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = value
  override def constraint = List("INPUTBOX", default.toString, boxtype.name)
}
case class View(left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
  patchSize: Double = 12, fontSize: Int = 9, wrappingAllowedInX: Boolean = true, wrappingAllowedInY: Boolean = true,
  minPxcor: Int = 0, maxPxcor: Int = 0, minPycor: Int = 0, maxPycor: Int = 0,
  updateMode: UpdateMode = UpdateMode.TickBased, showTickCounter: Boolean = true, tickCounterLabel: String = "ticks",
  frameRate: Double = 25) extends Widget {

  def dimensions: WorldDimensions = new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor,
                                                        patchSize, wrappingAllowedInX, wrappingAllowedInY)
}
object View {
  def square(dim: Int) = View(minPxcor = -dim, maxPxcor = dim, minPycor = -dim, maxPycor = dim)
}


