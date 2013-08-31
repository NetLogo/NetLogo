package org.nlogo.tortoise

package object api {
  import engine._, wrapper._
  implicit def world2Wrapper(world: World) = new WorldWrapper(world)
}
