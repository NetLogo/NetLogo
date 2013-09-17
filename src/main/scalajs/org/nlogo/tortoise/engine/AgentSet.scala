package org.nlogo.tortoise.engine

import
  org.nlogo.tortoise.adt.{ AnyJS, ArrayJS, EnhancedArray, JS2WrapperConverter }

object AgentSet {

  private var _self: Option[Agent] = None

  // The real source of our `self` return type woes.  Couldn't we fix this by
  // just having the compiler output functions that take the agent as an argument? --JAB (8/31/13)

  // The answer is a resounding "YES!".  I was worried that NetLogo might return `0` as a `self` value
  // when no `self` is available (i.e. in the observer context), but it looks like that's not the case;
  // only agents are even allowed to have `self` called on them. --JAB (8/31/13)
  def self: Any = _self getOrElse 0

  def count[T <: { def length: Int }](x: T) = x.length

  // This might be one of the most atrocious things --JAB (7/19/13)
  // I mean... why not just make the function take the agent as an argument? --JAB (8/3/13)
  def askAgent[T](a: Agent, f: () => T): T = {
    val old    = _self
    _self      = Option(a)
    val result = f()
    _self      = old
    result
  }

  def ask(agents: ArrayJS[Agent], f: () => Unit): Unit = {
    agents.E foreach (a => askAgent(a, f))
  }

  def agentFilter(agents: ArrayJS[Agent], f: () => Boolean): ArrayJS[Agent] =
    agents.E filter (agent => askAgent(agent, f))

  // I'm putting some things in Agents, and some in Prims
  // I did that on purpose to show how arbitrary/confusing this seems.
  // May we should put *everything* in Prims, and Agents can be private.
  // Prims could/would/should be the compiler/runtime interface. --SAT
  // Seth was onto something!  --JAB (8/3/13)

  // In the future: Links are also capable of dying --JAB (7/19/13)
  def die(): Unit = self.asInstanceOf[Turtle].die()

  // Because of some "the compiler's not giving me a `JSW`" insanity, I think we need some well-established entry points into the ScalaJS code...
  // (See above, where Seth was "onto something"; we should have some interface layer where inputs are converted from ScalaJS types to Scala types,
  // then forwarded to the rest of the ScalaJS engine) --JAB (8/1/13)
  def getTurtleVariable(n: Int): AnyJS = self.asInstanceOf[Turtle].getTurtleVariable(n)
  def getPatchVariable(n: Int):  AnyJS = self.asInstanceOf[CanTalkToPatches].getPatchVariable(n)

  def setTurtleVariable(n: Int, value: AnyJS): Unit =
    self.asInstanceOf[Turtle].setTurtleVariable(n, JS2WrapperConverter(value))

  def setPatchVariable(n: Int, value: AnyJS): Unit =
    self.asInstanceOf[CanTalkToPatches].setPatchVariable(n, JS2WrapperConverter(value))

}

// Choosing a non-ideal solution to get around a bug.  All of this implementation used to be in some `trait Agents`. --JAB (7/30/13)
//object AgentSet extends Agents
