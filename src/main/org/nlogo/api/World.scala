// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.MersenneTwisterFast

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
  def clearGlobals()
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
  def trailDrawer: TrailDrawerInterface
  def sendPixels: Boolean
  def markDrawingClean()
  def protractor: Protractor
  def wrappedObserverX(x: Double): Double
  def wrappedObserverY(y: Double): Double
  def getBreed(name: String): AgentSet
  def getLinkBreed(name: String): AgentSet
  def getVariablesArraySize(link: Link, breed: AgentSet): Int
  def getVariablesArraySize(turtle: Turtle, breed: AgentSet): Int
  def linksOwnNameAt(i: Int): String
  def turtlesOwnNameAt(i: Int): String
  def breedsOwnNameAt(breed: AgentSet, i: Int): String
  def allStoredValues: Iterator[AnyRef]
  def realloc()
  def getDimensions: WorldDimensions
  def isDimensionVariable(variableName: String): Boolean
  def equalDimensions(d: WorldDimensions): Boolean
  def mainRNG: MersenneTwisterFast
  def auxRNG: MersenneTwisterFast
  def observerOwnsIndexOf(name: String): Int
  @throws(classOf[AgentException])
  def setObserverVariableByName(variableName: String, value: AnyRef)
  def timer: Timer
}
