// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite
import org.nlogo.core.{ Breed, Program, WorldDimensions }

import scala.collection.immutable.ListMap

class WorldTests extends FunSuite with AbstractTestWorld {

  val worldSquare = new WorldDimensions(-2, 2, -2, 2)
  val worldRectangle = new WorldDimensions(-3, 3, -2, 2)
  val turtles5 = Array(Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0), Array(0, 0))
  val turtles2 = Array(Array(0, 1), Array(0, -1))
  val link1 = Array(0, 1)

  override def makeWorld(dimensions: WorldDimensions) =
    new World() {
      createPatches(dimensions)
      realloc()
    }
  override def makeTurtle(world: World, cors: Array[Int]) =
    new Turtle(world, world.turtles(),
               cors(0).toDouble, cors(1).toDouble)
  override def makeLink(world: World, ends: Array[Int]) =
    new Link(world, world.getTurtle(ends(0)),
             world.getTurtle(ends(1)), world.links)

  def makeBreededTurtle(world: World, breedName: String) =
    new Turtle(world, world.getBreed(breedName), 0.0, 0.0)

  def makeBreededLink(world: World, breedName: String, end1: Int, end2: Int) =
    new Link(world, world.getTurtle(end1), world.getTurtle(end2),
      world.getLinkBreed(breedName))

  def makeWorld(dimensions: WorldDimensions, program: Program) = {
    val w = new World() {
      createPatches(dimensions)
      realloc()
    }
    w.program(program)
    w.realloc()
    w
  }

  def changeProgram(w: World, program: Program) = {
    w.rememberOldProgram()
    w.program(program)
    w.realloc()
  }

  test("IteratorSkipsDeadTurtles1_2D") {
    testIteratorSkipsDeadTurtles1(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles2_2D") {
    testIteratorSkipsDeadTurtles2(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles3_2D") {
    testIteratorSkipsDeadTurtles3(worldSquare, turtles5)
  }
  test("IteratorSkipsDeadTurtles4_2D") {
    testIteratorSkipsDeadTurtles4(worldSquare, turtles5)
  }
  test("Shufflerator1_2D") {
    testShufflerator1(worldSquare, turtles5)
  }
  test("LinkDistance_2D") {
    testLinkDistance(worldSquare, turtles2, link1)
  }
  test("ShortestPath_2D") {
    testShortestPath(worldRectangle)
  }
  test("ShortestPathHorizontalCylinder") {
    val world = makeWorld(worldRectangle)
    world.changeTopology(false, true)
    assertResult(3.0)(world.topology.shortestPathY(2, -2))
    assertResult(-2.0)(world.topology.shortestPathX(2, -2))
  }
  test("ChangePublishedAfterWorldResize_2D") {
    testChangePublishedAfterWorldResize(worldSquare, worldRectangle)
  }
  test("saves turtles between recompiles") {
    val world = makeWorld(worldRectangle, Program.empty)
    for (i <- 1 to 100) { makeTurtle(world, Array(0, 0)) }
    changeProgram(world, Program.empty)
    assertResult(100)(world.turtles.count)
  }
  test("saves breeds between recompiles") {
    val breedProgram = Program.empty.copy(breeds = ListMap("FOOS" -> Breed("FOOS", "FOO")))
    val world = makeWorld(worldRectangle, breedProgram)
    for (i <- 1 to 100) { makeBreededTurtle(world, "FOOS") }
    changeProgram(world,
      breedProgram.copy(breeds = breedProgram.breeds + ("BARS" -> Breed("BARS", "BAR"))))
    assertResult(100)(world.turtles.count)
    assertResult(100)(world.getBreed("FOOS").count)
    assertResult(0)(world.getBreed("BARS").count)
  }
  test("saves link breeds between recompiles") {
    val lbProgram = Program.empty.copy(linkBreeds = ListMap("FOOS" -> Breed("FOOS", "FOO", isLinkBreed = true)))
    val world = makeWorld(worldRectangle, lbProgram)
    for (i <- 1 to 100) { makeTurtle(world, Array(0, 0)) }
    for (i <- 1 to 99) { makeBreededLink(world, "FOOS", i - 1, i) }
    changeProgram(world,
      lbProgram.copy(linkBreeds = lbProgram.linkBreeds + ("BARS" -> Breed("BARS", "BAR", isLinkBreed = true))))
    assertResult(99)(world.links.count)
    assertResult(99)(world.getLinkBreed("FOOS").count)
    assertResult(0)(world.getLinkBreed("BARS").count)
  }
  test("breedsOwnIndexOf returns the index for the breed variable") {
    val program = Program.empty.copy(breeds = ListMap("FOOS" -> Breed("FOOS", "FOO", owns = Seq("A", "B"))))
    val world = makeWorld(worldRectangle, program)
    assertResult(13)(world.breedsOwnIndexOf(world.getBreed("FOOS"), "A"))
    assertResult(14)(world.breedsOwnIndexOf(world.getBreed("FOOS"), "B"))
    assertResult(-1)(world.breedsOwnIndexOf(world.getBreed("FOOS"), "C"))
  }
  test("linkBreedsOwnIndexOf returns the index for link breed variable") {
    val program = Program.empty.copy(linkBreeds = ListMap("FOOS" -> Breed("FOOS", "FOO", owns = Seq("A", "B"), isLinkBreed = true)))
    val world = makeWorld(worldRectangle, program)
    assertResult(10)(world.linkBreedsOwnIndexOf(world.getLinkBreed("FOOS"), "A"))
    assertResult(11)(world.linkBreedsOwnIndexOf(world.getLinkBreed("FOOS"), "B"))
    assertResult(-1)(world.linkBreedsOwnIndexOf(world.getLinkBreed("FOOS"), "C"))
  }
}
