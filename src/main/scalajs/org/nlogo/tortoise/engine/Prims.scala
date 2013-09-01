package org.nlogo.tortoise.engine

import
  scala.js.Dynamic.{ global => g }

object Prims {

  // I don't think we can cache the objects from the global scope, for fear that they might get globally swapped out...? --JAB (7/26/13)

  // We should have a concept of something crazy... like a "Workspace", or something, which has exactly one `World`.
  // Everything that's currently being pull in from the global namespace (`World`, `Random`, `StrictMath`, even jQuery, if we wanted)
  // Could then be injected into this "Workspace" when it was first created, and we wouldn't need to call into the global scope. --JAB (8/5/13)
  private def getWorld():  World = g.world.value().asInstanceOf[World]

  def fd(n: Double):               Unit       = AgentSet.self.asInstanceOf[Turtle].fd(n)
  def bk(n: Double):               Unit       = AgentSet.self.asInstanceOf[Turtle].fd(-n)
  def right(n: Int):               Unit       = AgentSet.self.asInstanceOf[Turtle].right(n)
  def left(n: Int):                Unit       = AgentSet.self.asInstanceOf[Turtle].right(-n)
  def setxy(x: Double, y: Double): Unit       = AgentSet.self.asInstanceOf[Turtle].setxy(x, y)
  def getNeighbors:                Seq[Patch] = AgentSet.self.asInstanceOf[Patch].getNeighbors
  def sprout(n: Int):              Unit       = AgentSet.self.asInstanceOf[Patch].sprout(n)
  def patch(x: Double, y: Double): Patch      = getWorld().getPatchAt(XCor(x), YCor(y))
  def randomxcor():                Double     = getWorld().randomXCor().value
  def randomycor():                Double     = getWorld().randomYCor().value

}
