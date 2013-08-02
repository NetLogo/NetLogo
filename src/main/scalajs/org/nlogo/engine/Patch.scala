package org.nlogo.engine

import
  scala.js.Dynamic.{ global => g }

// Why isn't `id` a variable like all the others, like it is with `Turtle`s? --JAB (8/1/13)
class Patch private (override val id: ID, world: World, variables: VarMap) extends Vassal with CanTalkToPatches {

  import Patch._

  private val Random = g.Random

  override protected def updateType = Overlord.UpdateType.PatchType
  override protected def companion  = Patch

  registerUpdate("pxcor" -> pxcor, "pycor" -> pycor, "pcolor" -> pcolor, "plabel" -> plabel, "plabelcolor" -> plabelcolor)

  def this(id: ID, world: World, pxcor: XCor, pycor: YCor, pcolor: NLColor = NLColor(0.0),
           plabel: String = "",  plabelcolor: NLColor = NLColor(9.9)) {
    this(id, world, {
      import Patch._
      val builtins = VarMap(
        PxcorKey -> pxcor, PycorKey -> pycor, PcolorKey -> pcolor, PlabelKey -> plabel, PlabelcolorKey -> plabelcolor
      )
      builtins ++ (1 to varCount map (x => (x + builtins.size).toString -> (0: JSW)))
    }.asInstanceOf[VarMap])
  }

  // Actually, returns `AnyJS`... --JAB (8/1/13)
  override def getPatchVariable(n: Int): Any =
    variables.toSeq(n)._2.toJS

  // This sucks --JAB (8/1/13)
  // Actually, I'm not sure that this matching is even necessary
  override def setPatchVariable(n: Int, value: JSW): Unit = {

    val k      = variables.toSeq(n)._1
    val v: JSW = k match {
      case PxcorKey                   => XCor   (value.value.asInstanceOf[Double])
      case PycorKey                   => YCor   (value.value.asInstanceOf[Double])
      case PcolorKey | PlabelcolorKey => NLColor(value.value.asInstanceOf[Double])
      case x                          => value
    }

    val newEntry = k -> v
    variables += newEntry
    registerUpdate(newEntry)

  }

  def getNeighbors: Seq[Patch] =
    world.getNeighbors(pxcor, pycor)

  def sprout(n: Int): Unit =
    0 until n foreach (_ => world.createturtle(pxcor, pycor, NLColor(5 + 10 * Random.nextInt(14)), Random.nextInt(360)))

  override def toString = s"(patch ${pxcor.value} ${pycor.value})"

  def pxcor:       XCor    = variables(PxcorKey).      value.asInstanceOf[XCor]
  def pycor:       YCor    = variables(PycorKey).      value.asInstanceOf[YCor]
  def pcolor:      NLColor = variables(PcolorKey).     value.asInstanceOf[NLColor]
  def plabel:      String  = variables(PlabelKey).     value.asInstanceOf[String]
  def plabelcolor: NLColor = variables(PlabelcolorKey).value.asInstanceOf[NLColor]

}

object Patch extends VassalCompanion {

  private val PxcorKey       = "pxcor"
  private val PycorKey       = "pycor"
  private val PcolorKey      = "pcolor"
  private val PlabelKey      = "plabel"
  private val PlabelcolorKey = "plabelcolor"

  override val trackedKeys = Set(PxcorKey, PycorKey, PcolorKey, PlabelKey, PlabelcolorKey)

  private var varCount = 0
  def init(n: Int): Unit = varCount = n

}

trait CanTalkToPatches {
  def getPatchVariable(n: Int): Any
  def setPatchVariable(n: Int, value: JSW): Unit
}
