package org.nlogo.tortoise.engine

import
  scala.js.{ Any => AnyJS, Dynamic },
    Dynamic.{ global => g }

import EngineVariableNames.PatchE._

class Patch private (override val id: ID, world: World, variables: VarMap) extends Vassal with CanTalkToPatches {

  private val Random = g.Random

  override protected def updateType = Overlord.UpdateType.PatchType
  override protected def companion  = Patch

  registerUpdate(PXCorKeyE -> pxcor, PYCorKeyE -> pycor, PColorKeyE -> pcolor, PLabelKeyE -> plabel, PLabelColorKeyE -> plabelcolor)

  def this(id: ID, world: World, pxcor: XCor, pycor: YCor, pcolor: NLColor = NLColor(0.0),
           plabel: String = "",  plabelcolor: NLColor = NLColor(9.9)) {
    this(id, world, {
      import Patch._
      val builtins = VarMap(
        PXCorKeyE -> pxcor, PYCorKeyE -> pycor, PColorKeyE -> pcolor, PLabelKeyE -> plabel, PLabelColorKeyE -> plabelcolor
      )
      builtins ++ (1 to varCount map (x => (x + builtins.size).toString -> (0: JSW)))
    }.asInstanceOf[VarMap])
  }

  override def getPatchVariable(n: Int): AnyJS =
    variables.toSeq(n)._2.toJS

  override def setPatchVariable(n: Int, value: JSW): Unit = {

    val k      = variables.toSeq(n)._1
    val v: JSW = k match {
      case PXCorKeyE                    => XCor   (value.value.asInstanceOf[Double])
      case PYCorKeyE                    => YCor   (value.value.asInstanceOf[Double])
      case PColorKeyE | PLabelColorKeyE => NLColor(value.value.asInstanceOf[Double])
      case x                            => value
    }

    variables(k) = v
    registerUpdate(k -> v)

  }

  def getNeighbors: Seq[Patch] =
    world.getNeighbors(pxcor, pycor)

  def sprout(n: Int): Unit =
    world.createNTurtles(n, pxcor = pxcor, pycor = pycor)

  override def toString = s"(patch ${pxcor.value} ${pycor.value})"

  def pxcor:       XCor    = variables(PXCorKeyE).      value.asInstanceOf[XCor]
  def pycor:       YCor    = variables(PYCorKeyE).      value.asInstanceOf[YCor]
  def pcolor:      NLColor = variables(PColorKeyE).     value.asInstanceOf[NLColor]
  def plabel:      String  = variables(PLabelKeyE).     value.asInstanceOf[String]
  def plabelcolor: NLColor = variables(PLabelColorKeyE).value.asInstanceOf[NLColor]

}

object Patch extends VassalCompanion {

  override val trackedKeys = Set(PXCorKeyE, PYCorKeyE, PColorKeyE, PLabelKeyE, PLabelColorKeyE)

  private var varCount = 0
  def init(n: Int): Unit = varCount = n

}

trait CanTalkToPatches {
  def getPatchVariable(n: Int):             AnyJS
  def setPatchVariable(n: Int, value: JSW): Unit
}
