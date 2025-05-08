// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ConstraintSpecification._

trait Widget {
  def x:   Int
  def y:    Int
  def width:  Int
  def height: Int
  def oldSize: Boolean

  /** convertSource applies the given conversion to all code found in the widget. */
  def convertSource(conversion: String => String): Widget = this
}

trait NamedWidget {
  def varName: String
}


trait DeclaresGlobal extends NamedWidget

trait DeclaresConstraint extends NamedWidget {
  def constraint: ConstraintSpecification
}

trait DeclaresGlobalCommand extends NamedWidget {
  def default: Any
  def asNetLogoString(x: Any): String = x match {
    case s: String => s""""${StringEscaper.escapeString(s)}""""
    case b: Boolean => if(b) "true" else "false"
    case l: List[Any] => l.map(asNetLogoString).mkString("[", " ", "]")
    case ll: LogoList => ll.toList.map(asNetLogoString).mkString("[" , " ", "]")
    case a => a.toString
  }
  def command: String = "set " + varName + " " + asNetLogoString(default)
}

case class Button(source: Option[String],
  x: Int, y: Int,
  width: Int, height: Int,
  oldSize: Boolean = false,
  display: Option[String] = None,
  forever: Boolean = false,
  buttonKind: AgentKind = AgentKind.Observer,
  actionKey: Option[Char] = None,
  disableUntilTicksStart: Boolean = false) extends Widget {
    override def convertSource(conversion: String => String): Button =
      this.copy(source = source.map(conversion))
  }

case class TextBox(display: Option[String],
  x: Int = 0, y: Int = 0,
  width: Int = 5, height: Int = 5,
  fontSize: Int, markdown: Boolean = false,
  textColorLight: Option[Int] = None, textColorDark: Option[Int] = None,
  backgroundLight: Option[Int] = None, backgroundDark: Option[Int] = None) extends Widget {

  def oldSize: Boolean = false
}

case class Switch(variable: Option[String],
  x:  Int = 0, y:    Int = 0,
  width: Int = 0, height: Int = 0,
  oldSize: Boolean = false,
  display: Option[String] = None,
  on: Boolean = false) extends Widget
  with DeclaresGlobal
  with DeclaresGlobalCommand
  with DeclaresConstraint {
  override def varName = variable.getOrElse("")
  override def default = on
  override def constraint = BooleanConstraintSpecification(default.asInstanceOf[Boolean])
}

sealed trait Direction
case object Horizontal extends Direction
case object Vertical extends Direction
case class Slider(variable: Option[String],
  x:  Int = 0, y:    Int = 0,
  width: Int = 0, height: Int = 0,
  oldSize: Boolean = false,
  display:  Option[String]  = None,
  min:       String         = "0",
  max:       String         = "100",
  default:   Double         = 1,
  step:      String         = "1",
  units:     Option[String] = None,
  direction: Direction      = Horizontal)
  extends Widget
  with DeclaresGlobal
  with DeclaresGlobalCommand
  with DeclaresConstraint {
  override def varName = variable.getOrElse("")
  override val constraint =
    (for {
      minBound <- NumberParser.parse(min)
      maxBound <- NumberParser.parse(max)
      increment <- NumberParser.parse(step)
    } yield BoundedNumericConstraintSpecification(minBound, default, maxBound, increment))
      .getOrElse(NumericConstraintSpecification(default))

  override def convertSource(conversion: String => String): Slider =
    this.copy(min = conversion(min), max = conversion(max), step = conversion(step))
}

case class Monitor(
  source: Option[String],
  x:  Int, y:    Int,
  width: Int, height: Int,
  oldSize: Boolean,
  display: Option[String],
  precision: Int,
  fontSize:  Int = 11,
  units: Option[String] = None) extends Widget {
    override def convertSource(conversion: String => String): Monitor =
      this.copy(source = source.map(conversion))
  }

case class Output(
  x:  Int, y:    Int,
  width: Int, height: Int,
  fontSize: Int) extends Widget {

  def oldSize: Boolean = false
}

trait ViewLike extends Widget {
  def oldSize: Boolean = false
}

case class View(x: Int = 0, y: Int = 0, width: Int = 5, height: Int = 5,
  dimensions:       WorldDimensions = View.defaultDimensions,
  fontSize:         Int             = 13,
  updateMode:       UpdateMode      = UpdateMode.TickBased,
  showTickCounter:  Boolean         = true,
  tickCounterLabel: Option[String]  = Some("ticks"),
  frameRate:        Double          = 30)
extends ViewLike {
  val minPxcor = dimensions.minPxcor
  val maxPxcor = dimensions.maxPxcor
  val wrappingAllowedInX = dimensions.wrappingAllowedInX

  val minPycor = dimensions.minPycor
  val maxPycor = dimensions.maxPycor
  val wrappingAllowedInY = dimensions.wrappingAllowedInY

  val patchSize = dimensions.patchSize
}

// enables smoother folding in XML reader
case object DummyView extends ViewLike {
  def x: Int = 0
  def y: Int = 0
  def width: Int = 0
  def height: Int = 0
}

object View {
  def defaultDimensions: WorldDimensions =
    new WorldDimensions(0, 0, 0, 0, 12.0, true, true)

  def square(dim: Int) = View(dimensions = new WorldDimensions(minPxcor = -dim, maxPxcor = dim, minPycor = -dim, maxPycor = dim))
}
