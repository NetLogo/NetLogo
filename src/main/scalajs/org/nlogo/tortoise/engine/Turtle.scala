package org.nlogo.tortoise.engine

import scala.js.{ Any => AnyJS }

import
  EngineVariableNames.{ CommonE, TurtleE },
    CommonE._,
    TurtleE._

class Turtle private (world: World, variables: VarMap) extends Vassal with CanTalkToPatches {

  import Turtle._

  override val updateType = Overlord.UpdateType.TurtleType
  override val companion  = Turtle

  registerUpdate(variables.toSeq: _*)

  // `penmode` is a string?  Really?!  --JAB (7/26/13)
  def this(id:         ID,                     world:   World,              color:   NLColor,             heading: Int,
           xcor:       XCor,                   ycor:    YCor,               shape:   String  = "default", label:   String = "",
           labelcolor: NLColor = NLColor(9.9), breed:   String = "TURTLES", hidden:  Boolean = false,
           size:       Double  = 1.0,          pensize: Double = 1.0,       penmode: String  = "up") {
    this(world, {
      import Turtle._
      val builtins = VarMap(
        IDKeyE -> id,       ColorKeyE -> color,     HeadingKeyE -> heading,       XCorKeyE -> xcor,   YCorKeyE -> ycor,
        ShapeKeyE -> shape, LabelKeyE -> label,     LabelColorKeyE -> labelcolor, BreedKeyE -> breed, HiddenKeyE -> hidden,
        SizeKeyE -> size,   PenSizeKeyE -> pensize, PenModeKeyE -> penmode
      )
      builtins ++ (1 to varCount map (x => (x + builtins.size).toString -> (0: JSW)))
    }.asInstanceOf[VarMap])
  }

  def fd(amount: Double): Unit = {
    variables(XCorKeyE) = XCor(world.topology.wrap(xcor.value + amount * Trig.sin(heading), world.minPxcor - 0.5, world.maxPxcor + 0.5))
    variables(YCorKeyE) = YCor(world.topology.wrap(ycor.value + amount * Trig.cos(heading), world.minPycor - 0.5, world.maxPycor + 0.5))
    registerUpdate(XCorKeyE -> xcor, YCorKeyE -> ycor)
  }

  def right(amount: Int): Unit = {
    variables(HeadingKeyE) = normalizedHeading(heading + amount)
    registerUpdate(HeadingKeyE -> heading)
  }

  def setxy(x: Double, y: Double): Unit = {
    variables(XCorKeyE) = XCor(x)
    variables(YCorKeyE) = YCor(y)
    registerUpdate(XCorKeyE -> xcor, YCorKeyE -> ycor)
  }

  def die(): Unit = {
    if (id != DeadID) {
      world.removeTurtle(id)
      Overlord.registerDeath(id)
      variables(IDKeyE) = DeadID
    }
  }

  def getTurtleVariable(n: Int): AnyJS =
    variables.toSeq(n)._2.toJS

  def setTurtleVariable(n: Int, value: JSW): Unit = {
    val k      = variables.toSeq(n)._1
    val v: JSW = k match {
      case IDKeyE   => ID  (value.value.asInstanceOf[Long])
      case XCorKeyE => XCor(value.value.asInstanceOf[Double])
      case YCorKeyE => YCor(value.value.asInstanceOf[Double])
      case x        => value
    }
    variables(k) = v
    registerUpdate(k -> v)
  }

  def getPatchHere: Patch =
    world.getPatchAt(xcor, ycor)

  override def getPatchVariable(n: Int):             AnyJS = getPatchHere.getPatchVariable(n)
  override def setPatchVariable(n: Int, value: JSW): Unit  = getPatchHere.setPatchVariable(n, value)

  private def normalizedHeading(newHeading: Int, min: Int = 0, max: Int = 360): Int =
    if (newHeading < min || newHeading >= max)
      ((newHeading % max) + max) % max
    else
      newHeading

  override def toString = s"(turtle ${id.value})"

  override def id: ID = variables(IDKeyE).value.asInstanceOf[ID]

  private def heading: Int  = variables(HeadingKeyE).value.asInstanceOf[Int]
  private def xcor:    XCor = variables(XCorKeyE).   value.asInstanceOf[XCor]
  private def ycor:    YCor = variables(YCorKeyE).   value.asInstanceOf[YCor]

}

object Turtle extends VassalCompanion {

  private val DeadID     = ID(-1)

  override val trackedKeys = Set(BreedKeyE, ColorKeyE, HeadingKeyE, HiddenKeyE, IDKeyE, LabelKeyE, LabelColorKeyE,
                                 PenModeKeyE, PenSizeKeyE, ShapeKeyE, SizeKeyE, XCorKeyE, YCorKeyE)

  private var varCount = 0
  def init(n: Int): Unit = varCount = n

}
