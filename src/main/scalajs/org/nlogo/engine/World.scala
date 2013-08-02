package org.nlogo.engine

import scala.js.Dynamic.{ global => g }

class World(val minPxcor: Int, val maxPxcor: Int, val minPycor: Int, val maxPycor: Int) {

  // These global accessors and my getters on `var`s...--they're not really referentially transparent.  I shouldn't act like they are.... --JAB (8/1/13)
  private def Random     = g.Random
  private def StrictMath = g.StrictMath

  private var _nextId:  Int         = 0
  private var _turtles: Seq[Turtle] = Seq()
  private var _patches: Seq[Patch]  = Seq()

  private val _width:    Int      = maxPxcor - minPxcor + 1
  private val _topology: Topology = new Torus(this, XCor(minPxcor), XCor(maxPxcor), YCor(minPycor), YCor(maxPycor))

  Overlord.flushUpdates()
  createPatches()

  def turtles  = _turtles
  def patches  = _patches
  def topology = _topology

  private def createPatches(): Unit = {

    val nested = for {
      y <- maxPycor to minPycor by -1
      x <- minPxcor to maxPxcor
    } yield {
      val id = (_width * (maxPycor - y)) + x - minPxcor
      new Patch(ID(id), this, XCor(x), YCor(y))
    }

    _patches = nested

  }

  // TODO: this needs to support all topologies
  def getPatchAt(x: XCor, y: YCor): Patch = {
    val index = (maxPycor - StrictMath.round(y.value)) * _width + (StrictMath.round(x.value) - minPxcor)
    _patches(index.toInt)
  }

  def removeTurtle(id: ID): Unit =
    _turtles = _turtles.filterNot(_.id == id)

  def clearall(): Unit = {
    Globals.init(Globals.vars.length)
    _turtles foreach (_.die())
    createPatches()
    _nextId = 0
  }

  def createorderedturtles(n: Int): Unit =
    0 until n foreach (x => createturtle(XCor(0), YCor(0), NLColor((x * 10 + 5) % 140), x * (360 / n)))

  // The same as `Patch.sprout`, and very similar to `createorderedturtles` above --JAB (7/26/13)
  def createturtles(n: Int): Unit =
    0 until n foreach (_ => createturtle(XCor(0), YCor(0), NLColor(5 + 10 * Random.nextInt(14)), Random.nextInt(360)))

  def createturtle(x: XCor, y: YCor, color: NLColor, heading: Int): Unit = {
    _turtles = _turtles :+ new Turtle(ID(_nextId), this, color, heading, x, y)
    _nextId += 1
  }

  def getNeighbors(pxcor: XCor, pycor: YCor): Seq[Patch] =
    _topology.getNeighbors(pxcor, pycor)

}
