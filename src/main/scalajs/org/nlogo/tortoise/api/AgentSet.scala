package org.nlogo.tortoise.api

import
  scala.js.{ annotation, Any => AnyJS },
    annotation.expose

import
  org.nlogo.tortoise.{ api, engine },
    api.wrapper._,
    engine.{ AgentSet => EAS, ArrayJS }

object AgentSet {

  // Needed by the implicits used by `agentFilter`, for the array conversion.
  // I think this is a bug that's fixed in a later version of 2.10...? --JAB (8/31/13)
  import scala.reflect.ClassTag

  // This return type of `Any` sucks, but I don't see any way around it,
  // so long as `0` is a permissible return value. --JAB (8/31/13)
  @expose def self:                                                  Any                   = EAS.self
  @expose def count(agents: ArrayJS[AgentWrapper]):                  Int                   = EAS.count(agents.toUnwrappedSeq)
  @expose def ask(agent: AgentWrapper, f: JSFunc):                   Unit                  = EAS.ask(Seq(agent.value), f.toThunk[Unit])
  @expose def ask(agents: ArrayJS[AgentWrapper], f: JSFunc):         Unit                  = EAS.ask(agents.toUnwrappedSeq, f.toThunk[Unit])
  @expose def agentFilter(agents: ArrayJS[AgentWrapper], f: JSFunc): ArrayJS[AgentWrapper] = EAS.agentFilter(agents.toUnwrappedSeq, f.toBooleanThunk)
  @expose def die():                                                 Unit                  = EAS.die()
  @expose def getTurtleVariable(varNum: Int):                        AnyJS                 = EAS.getTurtleVariable(varNum)
  @expose def getPatchVariable(varNum: Int):                         AnyJS                 = EAS.getPatchVariable(varNum)
  @expose def setTurtleVariable(varNum: Int, value: AnyJS):          Unit                  = EAS.setTurtleVariable(varNum, value) // Use of `JS2WrapperConverter` should occur here --JAB (9/10/13)
  @expose def setPatchVariable(varNum: Int, value: AnyJS):           Unit                  = EAS.setPatchVariable(varNum, value)

}
