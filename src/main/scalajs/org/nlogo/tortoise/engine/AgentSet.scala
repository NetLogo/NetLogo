package org.nlogo.tortoise.engine

import scala.js.{ Any => AnyJS }

// It bothers me that this agent type is `Any`.  Can't we improve it somehow?  --JAB (8/3/13)
object AgentSet {

  private var _self: Any = 0

  def self = _self

  def count[T <: { def length: Int }](x: T) = x.length

  // This might be one of the most atrocious things --JAB (7/19/13)
  // I mean... why not just make the function take the agent as an argument? --JAB (8/3/13)
  def askAgent(a: Any, f: () => Boolean): Boolean = {
    val old    = _self
    _self      = a
    val result = f()
    _self      = old
    result
  }

  def ask(agentsOrAgent: Any, f: () => Boolean): Unit = {
    val seq = agentsOrAgent match {
      case agents: Seq[_] => agents
      case x              => Seq(x)
    }
    seq foreach (a => askAgent(a, f))
  }

  def agentFilter(agents: Seq[Any], f: () => Boolean): Seq[Any] =
    agents filter (agent => askAgent(agent, f))

  // I'm putting some things in Agents, and some in Prims
  // I did that on purpose to show how arbitrary/confusing this seems.
  // May we should put *everything* in Prims, and Agents can be private.
  // Prims could/would/should be the compiler/runtime interface. --SAT
  // Seth was onto something!  --JAB (8/3/13)

  // Links can also die --JAB (7/19/13)
  def die(): Unit = _self.asInstanceOf[Turtle].die()

  // Because of this "compiler's not giving me a `JSW`" insanity, I think we need some well-established entrypoints into the ScalaJS code... --JAB (8/1/13)
  // (See above, where Seth was onto something; we should have some interface layer where inputs are converted from ScalaJS types to Scala types,
  // then forwarded to the rest of the ScalaJS engine)
  def getTurtleVariable(n: Int): AnyJS = _self.asInstanceOf[Turtle].getTurtleVariable(n)
  def getPatchVariable(n: Int):  AnyJS = _self.asInstanceOf[CanTalkToPatches].getPatchVariable(n)

  // Code redundancy! --JAB (8/1/13)
  def setTurtleVariable(n: Int, value: AnyJS): Unit = {
    val v = JS2WrapperConverter(value) getOrElse (throw new IllegalArgumentException("Could not convert input to JS wrapper!"))
    _self.asInstanceOf[Turtle].setTurtleVariable(n, v)
  }

  def setPatchVariable(n: Int, value: AnyJS): Unit = {
    val v = JS2WrapperConverter(value) getOrElse (throw new IllegalArgumentException("Could not convert input to JS wrapper!"))
    _self.asInstanceOf[CanTalkToPatches].setPatchVariable(n, v)
  }

}

// Choosing a non-ideal solution to get around a bug.  All of this implementation used to be in some `trait Agents`. --JAB (7/30/13)
//object AgentSet extends Agents
