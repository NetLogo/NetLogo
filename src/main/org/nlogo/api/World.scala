// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait World {
  def patchSize: Double
  def worldWidth: Int
  def worldHeight: Int
  def minPxcor: Int
  def minPycor: Int
  def maxPxcor: Int
  def maxPycor: Int
  def wrappingAllowedInX: Boolean
  def wrappingAllowedInY: Boolean
  def wrap(pos: Double, min: Double, max: Double): Double
  def ticks: Double
  def observer: Observer
  def getPatch(i: Int): Patch
  @throws(classOf[AgentException])
  def getPatchAt(x: Double, y: Double): Patch
  def fastGetPatchAt(x: Int, y: Int): Patch
  def patchColors: Array[Int]
  def patchesAllBlack: Boolean
  def patchesWithLabels: Int
  def followOffsetX: Double
  def followOffsetY: Double
  @throws(classOf[AgentException])
  def wrapX(x: Double): Double
  @throws(classOf[AgentException])
  def wrapY(y: Double): Double
  def turtles: AgentSet
  def patches: AgentSet
  def links: AgentSet
  def program: Program
  def turtleShapeList: ShapeList
  def linkShapeList: ShapeList
  def getDrawing: AnyRef
  def sendPixels: Boolean
  def markDrawingClean()
  def protractor: Protractor
  def wrappedObserverX(x: Double): Double
  def wrappedObserverY(y: Double): Double
  def patchColorsDirty: Boolean
  def markPatchColorsDirty()
  def markPatchColorsClean()
  def getBreed(name: String): AgentSet
  def getLinkBreed(name: String): AgentSet
  def getVariablesArraySize(link: Link, breed: AgentSet): Int
  def getVariablesArraySize(turtle: Turtle, breed: AgentSet): Int
  def linksOwnNameAt(i: Int): String
  def turtlesOwnNameAt(i: Int): String
  def breedsOwnNameAt(breed: AgentSet, i: Int): String
  def allStoredValues: Iterator[AnyRef]
}
