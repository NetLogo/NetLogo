// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.{ AgentKind, ShapeListTracker }
import org.nlogo.core.Shape.{ VectorShape }
import org.scalatest.{ FunSuite, OneInstancePerTest }

class BreedShapesTests extends FunSuite with OneInstancePerTest {
  val airplaneShape = new VectorShape {
    val name = "AIRPLANE"
    def name_=(n: String): Unit = {}
    val rotatable = false
    val editableColorIndex = 0
    val elements = Seq()
  }

  val shapeTracker = new ShapeListTracker(AgentKind.Turtle, Map("AIRPLANE" ->  airplaneShape))

  def breedShapes = {
    val bs = new BreedShapes("TURTLES", shapeTracker)
    val map = new java.util.HashMap[String, AgentSet]()
    map.put("FOOS", foos)
    bs.setUpBreedShapes(false, map)
    bs
  }
  val foos = new ArrayAgentSet(AgentKind.Turtle, "FOOS", Array.empty[Agent])

  test("setUpBreedShapes with clear removes breed shape associations") {
    val bs = breedShapes
    val map = new java.util.HashMap[String, AgentSet]()
    map.put("FOOS", foos)
    bs.setBreedShape(foos, "AIRPLANE")
    bs.setUpBreedShapes(true, map)
    assert(! bs.breedHasShape(foos))
  }

  test("setUpBreedShapes without clear leaves breeds the same") {
    val bs = breedShapes
    val map = new java.util.HashMap[String, AgentSet]()
    map.put("FOOS", foos)
    bs.setBreedShape(foos, "AIRPLANE")
    bs.setUpBreedShapes(false, map)
    assert(bs.breedHasShape(foos))
  }

  test("removeFromBreedShapes sets the breed shape to default") {
    val bs = breedShapes
    bs.setBreedShape(foos, "AIRPLANE")
    bs.removeFromBreedShapes("AIRPLANE")
    assert(bs.breedShape(foos) == "default")
  }

  test("if no shape is set for a given breed, it returns the default shape for that agent kind") {
    val bs = breedShapes
    assert(bs.breedShape(foos) == "default")
  }

  test("breeds have no shape if not set") {
    val bs = breedShapes
    assert(! bs.breedHasShape(foos))
  }

  test("can have breed shapes set individually") {
    val bs = breedShapes
    bs.setBreedShape(foos, "AIRPLANE")
    assert(bs.breedHasShape(foos))
    assert(bs.breedShape(foos) == "AIRPLANE")
  }

  test("removes the shape when the ShapeTracker removes the shape") {
    val bs = breedShapes
    bs.setBreedShape(foos, "AIRPLANE")
    shapeTracker.removeShape(airplaneShape)
    assert(bs.breedShape(foos) == "default")
  }

  test("removes the shape when the ShapeTracker purges shapes") {
    val bs = breedShapes
    bs.setBreedShape(foos, "AIRPLANE")
    shapeTracker.replaceShapes(Seq())
    assert(bs.breedShape(foos) == "default")
  }
}
