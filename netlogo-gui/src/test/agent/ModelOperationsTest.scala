// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  java.lang.{ Double => JDouble }

import
  org.nlogo.{ core, api, internalapi },
    core.{ AgentKind, Program, WorldDimensions },
      AgentKind.{ Observer => ObserverKind, Turtle => TurtleKind },
    api.NetLogoLegacyDialect,
    internalapi.{ UpdateVariable, UpdateSuccess, UpdateFailure }

import
  org.scalatest.{ FunSuite, Inside }

import
  scala.util.{ Failure, Success, Try }

class ModelOperationsTest extends FunSuite with Inside {

  trait Helper {
    val dimensions = WorldDimensions(5, -5, 5, -5)
    val world =
      new World2D() {
        program = Program.fromDialect(NetLogoLegacyDialect).copy(userGlobals = Seq("FOO"))
        createPatches(dimensions)
        realloc()
      }
    def operations = new ModelOperations(world)
  }

  test("trying to set a turtle variable fails - not yet implemented") { new Helper {
    inside(operations(UpdateVariable("FOO", TurtleKind, 0, Double.box(0.0), Double.box(1.0)))) {
      case Failure(e: NotImplementedError) => }
  } }
  test("trying to set a nonexistent observer variable fails") { new Helper {
    inside(operations(UpdateVariable("ABC", ObserverKind, 0, Double.box(0.0), Double.box(1.0)))) {
      case Failure(e: IllegalArgumentException) => }
  } }

  test("setting an observer variable with improper expectation fails update") { new Helper {
    val update = UpdateVariable("FOO", ObserverKind, 0, Double.box(-1.0), Double.box(1.0))
    inside(operations(update)) {
      case Success(UpdateFailure(`update`, d: JDouble)) => assertResult(0.0)(d.doubleValue) }
  } }

  test("setting an observer variable with proper expectation updates value") { new Helper {
    val update = UpdateVariable("FOO", ObserverKind, 0, Double.box(0.0), Double.box(1.0))
    inside(operations(update)) { case Success(UpdateSuccess(`update`)) => }
    assertResult(Double.box(1.0))(world.getObserverVariableByName("FOO"))
  } }
}
