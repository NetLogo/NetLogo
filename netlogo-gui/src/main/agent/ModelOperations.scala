// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  org.nlogo.{ core, internalapi },
    core.AgentKind.{ Observer => ObserverKind },
    internalapi.{ ModelOperation, ModelUpdate, UpdateFailure, UpdateSuccess, UpdateVariable }

import
  scala.util.Try

// Note: This is placed in agent only because it has dependencies limited to agent
// at the time it was authored. This could almost certainly be moved into nvm or Workspace in the future
class ModelOperations(world: World) extends (ModelOperation => Try[ModelUpdate]) {
  def apply(operation: ModelOperation): Try[ModelUpdate] = {
    Try(operation match {
      case uv@UpdateVariable(name, ObserverKind, _, expected, update) =>
        val varIndex = world.observer.variableIndex(name.toUpperCase)
        if (varIndex == -1)
          throw new IllegalArgumentException(s""""${name}" not found""")
        else {
          val current = world.observer.variables(varIndex)
          if (current == expected) {
            world.observer.setVariable(varIndex, update)
            UpdateSuccess(uv)
          } else {
            UpdateFailure(uv, current)
          }
        }
      case _ => throw new NotImplementedError()
    })
  }
}

