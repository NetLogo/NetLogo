package org.nlogo.tortoise

import
  scala.reflect.ClassTag

import
  org.nlogo.tortoise.adt.{ ArrayJS, BooleanJS, DynamicJS, EnhancedArray }

package object api {

  import engine._, wrapper._

  type JSFunc = DynamicJS // For now. --JAB (8/31/13)

  implicit def agentArrWrapped2AgentArrUnwrapped(arr: ArrayJS[AgentWrapper]): ArrayJS[Agent] = arr.E map (_.value)

  implicit def agentArrUnwrapped2AgentArrWrapped[T <: Agent : ClassTag, U <: AgentWrapper](arr: ArrayJS[T])(implicit f: (T) => U): ArrayJS[U] = arr.E map f

  implicit def agent2Wrapper(agent: Agent): AgentWrapper =
    agent match {
      case p: Patch  => p
      case t: Turtle => t
      case x         => throw new Exception(s"Unknown agent type: ${x.getClass.getName}")
    }

  implicit def patch2Wrapper (patch: Patch):   PatchWrapper  = new PatchWrapper(patch)
  implicit def turtle2Wrapper(turtle: Turtle): TurtleWrapper = new TurtleWrapper(turtle)
  implicit def world2Wrapper (world: World):   WorldWrapper  = new WorldWrapper(world)

  implicit def wrapper2Unwrapped[T <: Wrapper](wrapper: T): T#ValueType = wrapper.value

  def unwrap[T <: Wrapper] = wrapper2Unwrapped[T] _

  implicit class Func2Thunk(jsFunc: JSFunc) {
    def toBooleanThunk: () => Boolean = () => BooleanJS.toBoolean(jsFunc.value().asInstanceOf[BooleanJS])
    def toThunk[T]: () => T = () => jsFunc.value().asInstanceOf[T]
  }

}
