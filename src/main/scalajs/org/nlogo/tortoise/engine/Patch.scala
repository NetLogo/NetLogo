package org.nlogo.tortoise.engine

import
  org.nlogo.tortoise.adt.{ AnyJS, ArrayJS, EnhancedArray, JSW, SetJS, VarMap }

import EngineVariableNames.PatchE._

class Patch private (override val id: ID, world: World, variables: VarMap) extends Agent with Vassal with CanTalkToPatches {

  override protected def updateType = Overlord.UpdateType.PatchType
  override protected def companion  = Patch

  registerUpdate(ArrayJS(PXCorKeyE -> pxcor, PYCorKeyE -> pycor, PColorKeyE -> pcolor, PLabelKeyE -> plabel, PLabelColorKeyE -> plabelcolor))

  def this(id: ID, world: World, pxcor: XCor, pycor: YCor, pcolor: NLColor = NLColor(0.0),
           plabel: String = "",  plabelcolor: NLColor = NLColor(9.9)) {
    this(id, world, {
      import Patch._
      val builtins = VarMap(
        ArrayJS(PXCorKeyE -> pxcor, PYCorKeyE -> pycor, PColorKeyE -> pcolor, PLabelKeyE -> plabel, PLabelColorKeyE -> plabelcolor)
      )
      builtins ++= ArrayJS(1 to varCount: _*).E map (x => (x + builtins.size).toString -> (0: JSW))
      builtins
    })
  }

  override def getPatchVariable(n: Int): AnyJS =
    variables(n)._2.toJS

  override def setPatchVariable(n: Int, value: JSW): Unit = {

    val k      = variables(n)._1
    val v: JSW = k match {
      case PXCorKeyE                    => XCor   (value.value.asInstanceOf[Double])
      case PYCorKeyE                    => YCor   (value.value.asInstanceOf[Double])
      case PColorKeyE | PLabelColorKeyE => NLColor(value.value.asInstanceOf[Double])
      case x                            => value
    }

    variables(k) = v
    registerUpdate(ArrayJS(k -> v))

  }

  def getNeighbors: ArrayJS[Patch] =
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

  override val trackedKeys = SetJS(ArrayJS(PXCorKeyE, PYCorKeyE, PColorKeyE, PLabelKeyE, PLabelColorKeyE))

  private var varCount = 0
  def init(n: Int): Unit = varCount = n

}

trait CanTalkToPatches {
  def getPatchVariable(n: Int):             AnyJS
  def setPatchVariable(n: Int, value: JSW): Unit
}
