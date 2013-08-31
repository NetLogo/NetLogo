package org.nlogo.tortoise.api

import
  scala.js.{ annotation, Any => AnyJS },
    annotation.expose

import
  org.nlogo.tortoise.engine.{ AgentSet => EAS }

//@ Stuff like returning `Seq[Any]` seems to me to jeopardize this whole API layer.
// Essentially, shouldn't everything that gets returned be API-like and unobfuscated?
// But, if _anything_ can be leaked out, we don't know all the things that need to API-ified! --JAB (8/26/13)
object AgentSet {
  @expose def self:                                            Any      = EAS.self
  @expose def count(that: { def length: Int }):                Int      = EAS.count(that)
  @expose def ask(agentsOrAgent: Any, f: () => Boolean):       Unit     = EAS.ask(agentsOrAgent, f)
  @expose def agentFilter(agents: Seq[Any], f: () => Boolean): Seq[Any] = EAS.agentFilter(agents, f)
  @expose def die():                                           Unit     = EAS.die()
  @expose def getTurtleVariable(varNum: Int):                  AnyJS    = EAS.getTurtleVariable(varNum)
  @expose def getPatchVariable(varNum: Int):                   AnyJS    = EAS.getPatchVariable(varNum)
  @expose def setTurtleVariable(varNum: Int, value: AnyJS):    Unit     = EAS.setTurtleVariable(varNum, value)
  @expose def setPatchVariable(varNum: Int, value: AnyJS):     Unit     = EAS.setPatchVariable(varNum, value)
}
