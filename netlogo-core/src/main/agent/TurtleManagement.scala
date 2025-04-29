// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ AgentKind, Program, ShapeList, ShapeListTracker }

import java.lang.{ Double => JDouble }

import java.util.{ HashMap => JHashMap, Map => JMap }

import World.NegativeOneInt

trait TurtleManagement extends WorldKernel { this: CoreWorld =>
  def clearObserverPosition(): Unit
  def program: Program
  protected def breedsOwnCache: JHashMap[String, Integer]

  // Turtle creation is a responsibility of the World
  def createTurtle(breed: AgentSet): Turtle
  def createTurtle(breed: AgentSet, color: Int, heading: Int): Turtle

  val turtleShapes = new ShapeListTracker(AgentKind.Turtle)
  val turtleBreedShapes = new BreedShapes("TURTLES", turtleShapes)

  private var _nextTurtleIndex: Long = 0

  protected val _turtles: TreeAgentSet = new TreeAgentSet(AgentKind.Turtle, "TURTLES")
  def turtles: TreeAgentSet = _turtles

  var breeds: JMap[String, TreeAgentSet] = new JHashMap[String, TreeAgentSet]()

  protected val lineThicknesses: JMap[Agent, JDouble] = new JHashMap[Agent, JDouble]()

  def turtleShapeList: ShapeList = turtleShapes.shapeList

  def turtlesOwnNameAt(index: Int): String = program.turtlesOwn(index)
  def turtlesOwnIndexOf(name: String): Int = program.turtlesOwn.indexOf(name)

  def getBreed(breedName: String): TreeAgentSet = breeds.get(breedName)
  def isBreed(breed: AgentSet): Boolean =
    program.breeds.isDefinedAt(breed.printName)
  def breedOwns(breed: AgentSet, name: String): Boolean =
    breed != turtles && breedsOwnIndexOf(breed, name) != -1
  def breedsOwnNameAt(breed: org.nlogo.api.AgentSet, index: Int): String =
    program.breeds(breed.printName).owns(index - program.turtlesOwn.size)
  def breedsOwnIndexOf(breed: AgentSet, name: String): Int =
    breedsOwnCache.getOrDefault(breed.printName + "~" + name, NegativeOneInt).intValue
  def getBreedSingular(breed: AgentSet): String =
    if (breed == turtles) "TURTLE"
    else
      program.breeds.get(breed.printName).map(_.singular).getOrElse("TURTLE")

  def getTurtle(id: Long): Turtle =
    _turtles.getAgent(JDouble.valueOf(id.toDouble)).asInstanceOf[Turtle]

  def nextTurtleIndex(nextTurtleIndex: Long): Unit = {
    _nextTurtleIndex = nextTurtleIndex
  }

  def nextTurtleIndex: Long = _nextTurtleIndex

  def setLineThickness(agent: Agent, size: Double): Unit = {
    lineThicknesses.put(agent, JDouble.valueOf(size))
  }

  def lineThickness(agent: Agent): Double = {
    val size = lineThicknesses.get(agent)
    if (size != null)
      size.doubleValue()
    else
      0.0
  }

  def removeLineThickness(agent: Agent): Unit =
    lineThicknesses.remove(agent)

  // null indicates failure
  def checkTurtleShapeName(name: String): String = {
    val lowName = name.toLowerCase()
    if (turtleShapeList.exists(lowName)) lowName
    else                                 null
  }

  def newTurtleId(): Long = {
    val r = _nextTurtleIndex
    _nextTurtleIndex += 1
    r
  }

  abstract override def clearAll(): Unit = {
    super.clearAll()
    clearTurtles()
  }

  def clearTurtles(): Unit = {
    if (program.breeds.nonEmpty) {
      val breedIterator = breeds.values.iterator
      while (breedIterator.hasNext) {
        breedIterator.next().asInstanceOf[TreeAgentSet].clear()
      }
    }
    val iter = turtles.iterator
    while (iter.hasNext) {
      val turtle = iter.next().asInstanceOf[Turtle]
      lineThicknesses.remove(turtle)
      linkManager.cleanupTurtle(turtle)
      turtle._id = -1
    }
    turtles.clear()
    val patchIter = patches.iterator
    while (patchIter.hasNext) {
      patchIter.next().asInstanceOf[Patch].clearTurtles()
    }
    _nextTurtleIndex = 0
    clearObserverPosition()
  }

  def getVariablesArraySize(turtle: org.nlogo.api.Turtle, breed: org.nlogo.api.AgentSet): Int = {
    if (breed == _turtles) {
      program.turtlesOwn.size
    } else {
      if (breed == null) throw new IllegalArgumentException("invalid breed")
      val breedOwns = program.breeds(breed.printName).owns
      program.turtlesOwn.size + breedOwns.size
    }
  }

}

