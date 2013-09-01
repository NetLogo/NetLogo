package org.nlogo.tortoise

import
  scala.{ js, reflect },
    reflect.ClassTag

package object api {

  import engine._, wrapper._

  implicit def agent2Wrapper(agent: Agent): AgentWrapper =
    agent match {
      case p: Patch  => p
      case t: Turtle => t
      case x         => throw new Exception(s"Unknown agent type: ${x.getClass.getName}")
    }

  implicit def patch2Wrapper (patch: Patch):   PatchWrapper  = new PatchWrapper(patch)
  implicit def turtle2Wrapper(turtle: Turtle): TurtleWrapper = new TurtleWrapper(turtle)
  implicit def world2Wrapper (world: World):   WorldWrapper  = new WorldWrapper(world)

  // Odd that I couldn't find a way to write this with a view bound (`<%`) --JAB (8/31/13)
  implicit def seq2JS[T <: Wrapper : ClassTag, U](xs: Seq[U])(implicit f: U => T): ArrayJS[T] = AnyJS.fromArray(xs map f toArray)

  implicit def wrapper2Unwrapped[T <: Wrapper](wrapper: T): T#ValueType = wrapper.value

  implicit class EnhancedWrapperArrayJS[T <: Wrapper : ClassTag](xs: ArrayJS[T]) {
    def toUnwrappedSeq: Seq[T#ValueType] = AnyJS.toArray(xs) map (_.value) toSeq
  }

}
