package org.nlogo.hubnet.mirroring

import java.util.Comparator

private object ClientWorldS {

  case class TurtleKey(who: Long, breedIndex: Int)
  private val turtleOrdering = implicitly[Ordering[(Int, Long)]]
  class TurtleKeyComparator extends Comparator[TurtleKey] {
    override def compare(key1: TurtleKey, key2: TurtleKey) =
      turtleOrdering.compare((key1.breedIndex, key1.who),
                             (key2.breedIndex, key2.who))
  }

  case class LinkKey(id: Long, end1: Long, end2: Long, breedIndex: Int)
  private val linkOrdering = implicitly[Ordering[(Long, Long, Int, Long)]]
  class LinkKeyComparator extends Comparator[LinkKey] {
    override def compare(key1: LinkKey, key2: LinkKey): Int =
      linkOrdering.compare((key1.end1, key1.end2, key1.breedIndex, key1.id),
                           (key2.end1, key2.end2, key2.breedIndex, key2.id))
  }

}

import org.nlogo.api

class ClientWorld(printErrors: Boolean = true, numPatches: Option[java.lang.Integer] = None)
extends ClientWorldJ(printErrors, numPatches) with Overrides {

  override def links = unsupported
  override def turtles = unsupported
  override def patches = unsupported
  override def program = unsupported
  override def turtleShapeList = unsupported
  override def linkShapeList = unsupported
  override def patchesWithLabels = unsupported
  override def getPatch(i: Int) = unsupported
  override def getPatchAt(x: Double, y: Double) = unsupported
  override def observer = unsupported
  override def getDrawing = unsupported
  override def sendPixels = unsupported
  override def markDrawingClean = unsupported
  override def protractor = unsupported
  override def wrappedObserverX(x: Double) = unsupported
  override def wrappedObserverY(y: Double) = unsupported
  override def markPatchColorsClean = unsupported
  override def markPatchColorsDirty = unsupported
  override def patchColorsDirty = unsupported
  override def fastGetPatchAt(x: Int, y: Int) = unsupported
  override def getVariablesArraySize(link: api.Link, breed: api.AgentSet) = unsupported
  override def linksOwnNameAt(i: Int) = unsupported
  override def getVariablesArraySize(turtle: api.Turtle, breed: api.AgentSet) = unsupported
  override def turtlesOwnNameAt(i: Int) = unsupported
  override def breedsOwnNameAt(breed: api.AgentSet, i: Int) = unsupported
  override def allStoredValues = unsupported
  override def mayHavePartiallyTransparentObjects = false
  override def ticks = unsupported

  private def unsupported = throw new UnsupportedOperationException
}

trait Overrides extends ClientWorldJ {

  private val overrideMap =
    collection.mutable.Map[Overridable, collection.mutable.Map[Int, AnyRef]]()

  def clearOverrides() {
    overrideMap.clear()
  }

  def updateOverrides(list: SendOverride) {
    list.`type` match {
      case AgentType.Turtle =>
        for(id <- list.overrides.keySet)
          addOverride(getTurtle(id), list.variable, list.overrides(id))
      case AgentType.Patch =>
        for (id <- list.overrides.keySet)
          addOverride(patchData(id.intValue), list.variable, list.overrides(id))
      case AgentType.Link =>
        for (id <- list.overrides.keySet)
          addOverride(getLink(id), list.variable, list.overrides(id))
      case _ =>
    }
  }

  private def addOverride(rider: Overridable, variable: Int, value: AnyRef) {
    val innerMap = overrideMap.getOrElseUpdate(rider, collection.mutable.HashMap())
    innerMap(variable) = value
  }

  def updateOverrides(list: ClearOverride) {
    list.`type` match {
      case AgentType.Turtle =>
        for (id <- list.agents)
          removeOverride(getTurtle(id), list.variable)
      case AgentType.Patch =>
        for (id <- list.agents)
          removeOverride(patchData(id.intValue), list.variable)
      case AgentType.Link =>
        for (id <- list.agents)
          removeOverride(getLink(id), list.variable)
      case _ =>
    }
  }

  private def removeOverride(rider: Overridable, variable: Int) {
    for(map <- overrideMap.get(rider))
      map -= variable
  }

  def applyOverrides() {
    for {
      (rider, overrides) <- overrideMap
      (variable, value) <- overrides
    } rider.set(variable, value)
  }

  def rollbackOverrides() {
    for (rider <- overrideMap.keySet)
      rider.rollback()
  }

}
