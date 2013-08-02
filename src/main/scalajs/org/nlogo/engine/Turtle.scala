package org.nlogo.engine

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
        IDKey -> id,       ColorKey -> color,     HeadingKey -> heading,       XcorKey -> xcor,  YcorKey -> ycor,
        ShapeKey -> shape, LabelKey -> label,     LabelColorKey -> labelcolor, BreedKey -> breed, HiddenKey -> hidden,
        SizeKey -> size,   PenSizeKey -> pensize, PenModeKey -> penmode
      )
      builtins ++ (1 to varCount map (x => (x + builtins.size).toString -> (0: JSW)))
    }.asInstanceOf[VarMap])
  }

  // What's up with the '0.5's? --JAB (7/26/13)
  def fd(amount: Double): Unit = {
    variables(XcorKey) = XCor(world.topology.wrap(xcor.value + amount * Trig.sin(heading), world.minPxcor - 0.5, world.maxPxcor + 0.5))
    variables(YcorKey) = YCor(world.topology.wrap(ycor.value + amount * Trig.cos(heading), world.minPycor - 0.5, world.maxPycor + 0.5))
    registerUpdate("xcor" -> xcor, "ycor" -> ycor)
  }

  def right(amount: Int): Unit = {
    variables(HeadingKey) = normalizedHeading(heading + amount)
    registerUpdate("heading" -> heading)
  }

  def setxy(x: Double, y: Double): Unit = {
    variables(XcorKey) = XCor(x)
    variables(YcorKey) = YCor(y)
    registerUpdate("xcor" -> xcor, "ycor" -> ycor)
  }

  def die(): Unit = {
    if (id != DeadID) {
      world.removeTurtle(id)
      Overlord.registerDeath(id)
      variables(IDKey) = DeadID
    }
  }

  // Really, it's an `AnyJS` --JAB (8/1/13)
  def getTurtleVariable(n: Int): Any =
    variables.toSeq(n)._2.toJS

  // This still sucks.  Seems necessary, anyway, given the explicit casts in the variable getters.
  // We really should just use some sort of HList for this sort of thing....  --JAB (8/1/13)
  def setTurtleVariable(n: Int, value: JSW): Unit = {
    val k      = variables.toSeq(n)._1
    val v: JSW = k match {
      case IDKey   => ID  (value.value.asInstanceOf[Long])
      case XcorKey => XCor(value.value.asInstanceOf[Double])
      case YcorKey => YCor(value.value.asInstanceOf[Double])
      case x       => value
    }
    val newEntry = k -> v
    variables += newEntry
    registerUpdate(newEntry)
  }

  def getPatchHere: Patch = world.getPatchAt(xcor, ycor)

  override def getPatchVariable(n: Int):             Any  = getPatchHere.getPatchVariable(n)
  override def setPatchVariable(n: Int, value: JSW): Unit = getPatchHere.setPatchVariable(n, value)

  private def normalizedHeading(newHeading: Int, min: Int = 0, max: Int = 360): Int =
    if (newHeading < min || newHeading >= max)
      ((newHeading % max) + max) % max
    else
      newHeading

  override def toString = s"(turtle ${id.value})"

  override def id: ID = variables(IDKey).value.asInstanceOf[ID]

  private def heading: Int  = variables(HeadingKey).value.asInstanceOf[Int]
  private def xcor:    XCor = variables(XcorKey).   value.asInstanceOf[XCor]
  private def ycor:    YCor = variables(YcorKey).   value.asInstanceOf[YCor]

}

object Turtle extends VassalCompanion {

  private val DeadID     = ID(-1)

  private val BreedKey      = "breed"
  private val ColorKey      = "color"
  private val HeadingKey    = "heading"
  private val HiddenKey     = "hidden"
  private val IDKey         = "id"
  private val LabelKey      = "label"
  private val LabelColorKey = "labelcolor"
  private val PenModeKey    = "penmode"
  private val PenSizeKey    = "pensize"
  private val ShapeKey      = "shape"
  private val SizeKey       = "size"
  private val XcorKey       = "xcor"
  private val YcorKey       = "ycor"

  override val trackedKeys = Set(BreedKey, ColorKey, HeadingKey, HiddenKey, IDKey, LabelKey, LabelColorKey,
                                 PenModeKey, PenSizeKey, ShapeKey, SizeKey, XcorKey, YcorKey)

  private var varCount = 0
  def init(n: Int): Unit = varCount = n

}
