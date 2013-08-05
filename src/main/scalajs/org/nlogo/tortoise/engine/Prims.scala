package org.nlogo.tortoise.engine

import
  scala.js.Dynamic.{ global => g }

object Prims {

  // I don't think we can cache the objects from the global scope, for fear that they might get globally swapped out...? --JAB (7/26/13)

  // We should have a concept of something crazy... like a "Workspace", or something, which has exactly one `World`.
  // Everything that's currently being pull in from the global namespace (`World`, `Random`, `StrictMath`, even jQuery, if we wanted)
  // Could then be injected into this "Workspace" when it was first created, and we wouldn't need to call into the global scope. --JAB (8/5/13)
  private def getWorld():  World = g.world.asInstanceOf[World]

  def fd(n: Double):               Unit       = AgentSet.self.asInstanceOf[Turtle].fd(n)
  def bk(n: Double):               Unit       = AgentSet.self.asInstanceOf[Turtle].fd(-n)
  def right(n: Int):               Unit       = AgentSet.self.asInstanceOf[Turtle].right(n)
  def left(n: Int):                Unit       = AgentSet.self.asInstanceOf[Turtle].right(-n)
  def setxy(x: Double, y: Double): Unit       = AgentSet.self.asInstanceOf[Turtle].setxy(x, y)
  def getNeighbors:                Seq[Patch] = AgentSet.self.asInstanceOf[Patch].getNeighbors
  def sprout(n: Int):              Unit       = AgentSet.self.asInstanceOf[Patch].sprout(n)
  def patch(x: Double, y: Double): Patch      = getWorld().getPatchAt(XCor(x), YCor(y))

  // This logic belongs on the `World` object, itself. --JAB (8/3/13)
  def randomxcor: Double = {
    val w = getWorld()
    w.minPxcor - 0.5 + g.Random.nextDouble() * (w.maxPxcor - w.minPxcor + 1)
  }

  def randomycor: Double = {
    val w = getWorld()
    w.minPycor - 0.5 + g.Random.nextDouble() * (w.maxPycor - w.minPycor + 1)
  }

}
